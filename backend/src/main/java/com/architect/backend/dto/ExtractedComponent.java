package com.architect.backend.dto;

import java.util.List;

import com.architect.backend.model.CodeComponentType;

/**
 * Parsing engine එක මඟින් source code file එකකින් සොයාගත් Architectural Component එකක විස්තර.
 */
public record ExtractedComponent(
        String name,
        CodeComponentType type,
        String filePath,
        int startLine,
        String summary,
        List<String> annotations,
        List<String> dependencies
) {
}
