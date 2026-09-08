package com.architect.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.architect.backend.dto.SourceFileInfo;

/**
 * Deterministic File Filtering ක්‍රියාත්මක කරන implementation එක (FR-2).
 * Non-source files, build artifacts, binaries, සහ media files ඉවත් කරයි.
 */
@Service
public class FileFilterServiceImpl implements FileFilterService {

    private static final Logger log = LoggerFactory.getLogger(FileFilterServiceImpl.class);

    /**
     * ඇතුළත ඇති සියලුම files සමඟ එකවර skip කළ යුතු Folders (High-performance skip).
     */
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git",
            "node_modules",
            "target",
            "dist",
            "build",
            ".idea",
            ".vscode",
            ".gradle",
            "vendor",
            "__pycache__",
            ".next",
            ".nuxt",
            "coverage",
            "out",
            "bin",
            "obj",
            ".settings"
    );

    /**
     * Source code නොවන නිසා ඉවත් කළ යුතු File Extensions.
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            // Binaries & Executables
            "class", "jar", "war", "ear", "exe", "dll", "so", "dylib", "bin",
            // Media & Images
            "png", "jpg", "jpeg", "gif", "svg", "ico", "webp", "mp4", "mp3", "pdf", "psd",
            // Archives
            "zip", "tar", "gz", "7z", "rar",
            // Lockfiles
            "lock", "lockb", "sum",
            // Source maps & Minified artifacts
            "map", "min.js", "min.css",
            // Fonts
            "woff", "woff2", "ttf", "eot", "otf"
    );

    /**
     * Max source file size (500 KB) - minified හෝ machine-generated files වලින් ආරක්ෂා වීමට.
     */
    private static final long MAX_FILE_SIZE_BYTES = 500 * 1024;

    @Override
    public List<SourceFileInfo> filterSourceFiles(Path rootDirectory) throws IOException {
        if (!Files.exists(rootDirectory) || !Files.isDirectory(rootDirectory)) {
            throw new IllegalArgumentException("Root directory does not exist or is not a directory: " + rootDirectory);
        }

        List<SourceFileInfo> sourceFiles = new ArrayList<>();

        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (IGNORED_DIRECTORIES.contains(dirName)) {
                    log.debug("Skipping ignored directory: {}", dirName);
                    return FileVisitResult.SKIP_SUBTREE; // මෙම folder එක ඇතුළට යාම සම්පූර්ණයෙන්ම නවත්වයි
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!attrs.isRegularFile()) {
                    return FileVisitResult.CONTINUE;
                }

                String fileName = file.getFileName().toString();
                String extension = getFileExtension(fileName);

                // Ignore criteria පරීක්ෂා කිරීම
                if (IGNORED_EXTENSIONS.contains(extension.toLowerCase()) || fileName.startsWith(".")) {
                    return FileVisitResult.CONTINUE;
                }

                long size = attrs.size();
                if (size > MAX_FILE_SIZE_BYTES || size == 0) {
                    return FileVisitResult.CONTINUE;
                }

                // Relative path එක '/' format එකෙන් සකස් කිරීම
                String relativePath = rootDirectory.relativize(file).toString().replace('\\', '/');
                int lineCount = countLines(file);

                sourceFiles.add(new SourceFileInfo(
                        relativePath,
                        fileName,
                        extension,
                        size,
                        lineCount
                ));

                return FileVisitResult.CONTINUE;
            }
        });

        log.info("Filtering complete for {}. Found {} source code files.", rootDirectory, sourceFiles.size());
        return sourceFiles;
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private static int countLines(Path file) {
        int lines = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (Exception e) {
            // Binary හෝ non-UTF8 file එකක් නම් 0 ලෙස සලකයි
            return 0;
        }
        return lines;
    }
}
