// java/com/barlog/loyaltyapi/controller/AppVersionController.java
package com.barlog.loyaltyapi.controller;

import com.barlog.loyaltyapi.dto.AppVersionResponseDto;
import com.barlog.loyaltyapi.dto.CreateAppVersionRequest;
import com.barlog.loyaltyapi.model.AppVersion;
import com.barlog.loyaltyapi.repository.AppVersionRepository;
import com.barlog.loyaltyapi.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app-version")
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionRepository appVersionRepository;
    private final FileStorageService fileStorageService;
    // Endpoint PUBLIC pentru aplicația mobilă
    @GetMapping("/latest")
    public ResponseEntity<AppVersionResponseDto> getLatestVersion(@RequestParam(defaultValue = "ANDROID") String platform) {
        return appVersionRepository.findTopByPlatformOrderByCreatedAtDesc(platform)
                .map(v -> ResponseEntity.ok(AppVersionResponseDto.builder()
                        .versionCode(v.getVersionCode())
                        .versionName(v.getVersionName())
                        .isCritical(v.isCritical())
                        .downloadUrl(v.getDownloadUrl())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint ADMIN pentru a publica o versiune nouă
    @PostMapping("/admin/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppVersion> publishVersion(@RequestBody @Valid CreateAppVersionRequest request) {
        AppVersion version = AppVersion.builder()
                .versionCode(request.getVersionCode())
                .versionName(request.getVersionName())
                .isCritical(request.isCritical())
                .downloadUrl(request.getDownloadUrl() != null ? request.getDownloadUrl() : "https://www.labarlog.ro/barlog-app.apk")
                .platform("ANDROID")
                .build();
        
        return ResponseEntity.ok(appVersionRepository.save(version));
    }

    // --- ENDPOINT NOU: Upload APK ---
    @PostMapping("/admin/upload-apk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadApk(@RequestPart("file") MultipartFile file) {
        String downloadUrl = fileStorageService.storeRawFile(file);
        // Returnăm direct URL-ul pentru a fi pus în formularul din frontend
        return ResponseEntity.ok(downloadUrl);
    }
}