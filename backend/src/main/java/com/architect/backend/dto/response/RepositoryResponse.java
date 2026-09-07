package com.architect.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Backend API එකෙන් Client (Frontend) වෙත ආපසු යවන Repository Data Response එක.
 * 
 * ඇයි Entity එක කෙලින්ම return නොකර Response DTO එකක් use කරන්නේ?
 * 1. Decoupling: Database schema එක වෙනස් උනත් frontend එකට යන API contract එක කැඩෙන්නේ නැත.
 * 2. Sensitive fields hide කිරීම: අනවශ්‍ය database columns client එකෙන් සඟවා තැබිය හැක.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryResponse {

    private UUID id;
    private UUID userId;
    private String repoUrl;
    private String defaultBranch;
    private RepositoryStatus status;
    private OffsetDateTime createdAt;

    /**
     * Database Entity එකක් පහසුවෙන් Response DTO එකකට convert කරගැනීමට Static Factory Method එකක් (Mapping helper).
     */
    public static RepositoryResponse fromEntity(RepositoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return RepositoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .repoUrl(entity.getRepoUrl())
                .defaultBranch(entity.getDefaultBranch())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
