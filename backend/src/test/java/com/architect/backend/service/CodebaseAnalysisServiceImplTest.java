package com.architect.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architect.backend.dto.ArchitectureEdgeDto;
import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.dto.ArchitectureNodeDto;
import com.architect.backend.dto.ClonedRepository;
import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.SourceFileInfo;
import com.architect.backend.exception.GitOperationException;
import com.architect.backend.model.ArchitectureSnapshotEntity;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;
import com.architect.backend.repository.ArchitectureSnapshotRepository;
import com.architect.backend.repository.RepositoryEntityRepository;

@ExtendWith(MockitoExtension.class)
class CodebaseAnalysisServiceImplTest {

    @Mock
    private RepositoryEntityRepository repositoryRepository;

    @Mock
    private GitCloneService gitCloneService;

    @Mock
    private FileFilterService fileFilterService;

    @Mock
    private CodeParserService codeParserService;

    @Mock
    private GeminiAiService geminiAiService;

    @Mock
    private ArchitectureSnapshotRepository snapshotRepository;

    @InjectMocks
    private CodebaseAnalysisServiceImpl analysisService;

    private UUID repoId;
    private RepositoryEntity repositoryEntity;

    @BeforeEach
    void setUp() {
        repoId = UUID.randomUUID();
        repositoryEntity = RepositoryEntity.builder()
                .id(repoId)
                .userId(UUID.randomUUID())
                .repoUrl("https://github.com/test/repo")
                .defaultBranch("main")
                .status(RepositoryStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("TriggerAnalysis - Complete pipeline එක සාර්ථකව run වී status එක COMPLETED විය යුතුය")
    void shouldExecuteAnalysisPipelineSuccessfully(@TempDir Path tempDir) throws IOException {
        // Given
        ClonedRepository clonedRepo = new ClonedRepository(tempDir, "abc1234", "main");
        List<SourceFileInfo> sourceFiles = List.of(new SourceFileInfo("App.java", "App.java", "java", 100, 10));
        CodebaseOutline outline = new CodebaseOutline(1, 1, List.of(), List.of("App.java"));
        ArchitectureGraphDto graph = new ArchitectureGraphDto(
                "Summary",
                "MVC",
                List.of(new ArchitectureNodeDto("n1", "controllerNode", "App", "App.java", 1, "App", "API")),
                List.of(new ArchitectureEdgeDto("e1", "n1", "n1", "self", false))
        );

        when(repositoryRepository.findById(repoId)).thenReturn(Optional.of(repositoryEntity));
        when(gitCloneService.cloneRepository(eq("https://github.com/test/repo"), eq("main"))).thenReturn(clonedRepo);
        when(fileFilterService.filterSourceFiles(tempDir)).thenReturn(sourceFiles);
        when(codeParserService.extractOutline(tempDir, sourceFiles)).thenReturn(outline);
        when(geminiAiService.synthesizeArchitecture(outline)).thenReturn(graph);
        when(snapshotRepository.save(any(ArchitectureSnapshotEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CompletableFuture<ArchitectureSnapshotEntity> future = analysisService.triggerAnalysis(repoId);
        ArchitectureSnapshotEntity result = future.join();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCommitHash()).isEqualTo("abc1234");
        assertThat(result.getArchitecturePattern()).isEqualTo("MVC");
        assertThat(repositoryEntity.getStatus()).isEqualTo(RepositoryStatus.COMPLETED);
        verify(snapshotRepository).save(any(ArchitectureSnapshotEntity.class));
    }

    @Test
    @DisplayName("TriggerAnalysis - Pipeline එකේ දෝෂයක් ආ විට status එක FAILED විය යුතුය")
    void shouldHandlePipelineFailureGracefully() {
        // Given
        when(repositoryRepository.findById(repoId)).thenReturn(Optional.of(repositoryEntity));
        when(gitCloneService.cloneRepository(any(), any())).thenThrow(new GitOperationException("Clone failed"));

        // When
        CompletableFuture<ArchitectureSnapshotEntity> future = analysisService.triggerAnalysis(repoId);

        // Then
        assertThat(future).isCompletedExceptionally();
        assertThat(repositoryEntity.getStatus()).isEqualTo(RepositoryStatus.FAILED);
    }
}
