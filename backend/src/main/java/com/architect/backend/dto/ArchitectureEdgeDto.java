package com.architect.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * React Flow Nodes අතර සම්බන්ධතා (Edges / Arrows) නිරූපණය කරන DTO (SRS Section 5).
 */
public record ArchitectureEdgeDto(
        @JsonProperty(required = true)
        String id,

        @JsonProperty(required = true)
        String source,

        @JsonProperty(required = true)
        String target,

        String label,

        boolean animated
) {
    public ArchitectureEdgeDto {
        if (id == null && source != null && target != null) {
            id = "e-" + source + "-" + target;
        }
    }
}
