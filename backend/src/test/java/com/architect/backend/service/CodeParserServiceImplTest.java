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

import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.ExtractedComponent;
import com.architect.backend.dto.SourceFileInfo;
import com.architect.backend.model.CodeComponentType;

class CodeParserServiceImplTest {

    private CodeParserServiceImpl parserService;

    @BeforeEach
    void setUp() {
        parserService = new CodeParserServiceImpl();
    }

    @Test
    @DisplayName("Spring REST Controller එකක් නිවැරදිව parse කර annotations, startLine සහ dependencies හඳුනාගත යුතුය")
    void shouldParseRestControllerSuccessfully(@TempDir Path tempDir) throws IOException {
        // Given (Mock Controller file එකක් සෑදීම)
        Path controllerDir = tempDir.resolve("src/main/java/com/example/controller");
        Files.createDirectories(controllerDir);
        Path controllerFile = controllerDir.resolve("UserController.java");

        String code = """
                package com.example.controller;

                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                    private final UserService userService;

                    public UserController(UserService userService) {
                        this.userService = userService;
                    }
                }
                """;
        Files.writeString(controllerFile, code);

        SourceFileInfo fileInfo = new SourceFileInfo(
                "src/main/java/com/example/controller/UserController.java",
                "UserController.java",
                "java",
                code.length(),
                16
        );

        // When
        ExtractedComponent component = parserService.parseFile(tempDir, fileInfo);

        // Then
        assertThat(component).isNotNull();
        assertThat(component.name()).isEqualTo("UserController");
        assertThat(component.type()).isEqualTo(CodeComponentType.CONTROLLER);
        assertThat(component.startLine()).isEqualTo(7); // "public class UserController {" is line 7
        assertThat(component.dependencies()).contains("UserService");
        assertThat(component.annotations()).anyMatch(a -> a.contains("@RestController"));
    }

    @Test
    @DisplayName("Database Entity එකක් සහ JPA Repository එකක් නිවැරදිව හඳුනාගත යුතුය")
    void shouldParseEntityAndRepository(@TempDir Path tempDir) throws IOException {
        // Given (Mock Entity සහ Repository files සෑදීම)
        Path modelDir = tempDir.resolve("src/main/java/com/example/model");
        Files.createDirectories(modelDir);
        Path entityFile = modelDir.resolve("UserEntity.java");

        String entityCode = """
                package com.example.model;

                import jakarta.persistence.Entity;
                import jakarta.persistence.Id;

                @Entity
                public class UserEntity {
                    @Id
                    private Long id;
                }
                """;
        Files.writeString(entityFile, entityCode);

        Path repoDir = tempDir.resolve("src/main/java/com/example/repository");
        Files.createDirectories(repoDir);
        Path repoFile = repoDir.resolve("UserRepository.java");

        String repoCode = """
                package com.example.repository;

                import org.springframework.data.jpa.repository.JpaRepository;
                import org.springframework.stereotype.Repository;

                @Repository
                public interface UserRepository extends JpaRepository<UserEntity, Long> {
                }
                """;
        Files.writeString(repoFile, repoCode);

        SourceFileInfo entityInfo = new SourceFileInfo(
                "src/main/java/com/example/model/UserEntity.java",
                "UserEntity.java",
                "java",
                entityCode.length(),
                10
        );

        SourceFileInfo repoInfo = new SourceFileInfo(
                "src/main/java/com/example/repository/UserRepository.java",
                "UserRepository.java",
                "java",
                repoCode.length(),
                8
        );

        // When
        CodebaseOutline outline = parserService.extractOutline(tempDir, List.of(entityInfo, repoInfo));

        // Then
        assertThat(outline.totalFiles()).isEqualTo(2);
        assertThat(outline.totalComponents()).isEqualTo(2);

        ExtractedComponent parsedEntity = outline.components().stream()
                .filter(c -> c.name().equals("UserEntity"))
                .findFirst().orElseThrow();
        assertThat(parsedEntity.type()).isEqualTo(CodeComponentType.ENTITY);
        assertThat(parsedEntity.startLine()).isEqualTo(7);

        ExtractedComponent parsedRepo = outline.components().stream()
                .filter(c -> c.name().equals("UserRepository"))
                .findFirst().orElseThrow();
        assertThat(parsedRepo.type()).isEqualTo(CodeComponentType.REPOSITORY);
        assertThat(parsedRepo.startLine()).isEqualTo(7);
    }
}
