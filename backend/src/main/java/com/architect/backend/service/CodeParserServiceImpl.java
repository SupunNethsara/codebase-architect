package com.architect.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.ExtractedComponent;
import com.architect.backend.dto.SourceFileInfo;
import com.architect.backend.model.CodeComponentType;

/**
 * Multi-language Code Parser Implementation (FR-3).
 * Source code files කියවා AST & Structural Signatures (Class, Type, Start Line, Annotations, Dependencies) හඳුනාගනියි.
 */
@Service
public class CodeParserServiceImpl implements CodeParserService {

    private static final Logger log = LoggerFactory.getLogger(CodeParserServiceImpl.class);

    // Class / Interface / Record definition pattern
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?:public\\s+|private\\s+|protected\\s+|export\\s+|default\\s+)*(?:class|interface|record|enum)\\s+([A-Za-z0-9_]+)"
    );

    // Python Class definition pattern
    private static final Pattern PYTHON_CLASS_PATTERN = Pattern.compile("^\\s*class\\s+([A-Za-z0-9_]+)");

    // Injected Dependency pattern (e.g., "private final UserService userService;" or "constructor(private userService: UserService)")
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
            "(?:private|protected|public)?\\s+(?:final\\s+)?([A-Z][A-Za-z0-9_]+)(?:<[^>]+>)?\\s+[a-z][A-Za-z0-9_]*\\s*;"
    );

    @Override
    public CodebaseOutline extractOutline(Path rootDirectory, List<SourceFileInfo> sourceFiles) {
        log.info("Starting AST & Blueprint extraction across {} files", sourceFiles.size());

        List<ExtractedComponent> components = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();

        for (SourceFileInfo fileInfo : sourceFiles) {
            filePaths.add(fileInfo.relativePath());
            ExtractedComponent component = parseFile(rootDirectory, fileInfo);
            if (component != null && component.type() != CodeComponentType.UNKNOWN) {
                components.add(component);
            }
        }

        log.info("Blueprint extraction completed. Identified {} primary architectural components.", components.size());
        return new CodebaseOutline(sourceFiles.size(), components.size(), components, filePaths);
    }

    @Override
    public ExtractedComponent parseFile(Path rootDirectory, SourceFileInfo fileInfo) {
        Path fullPath = rootDirectory.resolve(fileInfo.relativePath());
        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            return null;
        }

        String fileName = fileInfo.fileName();
        String relativePath = fileInfo.relativePath().replace('\\', '/');

        String componentName = stripExtension(fileName);
        int startLine = 1;
        List<String> annotations = new ArrayList<>();
        List<String> dependencies = new ArrayList<>();
        CodeComponentType detectedType = inferTypeFromPathOrName(relativePath, componentName);

        try (BufferedReader reader = Files.newBufferedReader(fullPath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            boolean classFound = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                // 1. Annotations හඳුනා ගැනීම (Java / TypeScript / Python Decorators)
                if (trimmed.startsWith("@")) {
                    annotations.add(trimmed);
                    CodeComponentType typeFromAnnotation = inferTypeFromAnnotation(trimmed);
                    if (typeFromAnnotation != CodeComponentType.UNKNOWN) {
                        detectedType = typeFromAnnotation;
                    }
                }

                // 2. Class / Interface declaration සහ StartLine හඳුනා ගැනීම
                if (!classFound) {
                    Matcher classMatcher = CLASS_PATTERN.matcher(trimmed);
                    if (classMatcher.find()) {
                        componentName = classMatcher.group(1);
                        startLine = lineNumber;
                        classFound = true;
                    } else {
                        Matcher pyMatcher = PYTHON_CLASS_PATTERN.matcher(trimmed);
                        if (pyMatcher.find()) {
                            componentName = pyMatcher.group(1);
                            startLine = lineNumber;
                            classFound = true;
                        }
                    }
                }

                // 3. Injected Dependencies හඳුනා ගැනීම (Spring DI / Fields)
                if (classFound) {
                    Matcher depMatcher = DEPENDENCY_PATTERN.matcher(trimmed);
                    if (depMatcher.find()) {
                        String depType = depMatcher.group(1);
                        // Primitive types හෝ Common Java types ignore කිරීම
                        if (!isCommonLibraryType(depType) && !dependencies.contains(depType)) {
                            dependencies.add(depType);
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.warn("Failed to parse file {}: {}", relativePath, e.getMessage());
        }

        String summary = generateComponentSummary(componentName, detectedType, annotations);

        return new ExtractedComponent(
                componentName,
                detectedType,
                relativePath,
                startLine,
                summary,
                annotations,
                dependencies
        );
    }

    private static CodeComponentType inferTypeFromAnnotation(String annotation) {
        String lower = annotation.toLowerCase();
        if (lower.contains("restcontroller") || lower.contains("controller") || lower.contains("requestmapping")) {
            return CodeComponentType.CONTROLLER;
        }
        if (lower.contains("service") || lower.contains("injectable")) {
            return CodeComponentType.SERVICE;
        }
        if (lower.contains("repository") || lower.contains("dao")) {
            return CodeComponentType.REPOSITORY;
        }
        if (lower.contains("entity") || lower.contains("table") || lower.contains("document")) {
            return CodeComponentType.ENTITY;
        }
        if (lower.contains("configuration") || lower.contains("config")) {
            return CodeComponentType.CONFIG;
        }
        return CodeComponentType.UNKNOWN;
    }

    private static CodeComponentType inferTypeFromPathOrName(String path, String name) {
        String lowerPath = path.toLowerCase();
        String lowerName = name.toLowerCase();

        if (lowerPath.contains("/controller/") || lowerPath.contains("/routes/") || lowerName.endsWith("controller")) {
            return CodeComponentType.CONTROLLER;
        }
        if (lowerPath.contains("/service/") || lowerName.endsWith("service") || lowerName.endsWith("serviceimpl")) {
            return CodeComponentType.SERVICE;
        }
        if (lowerPath.contains("/repository/") || lowerPath.contains("/dao/") || lowerName.endsWith("repository") || lowerName.endsWith("dao")) {
            return CodeComponentType.REPOSITORY;
        }
        if (lowerPath.contains("/model/") || lowerPath.contains("/entity/") || lowerPath.contains("/entities/") || lowerName.endsWith("entity")) {
            return CodeComponentType.ENTITY;
        }
        if (lowerPath.contains("/config/") || lowerName.endsWith("config") || lowerName.endsWith("configuration")) {
            return CodeComponentType.CONFIG;
        }
        if (lowerPath.contains("/util/") || lowerPath.contains("/utils/") || lowerName.endsWith("util") || lowerName.endsWith("helper")) {
            return CodeComponentType.UTILITY;
        }
        return CodeComponentType.UNKNOWN;
    }

    private static boolean isCommonLibraryType(String type) {
        return List.of("String", "Integer", "Long", "Boolean", "Double", "List", "Map", "Set", "Logger", "UUID", "Instant")
                .contains(type);
    }

    private static String generateComponentSummary(String name, CodeComponentType type, List<String> annotations) {
        return switch (type) {
            case CONTROLLER -> "REST API Controller handling client requests for " + name;
            case SERVICE -> "Core business logic service executing operations for " + name;
            case REPOSITORY -> "Data access repository managing database transactions for " + name;
            case ENTITY -> "Database persistence model representing " + name;
            case CONFIG -> "Application configuration defining beans for " + name;
            default -> "Architectural component: " + name;
        };
    }

    private static String stripExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx > 0) ? fileName.substring(0, idx) : fileName;
    }
}
