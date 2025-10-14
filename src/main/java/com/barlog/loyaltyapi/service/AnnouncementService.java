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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
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
        String fileName = fileStorageService.storeFile(imageFile);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/images/")
                .path(fileName)
                .toUriString();
        
        Announcement announcement = Announcement.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .build();
        
        return mapToDto(announcementRepository.save(announcement));
    }
    
    public void deleteAnnouncement(Long announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new EntityNotFoundException("Anunțul cu ID " + announcementId + " nu a fost găsit.");
        }
        announcementRepository.deleteById(announcementId);
    }

    private AnnouncementDto mapToDto(Announcement announcement) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setDescription(announcement.getDescription());
        dto.setImageUrl(announcement.getImageUrl());
        dto.setCreatedAt(announcement.getCreatedAt());
        return dto;
    }
}