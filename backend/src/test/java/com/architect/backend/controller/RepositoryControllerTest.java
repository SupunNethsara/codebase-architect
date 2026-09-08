package com.architect.backend.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;
import com.architect.backend.exception.GlobalExceptionHandler;
import com.architect.backend.exception.ResourceNotFoundException;
import com.architect.backend.model.RepositoryStatus;
import com.architect.backend.service.RepositoryService;

/**
 * RepositoryController සඳහා ලියන ලද REST API Unit Tests.
 * 
 * MockMvc Standalone Setup:
 * 1. Spring Boot server එකක් start නොකර MockMvc මඟින් HTTP Requests simulate කරයි.
 * 2. RepositoryService එක mock කර Controller layer එකේ routing, JSON parsing සහ status codes test කරයි.
 * 3. GlobalExceptionHandler එක attach කර error responses (400, 404) නිවැරදිදැයි පරීක්ෂා කරයි.
 */
@ExtendWith(MockitoExtension.class)
class RepositoryControllerTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private com.architect.backend.service.CodebaseAnalysisService analysisService;

    @InjectMocks
    private RepositoryController repositoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockMvc standalone setup: Controller සහ ExceptionHandler එක සම්බන්ධ කිරීම
        mockMvc = MockMvcBuilders.standaloneSetup(repositoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/repositories - සාර්ථක request එකකදී 201 CREATED ලැබිය යුතුය")
    void shouldCreateRepositoryAndReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RepositoryResponse response = RepositoryResponse.builder()
                .id(id)
                .userId(userId)
                .repoUrl("https://github.com/spring-projects/spring-boot")
                .defaultBranch("main")
                .status(RepositoryStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        when(repositoryService.createRepository(any(CreateRepositoryRequest.class))).thenReturn(response);

        // Java 21 Text Block භාවිතා කර JSON payload එක සකස් කිරීම
        String requestJson = """
                {
                    "userId": "%s",
                    "repoUrl": "https://github.com/spring-projects/spring-boot",
                    "defaultBranch": "main"
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.repoUrl").value("https://github.com/spring-projects/spring-boot"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/repositories - අවලංගු GitHub URL එකක් ලබාදුන් විට 400 BAD REQUEST ලැබිය යුතුය")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        UUID userId = UUID.randomUUID();

        String invalidRequestJson = """
                {
                    "userId": "%s",
                    "repoUrl": "invalid-url",
                    "defaultBranch": "main"
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.repoUrl").exists());
    }

    @Test
    @DisplayName("GET /api/v1/repositories/{id} - පවතින ID එකකදී 200 OK ලැබිය යුතුය")
    void shouldGetRepositoryByIdAndReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RepositoryResponse response = RepositoryResponse.builder()
                .id(id)
                .userId(userId)
                .repoUrl("https://github.com/google/guava")
                .defaultBranch("master")
                .status(RepositoryStatus.COMPLETED)
                .build();

        when(repositoryService.getRepositoryById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/repositories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.repoUrl").value("https://github.com/google/guava"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/v1/repositories/{id} - හමුනොවන ID එකකදී 404 NOT FOUND ලැබිය යුතුය")
    void shouldReturn404WhenRepositoryNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(repositoryService.getRepositoryById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Repository not found with id: " + nonExistentId));

        mockMvc.perform(get("/api/v1/repositories/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Repository not found with id: " + nonExistentId));
    }

    @Test
    @DisplayName("GET /api/v1/repositories?userId={userId} - 200 OK සහ repositories list එක ලැබිය යුතුය")
    void shouldGetRepositoriesByUserIdAndReturn200() throws Exception {
        UUID userId = UUID.randomUUID();

        RepositoryResponse repo1 = RepositoryResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .repoUrl("https://github.com/user/repo-1")
                .status(RepositoryStatus.PENDING)
                .build();

        RepositoryResponse repo2 = RepositoryResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .repoUrl("https://github.com/user/repo-2")
                .status(RepositoryStatus.COMPLETED)
                .build();

        when(repositoryService.getRepositoriesByUserId(userId)).thenReturn(List.of(repo1, repo2));

        mockMvc.perform(get("/api/v1/repositories")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].repoUrl").value("https://github.com/user/repo-1"))
                .andExpect(jsonPath("$[1].repoUrl").value("https://github.com/user/repo-2"));
    }

    @Test
    @DisplayName("POST /api/v1/repositories/{id}/analyze - 202 ACCEPTED සහ background parsing message ලැබිය යුතුය")
    void shouldTriggerAnalysisAndReturn202() throws Exception {
        UUID repoId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/repositories/{id}/analyze", repoId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.repositoryId").value(repoId.toString()))
                .andExpect(jsonPath("$.status").value("PARSING"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/repositories/{id}/snapshot - Snapshot එකක් ඇති විට 200 OK සහ graph data ලැබිය යුතුය")
    void shouldGetSnapshotAndReturn200() throws Exception {
        UUID repoId = UUID.randomUUID();
        com.architect.backend.dto.ArchitectureGraphDto graph = new com.architect.backend.dto.ArchitectureGraphDto(
                "Overview",
                "Layered",
                List.of(new com.architect.backend.dto.ArchitectureNodeDto("n1", "controllerNode", "Ctrl", "Ctrl.java", 1, "desc", "API")),
                List.of()
        );

        when(analysisService.getLatestSnapshot(repoId)).thenReturn(java.util.Optional.of(graph));

        mockMvc.perform(get("/api/v1/repositories/{id}/snapshot", repoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Overview"))
                .andExpect(jsonPath("$.architecturePattern").value("Layered"))
                .andExpect(jsonPath("$.nodes[0].id").value("n1"));
    }

    @Test
    @DisplayName("GET /api/v1/repositories/{id}/snapshot - Snapshot එකක් නොමැති විට 404 NOT_FOUND ලැබිය යුතුය")
    void shouldReturn404WhenSnapshotNotFound() throws Exception {
        UUID repoId = UUID.randomUUID();
        when(analysisService.getLatestSnapshot(repoId)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/repositories/{id}/snapshot", repoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
