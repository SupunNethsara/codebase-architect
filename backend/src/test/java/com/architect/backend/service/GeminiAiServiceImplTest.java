package com.architect.backend.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.ExtractedComponent;
import com.architect.backend.model.CodeComponentType;
import com.fasterxml.jackson.databind.ObjectMapper;

class GeminiAiServiceImplTest {

    private GeminiAiServiceImpl geminiAiService;

    @BeforeEach
    void setUp() {
        geminiAiService = new GeminiAiServiceImpl("", "gemini-1.5-flash", "https://generativelanguage.googleapis.com", new ObjectMapper());
    }

    @Test
    @DisplayName("Codebase Outline එකක් ලබා දුන් විට React Flow Nodes සහ Edges නිවැරදිව generate විය යුතුය")
    void shouldSynthesizeGraphFromOutline() {
        // Given (Mock Controller, Service සහ Repository components)
        ExtractedComponent controller = new ExtractedComponent(
                "OrderController",
                CodeComponentType.CONTROLLER,
                "src/main/OrderController.java",
                12,
                "Handles orders",
                List.of("@RestController"),
                List.of("OrderService")
        );

        ExtractedComponent service = new ExtractedComponent(
                "OrderService",
                CodeComponentType.SERVICE,
                "src/main/OrderService.java",
                8,
                "Order business logic",
                List.of("@Service"),
                List.of("OrderRepository")
        );

        ExtractedComponent repo = new ExtractedComponent(
                "OrderRepository",
                CodeComponentType.REPOSITORY,
                "src/main/OrderRepository.java",
                5,
                "Order data access",
                List.of("@Repository"),
                List.of()
        );

        CodebaseOutline outline = new CodebaseOutline(
                3,
                3,
                List.of(controller, service, repo),
                List.of("src/main/OrderController.java", "src/main/OrderService.java", "src/main/OrderRepository.java")
        );

        // When
        ArchitectureGraphDto graph = geminiAiService.synthesizeArchitecture(outline);

        // Then
        assertThat(graph).isNotNull();
        assertThat(graph.nodes()).hasSize(3);

        // Nodes validation
        assertThat(graph.nodes()).anyMatch(n -> n.label().equals("OrderController") && n.type().equals("controllerNode") && n.tier().equals("API"));
        assertThat(graph.nodes()).anyMatch(n -> n.label().equals("OrderService") && n.type().equals("serviceNode") && n.tier().equals("BUSINESS_LOGIC"));
        assertThat(graph.nodes()).anyMatch(n -> n.label().equals("OrderRepository") && n.type().equals("databaseNode") && n.tier().equals("DATA_ACCESS"));

        // Edges validation (OrderController -> OrderService -> OrderRepository)
        assertThat(graph.edges()).hasSize(2);
        assertThat(graph.edges().get(0).source()).isNotBlank();
        assertThat(graph.edges().get(0).target()).isNotBlank();
    }

    @Test
    @DisplayName("හිස් Outline එකක් දුන් විට empty graph එකක් safe ලෙස return විය යුතුය")
    void shouldHandleEmptyOutlineSafely() {
        CodebaseOutline emptyOutline = new CodebaseOutline(0, 0, List.of(), List.of());

        ArchitectureGraphDto graph = geminiAiService.synthesizeArchitecture(emptyOutline);

        assertThat(graph).isNotNull();
        assertThat(graph.nodes()).isEmpty();
        assertThat(graph.edges()).isEmpty();
    }
}
