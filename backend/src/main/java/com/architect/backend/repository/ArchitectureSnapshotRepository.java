package com.architect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.architect.backend.model.ArchitectureSnapshotEntity;

/**
 * ArchitectureSnapshotEntity සඳහා Spring Data JPA Repository interface එක.
 */
@Repository
public interface ArchitectureSnapshotRepository extends JpaRepository<ArchitectureSnapshotEntity, UUID> {

    /**
     * අදාළ repository ID එකට අයත් සියලුම Snapshots (ඉතිහාසය) ලබාගැනීම.
     */
    List<ArchitectureSnapshotEntity> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

    /**
     * අදාළ repository එකේ අලුත්ම Snapshot එක ලබාගැනීම.
     */
    Optional<ArchitectureSnapshotEntity> findFirstByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

    /**
     * Commit hash එක අනුව snapshot එකක් සොයාගැනීම.
     */
    Optional<ArchitectureSnapshotEntity> findByRepositoryIdAndCommitHash(UUID repositoryId, String commitHash);
}
