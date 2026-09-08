package com.architect.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.dto.ClonedRepository;
import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.SourceFileInfo;
import com.architect.backend.exception.ResourceNotFoundException;
import com.architect.backend.model.ArchitectureSnapshotEntity;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;
import com.architect.backend.repository.ArchitectureSnapshotRepository;
import com.architect.backend.repository.RepositoryEntityRepository;

/**
 * Repository Analysis Pipeline එක Asynchronous ලෙස Virtual Threads මත ධාවනය කරන Master Implementation එක.
 */
@Service
public class CodebaseAnalysisServiceImpl implements CodebaseAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CodebaseAnalysisServiceImpl.class);

    private final RepositoryEntityRepository repositoryRepository;
    private final GitCloneService gitCloneService;
    private final FileFilterService fileFilterService;
    private final CodeParserService codeParserService;
    private final GeminiAiService geminiAiService;
    private final ArchitectureSnapshotRepository snapshotRepository;

    public CodebaseAnalysisServiceImpl(
            RepositoryEntityRepository repositoryRepository,
            GitCloneService gitCloneService,
            FileFilterService fileFilterService,
            CodeParserService codeParserService,
            GeminiAiService geminiAiService,
            ArchitectureSnapshotRepository snapshotRepository
    ) {
        this.repositoryRepository = repositoryRepository;
        this.gitCloneService = gitCloneService;
        this.fileFilterService = fileFilterService;
        this.codeParserService = codeParserService;
        this.geminiAiService = geminiAiService;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Async("virtualThreadExecutor")
    public CompletableFuture<ArchitectureSnapshotEntity> triggerAnalysis(UUID repositoryId) {
        log.info("Triggering asynchronous analysis pipeline on Virtual Thread for repository ID: {}", repositoryId);

        RepositoryEntity repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with ID: " + repositoryId));

        try {
            // 1. Status එක PARSING ලෙස යාවත්කාලීන කිරීම
            repo.setStatus(RepositoryStatus.PARSING);
            repositoryRepository.save(repo);

            // 2. Ephemeral Shallow Clone (try-with-resources මඟින් අවසානයේ auto-delete වේ)
            try (ClonedRepository cloned = gitCloneService.cloneRepository(repo.getRepoUrl(), repo.getDefaultBranch())) {
                log.info("Step 1/4: Shallow cloned repo at commit: {}", cloned.commitHash());

                // 3. Deterministic File Filtering
                List<SourceFileInfo> sourceFiles = fileFilterService.filterSourceFiles(cloned.directory());
                log.info("Step 2/4: Filtered {} clean source code files", sourceFiles.size());

                // 4. AST Blueprint & Outline Extraction
                CodebaseOutline outline = codeParserService.extractOutline(cloned.directory(), sourceFiles);
                log.info("Step 3/4: Extracted AST outline with {} components", outline.totalComponents());

                // 5. Gemini AI Synthesis
                ArchitectureGraphDto graph = geminiAiService.synthesizeArchitecture(outline);
                log.info("Step 4/4: Synthesized architecture graph with {} nodes and {} edges",
                        graph.nodes().size(), graph.edges().size());

                // 6. Architecture Snapshot එක Database එකට Save කිරීම
                ArchitectureSnapshotEntity snapshot = ArchitectureSnapshotEntity.builder()
                        .repository(repo)
                        .commitHash(cloned.commitHash())
                        .architecturePattern(graph.architecturePattern())
                        .summary(graph.summary())
                        .nodesData(graph.nodes())
                        .edgesData(graph.edges())
                        .build();

                ArchitectureSnapshotEntity savedSnapshot = snapshotRepository.save(snapshot);

                // 7. Status එක COMPLETED ලෙස යාවත්කාලීන කිරීම
                repo.setStatus(RepositoryStatus.COMPLETED);
                repositoryRepository.save(repo);

                log.info("Analysis pipeline successfully completed for repository ID: {}", repositoryId);
                return CompletableFuture.completedFuture(savedSnapshot);
            }

        } catch (Exception e) {
            log.error("Pipeline failure for repository ID {}: {}", repositoryId, e.getMessage(), e);
            repo.setStatus(RepositoryStatus.FAILED);
            repositoryRepository.save(repo);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArchitectureGraphDto> getLatestSnapshot(UUID repositoryId) {
        return snapshotRepository.findFirstByRepositoryIdOrderByCreatedAtDesc(repositoryId)
                .map(snapshot -> new ArchitectureGraphDto(
                        snapshot.getSummary(),
                        snapshot.getArchitecturePattern(),
                        snapshot.getNodesData(),
                        snapshot.getEdgesData()
                ));
    }
}
