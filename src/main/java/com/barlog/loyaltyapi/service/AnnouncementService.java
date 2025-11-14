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
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementRequestDto requestDto, MultipartFile imageFile) {
        // 1. Găsește anunțul existent sau aruncă o excepție
        Announcement announcementToUpdate = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anunțul cu ID-ul " + id + " nu a fost găsit."));

        // 2. Actualizează câmpurile de text
        announcementToUpdate.setTitle(requestDto.getTitle());
        announcementToUpdate.setDescription(requestDto.getDescription());

        // 3. Dacă a fost încărcată o imagine nouă, o actualizăm.
        //    Dacă exista deja o imagine veche, o ștergem de pe Cloudinary înainte de a o înlocui.
        if (imageFile != null && !imageFile.isEmpty()) {
            // Șterge imaginea veche de pe Cloudinary, dacă există
            if (announcementToUpdate.getImageUrl() != null && !announcementToUpdate.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(announcementToUpdate.getImageUrl());
            }
            // Încarcă noua imagine și obține publicId-ul
            String newPublicId = fileStorageService.storeFile(imageFile);
            announcementToUpdate.setImageUrl(newPublicId);
        }
        // NOTĂ: Dacă imageFile este null sau gol, și nu există o cerere explicită de ștergere,
        // imaginea existentă (dacă există) rămâne neschimbată.

        // 4. Salvează entitatea actualizată în baza de date
        Announcement updatedAnnouncement = announcementRepository.save(announcementToUpdate);

        // 5. Mapează entitatea la DTO și returnează rezultatul
        return mapToDto(updatedAnnouncement);
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