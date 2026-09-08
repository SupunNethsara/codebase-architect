package com.architect.backend.dto;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shallow clone කරන ලද repository එකක් නිරූපණය කරන Resource wrapper class එක.
 * AutoCloseable implement කර ඇති බැවින් try-with-resources මඟින් analysis එක අවසානයේ
 * temporary directory එක ආරක්ෂිතව system එකෙන් auto-delete (purge) වේ.
 */
public record ClonedRepository(
        Path directory,
        String commitHash,
        String branch
) implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ClonedRepository.class);

    @Override
    public void close() {
        if (directory != null && Files.exists(directory)) {
            try {
                log.info("Purging ephemeral repository directory: {}", directory);
                deleteRecursively(directory);
            } catch (IOException e) {
                log.warn("Failed to completely delete temporary directory {}: {}", directory, e.getMessage());
            }
        }
    }

    /**
     * Temporary directory එකක් සහ එහි ඇතුළත ඇති සියලුම files/folders recursive ලෙස delete කිරීම.
     */
    private static void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Windows වල read-only files delete කිරීමට පෙර writable කිරීම
                file.toFile().setWritable(true);
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.toFile().setWritable(true);
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
