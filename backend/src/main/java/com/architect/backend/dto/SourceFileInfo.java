package com.architect.backend.dto;

/**
 * Filter කිරීමෙන් පසු හඳුනාගත් Source Code file එකක මූලික තොරතුරු.
 */
public record SourceFileInfo(
        String relativePath,
        String fileName,
        String extension,
        long sizeBytes,
        int lineCount
) {
}
