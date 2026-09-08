package com.architect.backend.dto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClonedRepositoryTest {

    @Test
    @DisplayName("close() call කළ විට temporary repository directory එක සහ එහි files ආරක්ෂිතව auto-delete (purge) විය යුතුය")
    void shouldDeleteDirectoryOnClose(@TempDir Path tempBaseDir) throws IOException {
        // Given (Mock cloned directory එකක් සෑදීම)
        Path mockRepoDir = Files.createTempDirectory(tempBaseDir, "test-repo-");
        Path subDir = Files.createDirectories(mockRepoDir.resolve("src"));
        Files.writeString(subDir.resolve("Main.java"), "class Main {}");

        assertThat(Files.exists(mockRepoDir)).isTrue();
        assertThat(Files.exists(subDir.resolve("Main.java"))).isTrue();

        // When (try-with-resources මඟින් AutoCloseable trigger කිරීම)
        try (ClonedRepository clonedRepo = new ClonedRepository(mockRepoDir, "abc1234", "main")) {
            assertThat(clonedRepo.commitHash()).isEqualTo("abc1234");
            assertThat(clonedRepo.branch()).isEqualTo("main");
        }

        // Then (close වූ පසු folder එක සම්පූර්ණයෙන්ම delete වී තිබිය යුතුය)
        assertThat(Files.exists(mockRepoDir)).isFalse();
    }
}
