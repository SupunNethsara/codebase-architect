package com.architect.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.architect.backend.dto.ClonedRepository;
import com.architect.backend.exception.GitOperationException;

class GitCloneServiceImplTest {

    private GitCloneServiceImpl gitCloneService;

    @BeforeEach
    void setUp() {
        gitCloneService = new GitCloneServiceImpl();
    }

    @Test
    @DisplayName("හිස් හෝ null URL එකක් ලබා දුන් විට GitOperationException විසි විය යුතුය")
    void shouldThrowExceptionForInvalidUrl() {
        assertThatThrownBy(() -> gitCloneService.cloneRepository(null, "main"))
                .isInstanceOf(GitOperationException.class)
                .hasMessageContaining("Repository URL cannot be null or blank");

        assertThatThrownBy(() -> gitCloneService.cloneRepository("   ", "main"))
                .isInstanceOf(GitOperationException.class)
                .hasMessageContaining("Repository URL cannot be null or blank");
    }

    @Test
    @DisplayName("Local git repo එකක් සාර්ථකව shallow clone කර commit hash එක ලබාගත හැකි විය යුතුය")
    void shouldCloneRepositorySuccessfully(@TempDir Path tempSourceDir) throws GitAPIException, IOException {
        // Given (පරීක්ෂාව සඳහා තාවකාලික local git repository එකක් සෑදීම)
        try (Git git = Git.init().setDirectory(tempSourceDir.toFile()).call()) {
            Path sampleFile = tempSourceDir.resolve("README.md");
            Files.writeString(sampleFile, "# Test Repo");

            git.add().addFilepattern("README.md").call();
            git.commit().setMessage("Initial commit").setAuthor("Test", "test@example.com").call();
        }

        String repoUri = tempSourceDir.toUri().toString();

        // When (Shallow clone service එක call කිරීම)
        try (ClonedRepository cloned = gitCloneService.cloneRepository(repoUri, null)) {
            // Then
            assertThat(cloned.directory()).isNotNull();
            assertThat(Files.exists(cloned.directory())).isTrue();
            assertThat(Files.exists(cloned.directory().resolve("README.md"))).isTrue();
            assertThat(cloned.commitHash()).isNotEmpty().isNotEqualTo("HEAD");
        }
    }
}
