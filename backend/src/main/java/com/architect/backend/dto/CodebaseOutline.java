package com.architect.backend.dto;

import java.util.List;

/**
 * Gemini AI එක වෙත යැවීම සඳහා සම්පූර්ණ Codebase එකෙන් සාරාංශ කරගත් Blueprint එක (Codebase Outline).
 */
public record CodebaseOutline(
        int totalFiles,
        int totalComponents,
        List<ExtractedComponent> components,
        List<String> allFilePaths
) {
}
