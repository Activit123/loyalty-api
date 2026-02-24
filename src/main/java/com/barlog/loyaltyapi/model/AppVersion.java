// java/com/barlog/loyaltyapi/model/AppVersion.java
package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_versions")
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_code", nullable = false)
    private Integer versionCode; // Comparăm int-uri (ex: 2 > 1)

    @Column(name = "version_name", nullable = false)
    private String versionName; // String (ex: "1.2.0")

    @Column(name = "is_critical", nullable = false)
    private boolean isCritical;

    @Column(name = "download_url", nullable = false)
    private String downloadUrl;

    @Column(nullable = false)
    private String platform; // "ANDROID"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}