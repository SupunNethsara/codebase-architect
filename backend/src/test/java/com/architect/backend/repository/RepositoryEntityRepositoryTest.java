package com.architect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;

/**
 * RepositoryEntityRepository සඳහා ලියන ලද Unit / Integration Tests.
 * @Transactional: Test එක අවසානයේ Database එකට ඇතුළු කළ test records auto-rollback වේ (Database එක cleanව තබාගැනීම).
 */
@SpringBootTest
@Transactional
class RepositoryEntityRepositoryTest {

    @Autowired
    private RepositoryEntityRepository repository;

    @Test
    @DisplayName("Repository record එකක් සාර්ථකව save විය යුතු අතර, UUID සහ CreatedAt auto-generate විය යුතුය")
    void shouldSaveRepositoryEntitySuccessfully() {
        // Given (Test data සකස් කිරීම)
        UUID userId = UUID.randomUUID();
        RepositoryEntity entity = RepositoryEntity.builder()
                .userId(userId)
                .repoUrl("https://github.com/spring-projects/spring-boot")
                .defaultBranch("main")
                .status(RepositoryStatus.PENDING)
                .build();

        // When (Repository එක හරහා Database එකට save කිරීම)
        RepositoryEntity saved = repository.save(entity);

        // Then (ප්‍රතිඵලය පරීක්ෂා කිරීම - Assertions)
        assertThat(saved.getId()).isNotNull(); // UUID එක auto-generate විය යුතුයි
        assertThat(saved.getCreatedAt()).isNotNull(); // @CreationTimestamp auto-fill විය යුතුයි
        assertThat(saved.getRepoUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
        assertThat(saved.getStatus()).isEqualTo(RepositoryStatus.PENDING);
        assertThat(saved.getDefaultBranch()).isEqualTo("main");
    }

    @Test
    @DisplayName("GitHub repoUrl එක මඟින් repository එකක් සොයාගත හැකි විය යුතුය")
    void shouldFindByRepoUrl() {
        // Given
        String testUrl = "https://github.com/google/guava";
        RepositoryEntity entity = RepositoryEntity.builder()
                .userId(UUID.randomUUID())
                .repoUrl(testUrl)
                .status(RepositoryStatus.PARSING)
                .build();
        repository.save(entity);

        // When
        Optional<RepositoryEntity> found = repository.findByRepoUrl(testUrl);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRepoUrl()).isEqualTo(testUrl);
        assertThat(found.get().getStatus()).isEqualTo(RepositoryStatus.PARSING);
    }

    @Test
    @DisplayName("Status එක අනුව (උදා: PENDING) repositories filter කර ලබාගත හැකි විය යුතුය")
    void shouldFindByStatus() {
        // Given
        UUID user = UUID.randomUUID();
        RepositoryEntity repo1 = RepositoryEntity.builder()
                .userId(user)
                .repoUrl("https://github.com/user/repo-pending-1")
                .status(RepositoryStatus.PENDING)
                .build();
        RepositoryEntity repo2 = RepositoryEntity.builder()
                .userId(user)
                .repoUrl("https://github.com/user/repo-completed-1")
                .status(RepositoryStatus.COMPLETED)
                .build();

        repository.save(repo1);
        repository.save(repo2);

        // When
        List<RepositoryEntity> pendingRepos = repository.findByStatus(RepositoryStatus.PENDING);

        // Then
        assertThat(pendingRepos).isNotEmpty();
        assertThat(pendingRepos).allMatch(r -> r.getStatus() == RepositoryStatus.PENDING);
    }
}
