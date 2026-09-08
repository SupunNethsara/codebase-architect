package com.architect.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gemini AI API එකෙන් කෙලින්ම generate වන සම්පූර්ණ Architecture Graph Schema එක (SRS Section 5).
 */
public record ArchitectureGraphDto(
        @JsonProperty(required = true)
        String summary,

        @JsonProperty(required = true)
        String architecturePattern,

        @JsonProperty(required = true)
        List<ArchitectureNodeDto> nodes,

        @JsonProperty(required = true)
        List<ArchitectureEdgeDto> edges
) {
}
