package com.architect.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * React Flow සඳහා අවශ්‍ය Node දත්ත (SRS Section 5 Specification).
 */
public record ArchitectureNodeDto(
        @JsonProperty(required = true)
        String id,

        @JsonProperty(required = true)
        String type, // "controllerNode", "serviceNode", "databaseNode", "gatewayNode"

        @JsonProperty(required = true)
        String label,

        @JsonProperty(required = true)
        String filePath,

        @JsonProperty(required = true)
        int startLine,

        String description,

        @JsonProperty(required = true)
        String tier // "API", "BUSINESS_LOGIC", "DATA_ACCESS", "INFRASTRUCTURE"
) {
}
