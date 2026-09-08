package com.architect.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.architect.backend.dto.SourceFileInfo;

class FileFilterServiceImplTest {

    private FileFilterServiceImpl fileFilterService;

    @BeforeEach
    void setUp() {
        fileFilterService = new FileFilterServiceImpl();
    }

    @Test
    @DisplayName("Source code files පමණක් filter විය යුතු අතර non-source, build artifacts සහ media files ඉවත් විය යුතුය")
    void shouldFilterSourceFilesDeterministically(@TempDir Path tempDir) throws IOException {
        // Given (Mock directory structure එකක් සෑදීම)
        // 1. Valid source code files
        Path srcJava = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcJava);
        Files.writeString(srcJava.resolve("App.java"), "public class App {\n    public static void main(String[] args) {}\n}\n");

        Path rootJson = tempDir.resolve("package.json");
        Files.writeString(rootJson, "{\n  \"name\": \"my-app\"\n}\n");

        // 2. Ignored directories & files
        Path nodeModules = tempDir.resolve("node_modules/express");
        Files.createDirectories(nodeModules);
        Files.writeString(nodeModules.resolve("index.js"), "console.log('ignored');");

        Path gitDir = tempDir.resolve(".git/objects");
        Files.createDirectories(gitDir);
        Files.writeString(gitDir.resolve("some-commit"), "git-data");

        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);
        Files.writeString(targetDir.resolve("app.jar"), "binary-jar-content");

        // 3. Ignored extensions (Media, Lockfiles)
        Files.writeString(tempDir.resolve("banner.png"), "fake-png");
        Files.writeString(tempDir.resolve("package-lock.json"), "lock-content"); // wait, lock is in ignored extensions
        Files.writeString(tempDir.resolve("yarn.lock"), "lock-content");

        // When
        List<SourceFileInfo> result = fileFilterService.filterSourceFiles(tempDir);

        // Then
        List<String> relativePaths = result.stream().map(SourceFileInfo::relativePath).toList();

        // Valid files තිබිය යුතුය
        assertThat(relativePaths).contains("src/main/java/com/example/App.java", "package.json");

        // Ignored folders වල files නොතිබිය යුතුය
        assertThat(relativePaths).noneMatch(p -> p.startsWith("node_modules"));
        assertThat(relativePaths).noneMatch(p -> p.startsWith(".git"));
        assertThat(relativePaths).noneMatch(p -> p.startsWith("target"));

        // Ignored extensions නොතිබිය යුතුය
        assertThat(relativePaths).noneMatch(p -> p.endsWith(".png"));
        assertThat(relativePaths).noneMatch(p -> p.endsWith(".lock"));

        // Line count නිවැරදිව ගණනය වී තිබිය යුතුය
        SourceFileInfo appJava = result.stream()
                .filter(f -> f.fileName().equals("App.java"))
                .findFirst()
                .orElseThrow();
        assertThat(appJava.lineCount()).isEqualTo(3);
    }
}
