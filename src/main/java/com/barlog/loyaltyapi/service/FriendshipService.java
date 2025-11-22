package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.FriendResponseDto;
import com.barlog.loyaltyapi.exception.ResourceNotFoundException;
import com.barlog.loyaltyapi.model.Friendship;
import com.barlog.loyaltyapi.model.FriendshipStatus;
import com.barlog.loyaltyapi.model.User;
import com.barlog.loyaltyapi.repository.FriendshipRepository;
import com.barlog.loyaltyapi.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final LevelService levelService;
    private final FileStorageService fileStorageService; // INJECTAT PENTRU AVATAR

    // --- Helpers ---
    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByNickname(identifier).map(u -> (User)u))
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul '" + identifier + "' nu a fost găsit (caută după Email sau Nickname)."));
    }

    private User[] getCanonicalUsers(User u1, User u2) {
        if (u1.getId() < u2.getId()) {
            return new User[]{u1, u2};
        } else {
            return new User[]{u2, u1};
        }
    }

    // NOU: Mapează o Relație de Prietenie/User -> FriendResponseDto
    private FriendResponseDto mapToDto(Friendship friendship, User currentUser) {
        User friend = friendship.getUserA().equals(currentUser) ? friendship.getUserB() : friendship.getUserA();

        FriendResponseDto dto = new FriendResponseDto();
        dto.setFriendshipId(friendship.getId());
        dto.setUserId(friend.getId());
        dto.setNickname(friend.getNickname());
        dto.setExperience(friend.getExperience());
        dto.setCoins(friend.getCoins());
        dto.setEmail(friend.getEmail());

        // LOGICA AVATAR
        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            dto.setAvatarUrl(fileStorageService.getImageUrlFromPublicId(friend.getAvatarUrl()));
        } else {
            dto.setAvatarUrl(null);
        }

        // LOGICA STATUS NOU (Folosind InitiatorId)
        if (friendship.getStatus() == FriendshipStatus.PENDING) {
            if (friendship.getInitiatorId().equals(currentUser.getId())) {
                dto.setStatus("OUTGOING_PENDING");
            } else {
                dto.setStatus("INCOMING_PENDING");
            }
        } else {
            dto.setStatus(friendship.getStatus().name());
        }

        dto.setLevelInfo(levelService.calculateLevelInfo(friend.getExperience()));
        return dto;
    }

    // --- Funcționalități ---
    @Transactional
    public FriendResponseDto sendFriendRequest(User sender, String identifier) {
        User receiver = findUserByIdentifier(identifier);

        if (sender.equals(receiver)) {
            throw new IllegalStateException("Nu poți trimite o cerere de prietenie către tine însuți.");
        }

        User[] canonical = getCanonicalUsers(sender, receiver);
        User userA = canonical[0];
        User userB = canonical[1];

        if (friendshipRepository.findByUserAAndUserB(userA, userB).isPresent()) {
            throw new EntityExistsException("O cerere de prietenie este deja în așteptare sau relația există deja.");
        }

        Friendship friendship = Friendship.builder()
                .userA(userA)
                .userB(userB)
                .status(FriendshipStatus.PENDING)
                .initiatorId(sender.getId()) // ESENTIAL: Salvăm cine a trimis
                .build();
        Friendship savedFriendship = friendshipRepository.save(friendship);

        return mapToDto(savedFriendship, sender);
    }

    @Transactional
    public FriendResponseDto acceptFriendRequest(User currentUser, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cerere de prietenie negăsită."));

        // Verificăm dacă utilizatorul curent este destinatarul (cel care nu e Initiator)
        if (friendship.getInitiatorId().equals(currentUser.getId())) {
            throw new IllegalStateException("Nu poți accepta o cerere pe care tu ai trimis-o.");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Cererea nu este în starea PENDING.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship acceptedFriendship = friendshipRepository.save(friendship);

        return mapToDto(acceptedFriendship, currentUser);
    }

    @Transactional
    public void deleteFriendship(User currentUser, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Relația de prietenie negăsită."));

        // Verificăm dacă utilizatorul curent face parte din relație
        if (!friendship.getUserA().equals(currentUser) && !friendship.getUserB().equals(currentUser)) {
            throw new IllegalStateException("Nu poți șterge o relație din care nu faci parte.");
        }

        friendshipRepository.delete(friendship);
    }

    // Returnează lista completă (Prietenii Acceptați + Cererile Primite/Trimise)
    public List<FriendResponseDto> getFriendshipList(User currentUser) {
        // 1. Relațiile permanente (ACCEPTED sau BLOCKED)
        List<Friendship> permanent = friendshipRepository.findPermanentFriendships(currentUser);

        // 2. Cereri PENDING (ambele direcții)
        List<Friendship> pending = friendshipRepository.findAllPendingFriendships(currentUser);

        // Combinăm și eliminăm PENDING-urile care sunt de fapt cereri către noi înșine (deși ar trebui eliminate de InitiatorId)
        List<Friendship> combinedList = Stream.concat(permanent.stream(), pending.stream())
                .distinct()
                .collect(Collectors.toList());

        return combinedList.stream()
                .map(f -> mapToDto(f, currentUser))
                .sorted(Comparator.comparing(FriendResponseDto::getStatus).reversed())
                .collect(Collectors.toList());
    }
}