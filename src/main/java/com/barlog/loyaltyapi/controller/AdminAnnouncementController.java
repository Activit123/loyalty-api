package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AnnouncementDto;
import com.barlog.loyaltyapi.dto.AnnouncementRequestDto;
import com.barlog.loyaltyapi.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnnouncementDto> createAnnouncement(
            @Valid @RequestPart("announcement") AnnouncementRequestDto requestDto,
            @RequestPart("image") MultipartFile imageFile) {
        return new ResponseEntity<>(announcementService.createAnnouncement(requestDto, imageFile), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}