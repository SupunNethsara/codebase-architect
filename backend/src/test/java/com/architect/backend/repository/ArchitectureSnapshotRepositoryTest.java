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

import com.architect.backend.dto.ArchitectureEdgeDto;
import com.architect.backend.dto.ArchitectureNodeDto;
import com.architect.backend.model.ArchitectureSnapshotEntity;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;

@SpringBootTest
@Transactional
class ArchitectureSnapshotRepositoryTest {

    @Autowired
    private ArchitectureSnapshotRepository snapshotRepository;

    @Autowired
    private RepositoryEntityRepository repositoryRepository;

    @Test
    @DisplayName("ArchitectureSnapshot එකක් JSONB nodes සහ edges දත්ත සමඟ සාර්ථකව save සහ query කළ හැකි විය යුතුය")
    void shouldSaveAndRetrieveSnapshotWithJsonbData() {
        // Given (පළමුව RepositoryEntity එකක් save කරගැනීම)
        RepositoryEntity repo = repositoryRepository.save(RepositoryEntity.builder()
                .userId(UUID.randomUUID())
                .repoUrl("https://github.com/example/architecture-test")
                .status(RepositoryStatus.COMPLETED)
                .build());

        ArchitectureNodeDto node1 = new ArchitectureNodeDto(
                "node-1", "controllerNode", "UserController", "UserController.java", 10, "User API", "API"
        );
        ArchitectureNodeDto node2 = new ArchitectureNodeDto(
                "node-2", "serviceNode", "UserService", "UserService.java", 8, "User logic", "BUSINESS_LOGIC"
        );
        ArchitectureEdgeDto edge1 = new ArchitectureEdgeDto(
                "e-1-2", "node-1", "node-2", "calls", true
        );

        ArchitectureSnapshotEntity snapshot = ArchitectureSnapshotEntity.builder()
                .repository(repo)
                .commitHash("9f83ac45b8e9120349a")
                .architecturePattern("Layered Architecture")
                .summary("Clean 3-tier Spring Boot architecture")
                .nodesData(List.of(node1, node2))
                .edgesData(List.of(edge1))
                .build();

        // When
        ArchitectureSnapshotEntity saved = snapshotRepository.save(snapshot);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<ArchitectureSnapshotEntity> retrieved = snapshotRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repo.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCommitHash()).isEqualTo("9f83ac45b8e9120349a");
        assertThat(retrieved.get().getArchitecturePattern()).isEqualTo("Layered Architecture");

        // JSONB දත්ත පරීක්ෂා කිරීම
        assertThat(retrieved.get().getNodesData()).hasSize(2);
        assertThat(retrieved.get().getNodesData().get(0).label()).isEqualTo("UserController");
        assertThat(retrieved.get().getEdgesData()).hasSize(1);
        assertThat(retrieved.get().getEdgesData().get(0).source()).isEqualTo("node-1");
    }
}
