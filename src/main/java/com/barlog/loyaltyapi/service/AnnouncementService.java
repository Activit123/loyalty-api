package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.AnnouncementDto;
import com.barlog.loyaltyapi.dto.AnnouncementRequestDto;
import com.barlog.loyaltyapi.model.Announcement;
import com.barlog.loyaltyapi.repository.AnnouncementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final FileStorageService fileStorageService;

    public List<AnnouncementDto> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto createAnnouncement(AnnouncementRequestDto requestDto, MultipartFile imageFile) {
        // Salvează imaginea pe Cloudinary și obține Public ID-ul
        String publicId = fileStorageService.storeFile(imageFile);

        Announcement announcement = Announcement.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .imageUrl(publicId) // *** SALVĂM PUBLIC ID-UL CLOUDINARY ***
                .build();

        return mapToDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("Anunțul cu ID " + announcementId + " nu a fost găsit."));

        // --- LOGICĂ OPȚIONALĂ PENTRU ȘTERGEREA IMAGINII DE PE CLOUDINARY ---
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(announcement.getImageUrl());
        }
        // -------------------------------------------------------------------

        announcementRepository.deleteById(announcementId);
    }

    private AnnouncementDto mapToDto(Announcement announcement) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setDescription(announcement.getDescription());

        // *** GENERARE DINAMICĂ A URL-ULUI ***
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            dto.setImageUrl(fileStorageService.getImageUrlFromPublicId(announcement.getImageUrl()));
        } else {
            dto.setImageUrl(null);
        }

        dto.setCreatedAt(announcement.getCreatedAt());
        return dto;
    }

    public AnnouncementDto getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anunțul cu ID " + id + " nu a fost găsit."));
        return mapToDto(announcement);
    }

    // NOTĂ: Dacă există o metodă de UPDATE, ar trebui modificată pentru a șterge/suprascrie imaginea dacă este furnizată una nouă.
}