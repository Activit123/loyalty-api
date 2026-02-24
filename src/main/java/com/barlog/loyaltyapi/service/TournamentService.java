package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.CreateTournamentRequest;
import com.barlog.loyaltyapi.dto.TournamentDto;
import com.barlog.loyaltyapi.dto.TournamentMatchDto;
import com.barlog.loyaltyapi.model.*;
import com.barlog.loyaltyapi.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final TournamentMatchRepository matchRepository;
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final AdminService adminService;
    private final UserNotificationService notificationService;

    // Injectăm serviciile/repozitoarele noi necesare
    private final ObjectMapper objectMapper;
    private final ItemTemplateRepository itemTemplateRepository;
    private final ProductRepository productRepository;
    private final UserItemRepository userItemRepository;
    private final UserInventoryItemRepository userInventoryItemRepository;
    private final ExperienceService experienceService;

    // --- Clasă internă pentru a stoca JSON-ul în DB fără migrații noi ---
    @Data
    public static class PrizeData {
        private String text;
        private Double xp;
        private Long itemId;
        private Long productId;
    }

    private PrizeData parsePrizeData(String raw) {
        PrizeData pd = new PrizeData();
        if (raw == null || raw.isEmpty()) return pd;
        if (raw.startsWith("{")) {
            try {
                return objectMapper.readValue(raw, PrizeData.class);
            } catch (Exception e) {
                pd.setText(raw);
            }
        } else {
            pd.setText(raw);
        }
        return pd;
    }

    // --- MAPPERE ---
    private TournamentDto mapToTournamentDto(Tournament t) {
        int count = participantRepository.countByTournamentId(t.getId());

        PrizeData pd = parsePrizeData(t.getPrizeDescription());
        String itemName = pd.getItemId() != null ? itemTemplateRepository.findById(pd.getItemId()).map(ItemTemplate::getName).orElse(null) : null;
        String prodName = pd.getProductId() != null ? productRepository.findById(pd.getProductId()).map(Product::getName).orElse(null) : null;

        return TournamentDto.builder()
                .id(t.getId())
                .title(t.getTitle())
                .gameName(t.getGameName())
                .description(t.getDescription())
                .startTime(t.getStartTime())
                .maxPlayers(t.getMaxPlayers())
                .entryFeeCoins(t.getEntryFeeCoins())
                .prizeCoins(t.getPrizeCoins())
                .prizeDescription(pd.getText())
                .rewardXp(pd.getXp())
                .rewardItemId(pd.getItemId())
                .rewardItemName(itemName)
                .rewardProductId(pd.getProductId())
                .rewardProductName(prodName)
                .status(t.getStatus())
                .enrolledCount(count)
                .build();
    }

    private TournamentMatchDto mapToMatchDto(TournamentMatch m) {
        return TournamentMatchDto.builder()
                .id(m.getId())
                .tournamentId(m.getTournament().getId())
                .roundNumber(m.getRoundNumber())
                .matchOrder(m.getMatchOrder())
                .nextMatchId(m.getNextMatch() != null ? m.getNextMatch().getId() : null)
                .player1(m.getPlayer1() != null ? adminService.mapUserToDto(m.getPlayer1()) : null)
                .player2(m.getPlayer2() != null ? adminService.mapUserToDto(m.getPlayer2()) : null)
                .winner(m.getWinner() != null ? adminService.mapUserToDto(m.getWinner()) : null)
                .build();
    }

    public List<TournamentDto> getAllTournaments() {
        return tournamentRepository.findAllByOrderByStartTimeDesc().stream()
                .map(this::mapToTournamentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TournamentDto createTournament(CreateTournamentRequest req) {
        // 1. Construim datele premiului (JSON)
        PrizeData pd = new PrizeData();
        pd.setText(req.getPrizeDescription());
        pd.setXp(req.getRewardXp());
        pd.setItemId(req.getRewardItemId());
        pd.setProductId(req.getRewardProductId());

        String jsonPrize = "";
        try {
            jsonPrize = objectMapper.writeValueAsString(pd);
        } catch (Exception e) {
            jsonPrize = req.getPrizeDescription();
        }

        // 2. Construim obiectul Turneu
        Tournament t = Tournament.builder()
                .title(req.getTitle())
                .gameName(req.getGameName())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .maxPlayers(req.getMaxPlayers())
                .entryFeeCoins(req.getEntryFeeCoins() != null ? req.getEntryFeeCoins() : 0)
                .prizeDescription(jsonPrize)
                .prizeCoins(req.getPrizeCoins() != null ? req.getPrizeCoins() : 0)
                .status("REGISTRATION_OPEN")
                .build();

        // 3. Salvăm turneul în bază
        Tournament savedTournament = tournamentRepository.save(t);

        // 4. --- NOTIFICARE CĂTRE TOȚI UTILIZATORII ---
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            notificationService.notifyUser(
                    user,
                    "🏆 Turneu Nou: " + savedTournament.getTitle() + "! Înscrierile sunt deschise.",
                    NotificationType.SYSTEM,
                    "/tournaments" // Link către pagina de turnee
            );
        }

        return mapToTournamentDto(savedTournament);
    }

    @Transactional
    public void joinTournament(User user, Long tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId).orElseThrow();
        if (!"REGISTRATION_OPEN".equals(t.getStatus())) throw new IllegalStateException("Înscrierile sunt închise.");
        if (participantRepository.countByTournamentId(tournamentId) >= t.getMaxPlayers()) throw new IllegalStateException("Turneul este plin.");
        if (participantRepository.existsByTournamentIdAndUserId(tournamentId, user.getId())) throw new IllegalStateException("Ești deja înscris.");
        if (user.getCoins() < t.getEntryFeeCoins()) throw new IllegalStateException("Fonduri insuficiente pentru taxa de înscriere.");

        if (t.getEntryFeeCoins() > 0) {
            user.setCoins(user.getCoins() - t.getEntryFeeCoins());
            userRepository.save(user);
            coinTransactionRepository.save(CoinTransaction.builder().user(user).amount(-t.getEntryFeeCoins()).transactionType("TOURNAMENT_FEE").description("Taxă turneu: " + t.getTitle()).createdAt(LocalDateTime.now()).build());
        }

        participantRepository.save(TournamentParticipant.builder().tournament(t).user(user).build());
    }

    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament t = tournamentRepository.findById(tournamentId).orElseThrow();
        if (!"REGISTRATION_OPEN".equals(t.getStatus())) throw new IllegalStateException("Bracket-ul a fost deja generat.");

        List<TournamentParticipant> participants = participantRepository.findByTournamentId(tournamentId);
        if (participants.size() < 2) throw new IllegalStateException("Nu sunt suficienți jucători (minim 2).");

        t.setStatus("ONGOING");
        tournamentRepository.save(t);

        for (TournamentParticipant participant : participants) {
            notificationService.notifyUser(participant.getUser(), "Turneul '" + t.getTitle() + "' a început! Verifică tabloul meciurilor.", NotificationType.SYSTEM, "/tournaments");
        }

        List<User> users = participants.stream().map(TournamentParticipant::getUser).collect(Collectors.toList());
        Collections.shuffle(users);

        int numPlayers = users.size();
        int rounds = (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
        int matchesInR1 = (int) Math.pow(2, rounds - 1);

        Map<Integer, List<TournamentMatch>> matchesByRound = new HashMap<>();

        for (int r = rounds; r >= 1; r--) {
            int matchesInRound = (int) Math.pow(2, rounds - r);
            List<TournamentMatch> currentRoundMatches = new ArrayList<>();
            for (int m = 0; m < matchesInRound; m++) {
                TournamentMatch match = TournamentMatch.builder().tournament(t).roundNumber(r).matchOrder(m).build();
                if (r < rounds) {
                    TournamentMatch nextMatch = matchesByRound.get(r + 1).get(m / 2);
                    match.setNextMatch(nextMatch);
                }
                match = matchRepository.save(match);
                currentRoundMatches.add(match);
            }
            matchesByRound.put(r, currentRoundMatches);
        }

        List<TournamentMatch> firstRoundMatches = matchesByRound.get(1);
        for (int i = 0; i < matchesInR1; i++) {
            if (i < numPlayers) firstRoundMatches.get(i).setPlayer1(users.get(i));
        }

        int remainingStart = matchesInR1;
        for (int i = 0; i < matchesInR1; i++) {
            int userIndex = remainingStart + i;
            if (userIndex < numPlayers) firstRoundMatches.get(i).setPlayer2(users.get(userIndex));
        }

        for (TournamentMatch match : firstRoundMatches) {
            matchRepository.save(match);
            if (match.getPlayer1() != null && match.getPlayer2() == null) {
                setMatchWinnerInternal(match, match.getPlayer1());
            }
        }
    }

    public List<TournamentMatchDto> getBracket(Long tournamentId) {
        return matchRepository.findByTournamentIdOrderByRoundNumberAscMatchOrderAsc(tournamentId)
                .stream().map(this::mapToMatchDto).collect(Collectors.toList());
    }

    @Transactional
    public void setMatchWinner(Long matchId, Long winnerId) {
        TournamentMatch match = matchRepository.findById(matchId).orElseThrow();
        User winner = userRepository.findById(winnerId).orElseThrow();

        if (match.getWinner() != null) throw new IllegalStateException("Meciul are deja un câștigător.");
        if (match.getPlayer1() == null || match.getPlayer2() == null) throw new IllegalStateException("Meciul nu are ambii jucători pregătiți încă.");
        if (!match.getPlayer1().getId().equals(winnerId) && !match.getPlayer2().getId().equals(winnerId)) {
            throw new IllegalStateException("Jucătorul specificat nu face parte din acest meci.");
        }

        setMatchWinnerInternal(match, winner);
    }

    private void setMatchWinnerInternal(TournamentMatch match, User winner) {
        match.setWinner(winner);
        matchRepository.save(match);

        Tournament t = match.getTournament();
        User loser = null;
        if (match.getPlayer1() != null && match.getPlayer2() != null) {
            loser = match.getPlayer1().getId().equals(winner.getId()) ? match.getPlayer2() : match.getPlayer1();
        }

        if (match.getNextMatch() != null) {
            if (loser != null) {
                notificationService.notifyUser(winner, "Ai câștigat meciul din runda " + match.getRoundNumber() + "! Treci mai departe.", NotificationType.SYSTEM, "/tournaments");
                notificationService.notifyUser(loser, "Ai fost eliminat din turneul '" + t.getTitle() + "'. Mult succes data viitoare!", NotificationType.SYSTEM, "/tournaments");
            }

            TournamentMatch next = match.getNextMatch();
            if (match.getMatchOrder() % 2 == 0) {
                next.setPlayer1(winner);
            } else {
                next.setPlayer2(winner);
            }
            matchRepository.save(next);

        } else {
            // --- FINALA (AICI ACORDĂM PREMIILE) ---
            t.setStatus("COMPLETED");
            tournamentRepository.save(t);

            if (loser != null) {
                notificationService.notifyUser(loser, "Ai pierdut în FINALA turneului '" + t.getTitle() + "'. Felicitări pentru parcurs!", NotificationType.SYSTEM, "/tournaments");
            }

            // ACORDARE PREMII MULTIPLE (Monede, XP, Iteme, Produse)
            StringBuilder prizeText = new StringBuilder();

            if (t.getPrizeCoins() != null && t.getPrizeCoins() > 0) {
                winner.setCoins(winner.getCoins() + t.getPrizeCoins());
                userRepository.save(winner);
                coinTransactionRepository.save(CoinTransaction.builder().user(winner).amount(t.getPrizeCoins()).transactionType("TOURNAMENT_PRIZE").description("Câștigător turneu: " + t.getTitle()).createdAt(LocalDateTime.now()).build());
                prizeText.append(t.getPrizeCoins()).append(" monede! ");
            }

            PrizeData pd = parsePrizeData(t.getPrizeDescription());

            // Acordare XP
            if (pd.getXp() != null && pd.getXp() > 0) {
                experienceService.addManualExperience(winner.getEmail(), pd.getXp());
                prizeText.append(pd.getXp()).append(" XP! ");
            }

            // Acordare Item RPG
            if (pd.getItemId() != null) {
                itemTemplateRepository.findById(pd.getItemId()).ifPresent(item -> {
                    userItemRepository.save(UserItem.builder().user(winner).itemTemplate(item).isEquipped(false).build());
                    prizeText.append("Item RPG: ").append(item.getName()).append("! ");
                });
            }

            // Acordare Produs Fizic
            if (pd.getProductId() != null) {
                productRepository.findById(pd.getProductId()).ifPresent(prod -> {
                    userInventoryItemRepository.save(UserInventoryItem.builder().user(winner).product(prod).status("IN_INVENTORY").build());
                    prizeText.append("Produs: ").append(prod.getName()).append("! ");
                });
            }

            notificationService.notifyUser(winner, "🏆 FELICITĂRI! Ai câștigat turneul '" + t.getTitle() + "'! Premii primite: " + prizeText.toString(), NotificationType.SYSTEM, "/tournaments");

            // Notificăm toți ceilalți participanți
            List<TournamentParticipant> allParticipants = participantRepository.findByTournamentId(t.getId());
            for (TournamentParticipant p : allParticipants) {
                if (!p.getUser().getId().equals(winner.getId()) && (loser == null || !p.getUser().getId().equals(loser.getId()))) {
                    notificationService.notifyUser(p.getUser(), "Turneul '" + t.getTitle() + "' s-a încheiat. Câștigătorul este " + (winner.getNickname() != null ? winner.getNickname() : winner.getFirstName()) + "!", NotificationType.SYSTEM, "/tournaments");
                }
            }
        }
    }
}