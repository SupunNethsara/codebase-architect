package com.architect.backend.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client (Frontend) එකෙන් අලුත් GitHub Repository එකක් analyze කිරීමට එවන Request Payload එක.
 * 
 * ඇයි Entity එක කෙලින්ම use නොකර DTO එකක් use කරන්නේ?
 * 1. Security: Client එකෙන් id, createdAt වැනි internal fields වෙනස් කිරීම වැළැක්වීම.
 * 2. Validation: Data Database එකට යන්න කලින්ම format එක නිවැරදිදැයි පරීක්ෂා කිරීම.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRepositoryRequest {

    /**
     * මෙම Repository එක submit කරන User ගේ UUID එක.
     * @NotNull: null අගයක් එවිය නොහැක.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * Analyze කළ යුතු GitHub Repository URL එක.
     * @NotBlank: හිස් අගයක් (empty හෝ whitespace) විය නොහැක.
     * @Pattern: නිවැරදි GitHub URL එකක්දැයි Regex මඟින් තහවුරු කරයි.
     * උදා: https://github.com/owner/repo
     */
    @NotBlank(message = "Repository URL is required")
    @Pattern(
        regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$",
        message = "Invalid GitHub repository URL format. Example: https://github.com/owner/repo"
    )
    private String repoUrl;

    /**
     * Clone කළ යුතු default branch එක (Default: main).
     */
    @Builder.Default
    private String defaultBranch = "main";
}
