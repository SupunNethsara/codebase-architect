package com.architect.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;
import com.architect.backend.exception.ResourceNotFoundException;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;
import com.architect.backend.repository.RepositoryEntityRepository;

/**
 * RepositoryServiceImpl සඳහා ලියන ලද Unit Tests (Mockito භාවිතයෙන්).
 * 
 * වැදගත් Mockito සංකල්ප:
 * 1. @ExtendWith(MockitoExtension.class): Spring Boot context එකක් හෝ Database එකක් load නොකර pure mock engine එක පමණක් ක්‍රියාත්මක කරයි (ඉතා වේගවත්ය).
 * 2. @Mock: ඇත්ත Database එක වෙනුවට බොරු (Mock) Repository එකක් සාදයි.
 * 3. @InjectMocks: අපේ RepositoryServiceImpl එක create කර, ඊට උඩින් සාදාගත් @Mock repository එක inject කරයි.
 * 4. when(...).thenReturn(...): Mock object එකට method call එකක් ආ විට දිය යුතු පිළිතුර කලින්ම ලෑස්ති කිරීම (Stubbing).
 * 5. verify(...): අදාළ method එක අප බලාපොරොත්තු වූ වාර ගණන call වුණාදැයි තහවුරු කිරීම.
 */
@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryEntityRepository repository;

    @InjectMocks
    private RepositoryServiceImpl service;

    @Test
    @DisplayName("CreateRepository සාර්ථකව ක්‍රියාත්මක වී PENDING status එක සහිතව save විය යුතුය")
    void shouldCreateRepositorySuccessfully() {
        // Given (Mock data සහ හැසිරීම් සකස් කිරීම)
        UUID userId = UUID.randomUUID();
        UUID generatedId = UUID.randomUUID();

        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .userId(userId)
                .repoUrl("https://github.com/spring-projects/spring-boot")
                .defaultBranch("main")
                .build();

        RepositoryEntity savedEntity = RepositoryEntity.builder()
                .id(generatedId)
                .userId(userId)
                .repoUrl(request.getRepoUrl())
                .defaultBranch("main")
                .status(RepositoryStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        // Repository එකේ save() method එක call වූ විට savedEntity එක return කරන්නැයි Mockito වෙත පැවසීම
        when(repository.save(any(RepositoryEntity.class))).thenReturn(savedEntity);

        // When (Service method එක ක්‍රියාත්මක කිරීම)
        RepositoryResponse response = service.createRepository(request);

        // Then (ප්‍රතිඵලය පරීක්ෂා කිරීම)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(generatedId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRepoUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
        assertThat(response.getStatus()).isEqualTo(RepositoryStatus.PENDING);

        // Verify: repository.save() method එක හරියටම 1 වතාවක් call වූ බව තහවුරු කිරීම
        verify(repository, times(1)).save(any(RepositoryEntity.class));
    }

    @Test
    @DisplayName("පවතින ID එකක් ලබාදුන් විට නිවැරදි RepositoryResponse එක ලබාගත හැකි විය යුතුය")
    void shouldGetRepositoryByIdSuccessfully() {
        // Given
        UUID repoId = UUID.randomUUID();
        RepositoryEntity entity = RepositoryEntity.builder()
                .id(repoId)
                .userId(UUID.randomUUID())
                .repoUrl("https://github.com/google/guava")
                .defaultBranch("master")
                .status(RepositoryStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();

        when(repository.findById(repoId)).thenReturn(Optional.of(entity));

        // When
        RepositoryResponse response = service.getRepositoryById(repoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(repoId);
        assertThat(response.getRepoUrl()).isEqualTo("https://github.com/google/guava");
        assertThat(response.getStatus()).isEqualTo(RepositoryStatus.COMPLETED);

        verify(repository, times(1)).findById(repoId);
    }

    @Test
    @DisplayName("නොපවතින ID එකක් ලබාදුන් විට ResourceNotFoundException throw විය යුතුය")
    void shouldThrowResourceNotFoundExceptionWhenIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then (Exception එකක් throw වෙනවාදැයි පරීක්ෂා කිරීම)
        assertThatThrownBy(() -> service.getRepositoryById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found with id: " + nonExistentId);

        verify(repository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("UserId එක මඟින් repositories list එකක් ලබාගත හැකි විය යුතුය")
    void shouldGetRepositoriesByUserIdSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        RepositoryEntity repo1 = RepositoryEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .repoUrl("https://github.com/user/repo-1")
                .status(RepositoryStatus.PENDING)
                .build();

        RepositoryEntity repo2 = RepositoryEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .repoUrl("https://github.com/user/repo-2")
                .status(RepositoryStatus.COMPLETED)
                .build();

        when(repository.findByUserId(userId)).thenReturn(List.of(repo1, repo2));

        // When
        List<RepositoryResponse> results = service.getRepositoriesByUserId(userId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getRepoUrl()).isEqualTo("https://github.com/user/repo-1");
        assertThat(results.get(1).getRepoUrl()).isEqualTo("https://github.com/user/repo-2");

        verify(repository, times(1)).findByUserId(userId);
    }
}
