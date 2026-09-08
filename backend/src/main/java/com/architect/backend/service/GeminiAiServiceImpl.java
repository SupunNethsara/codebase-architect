package com.architect.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.architect.backend.dto.ArchitectureEdgeDto;
import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.dto.ArchitectureNodeDto;
import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.ExtractedComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Google Gemini 1.5 API Integration Service (FR-4 & Section 5 Specification).
 * Strict JSON Schema enforcement මඟින් React Flow Graph state එක නිපදවයි.
 */
@Service
public class GeminiAiServiceImpl implements GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiServiceImpl.class);

    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiAiServiceImpl(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.model:gemini-1.5-flash}") String model,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}") String apiUrl,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public ArchitectureGraphDto synthesizeArchitecture(CodebaseOutline outline) {
        if (outline == null || outline.components() == null || outline.components().isEmpty()) {
            return new ArchitectureGraphDto(
                    "Empty codebase with no identifiable architectural components.",
                    "Unknown",
                    List.of(),
                    List.of()
            );
        }

        // Gemini API Key එකක් සකසා ඇත්නම් Real Gemini API එක Call කිරීම
        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-gemini-api-key-here")) {
            try {
                return callGeminiApi(outline);
            } catch (Exception e) {
                log.warn("Gemini API call failed ({}). Falling back to deterministic AST graph synthesis.", e.getMessage());
            }
        } else {
            log.info("No Gemini API key provided. Using deterministic AST graph generator.");
        }

        // Fallback: Offline / Local Testing සඳහා AST දත්ත මඟින් 100% Valid Graph එකක් සෑදීම
        return synthesizeDeterministicGraph(outline);
    }

    private ArchitectureGraphDto callGeminiApi(CodebaseOutline outline) throws Exception {
        String prompt = buildPrompt(outline);
        String endpoint = String.format("%s/models/%s:generateContent?key=%s", apiUrl, model, apiKey);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.2
                )
        );

        String responseJson = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode rootNode = objectMapper.readTree(responseJson);
        String textOutput = rootNode.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        return objectMapper.readValue(textOutput, ArchitectureGraphDto.class);
    }

    private String buildPrompt(CodebaseOutline outline) throws Exception {
        String outlineJson = objectMapper.writeValueAsString(outline);

        return """
                You are a Principal Software Architect. Analyze the following Codebase Outline and generate an interactive React Flow Architecture Graph.
                Output ONLY valid JSON matching this exact schema:
                {
                  "summary": "Executive architectural overview of the system",
                  "architecturePattern": "Layered / Microservices / Event-Driven / Modular Monolith",
                  "nodes": [
                    {
                      "id": "node-1",
                      "type": "controllerNode" | "serviceNode" | "databaseNode" | "gatewayNode",
                      "label": "ComponentName",
                      "filePath": "path/to/File.java",
                      "startLine": 1,
                      "description": "Brief description of role",
                      "tier": "API" | "BUSINESS_LOGIC" | "DATA_ACCESS" | "INFRASTRUCTURE"
                    }
                  ],
                  "edges": [
                    {
                      "id": "e-1-2",
                      "source": "node-1",
                      "target": "node-2",
                      "label": "calls / queries",
                      "animated": true
                    }
                  ]
                }
                
                Codebase Outline:
                """ + outlineJson;
    }

    /**
     * Offline හෝ API Key නොමැති අවස්ථාවලදී AST data යොදාගෙන React Flow nodes සහ edges සාදන Deterministic Generator එක.
     */
    public ArchitectureGraphDto synthesizeDeterministicGraph(CodebaseOutline outline) {
        List<ArchitectureNodeDto> nodes = new ArrayList<>();
        List<ArchitectureEdgeDto> edges = new ArrayList<>();

        Map<String, String> componentIdMap = new java.util.HashMap<>();

        // 1. Nodes නිර්මාණය කිරීම
        for (int i = 0; i < outline.components().size(); i++) {
            ExtractedComponent comp = outline.components().get(i);
            String nodeId = "node-" + (i + 1);
            componentIdMap.put(comp.name(), nodeId);

            String nodeType = switch (comp.type()) {
                case CONTROLLER -> "controllerNode";
                case SERVICE -> "serviceNode";
                case REPOSITORY, ENTITY -> "databaseNode";
                default -> "gatewayNode";
            };

            String tier = switch (comp.type()) {
                case CONTROLLER -> "API";
                case SERVICE -> "BUSINESS_LOGIC";
                case REPOSITORY, ENTITY -> "DATA_ACCESS";
                default -> "INFRASTRUCTURE";
            };

            nodes.add(new ArchitectureNodeDto(
                    nodeId,
                    nodeType,
                    comp.name(),
                    comp.filePath(),
                    comp.startLine(),
                    comp.summary(),
                    tier
            ));
        }

        // 2. Dependencies අනුව Edges (සම්බන්ධතා) නිර්මාණය කිරීම
        int edgeIndex = 1;
        for (ExtractedComponent comp : outline.components()) {
            String sourceId = componentIdMap.get(comp.name());
            if (sourceId == null) continue;

            for (String depName : comp.dependencies()) {
                String targetId = componentIdMap.get(depName);
                if (targetId != null && !sourceId.equals(targetId)) {
                    edges.add(new ArchitectureEdgeDto(
                            "edge-" + (edgeIndex++),
                            sourceId,
                            targetId,
                            "injects / calls",
                            true
                    ));
                }
            }
        }

        return new ArchitectureGraphDto(
                "Codebase contains " + nodes.size() + " primary architectural components across " + outline.totalFiles() + " source files.",
                "Layered Architecture (N-Tier)",
                nodes,
                edges
        );
    }
}
