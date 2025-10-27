package com.barlog.loyaltyapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column // Am șters (nullable = false)
    private String password;

    @Column(name = "coins")
    @Builder.Default
    private Integer coins = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    // ADAUGĂ ACEST CÂMP NOU
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Long experience = 0L;

    @Column(name = "xp_rate", nullable = false)
    private Double xpRate = 1.0;

    @Column(length = 50, unique = true)
    private String nickname;

    @ManyToOne(fetch = FetchType.EAGER) // <-- ADAUGĂ ACEASTA
    @JoinColumn(name = "race_id")
    private Race race;

    @ManyToOne(fetch = FetchType.EAGER) // <-- ADAUGĂ ACEASTA
    @JoinColumn(name = "class_type_id")
    private ClassType classType;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // câmpul unic de identificare este emailul în cazul nostru
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}