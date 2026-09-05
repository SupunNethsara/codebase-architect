package com.architect.backend.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System එක වෙත ලබාදෙන GitHub Repositories පිළිබඳ තොරතුරු තැන්පත් කරන Database Entity එක.
 * Database Table: "repositories"
 */
@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    /**
     * Primary Key එක ලෙස UUID භාවිතා කෙරේ (Security & Scalability Best Practice).
     * Hibernate මඟින් අලුත් record එකක් create වෙද්දී UUID එකක් auto-generate කරනු ලබයි.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * මෙම Repository එක අයත් User ගේ Unique Identifier එක.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Analyze කළ යුතු GitHub Repository URL එක (උදා: https://github.com/facebook/react).
     */
    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    /**
     * Clone කළ යුතු default git branch එක. Default අගය "main" වේ.
     */
    @Builder.Default
    @Column(name = "default_branch", length = 64)
    private String defaultBranch = "main";

    /**
     * Repository එකේ වත්මන් status එක (PENDING, PARSING, COMPLETED, FAILED).
     * EnumType.STRING: Database එකේ integer id එකක් වෙනුවට නමම ("PENDING") save වේ.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RepositoryStatus status;

    /**
     * Record එක Database එකට ඇතුලත් කළ දිනය සහ වේලාව (Auditing).
     * @CreationTimestamp: Save වන මොහොතේම Hibernate මඟින් timestamp එක auto-fill කරයි.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Entity එක persist වීමට පෙර createdAt set කර නොමැති නම් වත්මන් වේලාව ලබාදීම (JPA Lifecycle Hook).
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
