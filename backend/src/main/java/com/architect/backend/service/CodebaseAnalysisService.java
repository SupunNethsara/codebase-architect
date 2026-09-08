package com.architect.backend.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.model.ArchitectureSnapshotEntity;

/**
 * Repository Ingestion, File Filtering, AST Parsing, සහ AI Graph Synthesis
 * එකිනෙක සම්බන්ධ කරමින් ක්‍රියාත්මක කරන ප්‍රධාන Orchestrator Service Interface එක.
 */
public interface CodebaseAnalysisService {

    /**
     * අදාළ repository ID එක සඳහා Asynchronous Background Analysis Pipeline එකක් ආරම්භ කරයි.
     *
     * @param repositoryId විශ්ලේෂණය කළ යුතු repository එකේ UUID එක
     * @return CompletableFuture සමඟ නිමවූ ArchitectureSnapshotEntity එක
     */
    CompletableFuture<ArchitectureSnapshotEntity> triggerAnalysis(UUID repositoryId);

    /**
     * Repository එක සඳහා සාදන ලද නවතම Architecture Snapshot එක (Graph එක) ලබාගැනීම.
     */
    Optional<ArchitectureGraphDto> getLatestSnapshot(UUID repositoryId);
}
