package com.architect.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.architect.backend.dto.ClonedRepository;
import com.architect.backend.exception.GitOperationException;

/**
 * Eclipse JGit භාවිතයෙන් shallow clone (--depth 1) සිදුකරන implementation එක.
 */
@Service
public class GitCloneServiceImpl implements GitCloneService {

    private static final Logger log = LoggerFactory.getLogger(GitCloneServiceImpl.class);
    private static final String TEMP_DIR_PREFIX = "codebase-architect-repo-";

    @Override
    public ClonedRepository cloneRepository(String repoUrl, String branch) {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new GitOperationException("Repository URL cannot be null or blank");
        }

        String targetBranch = (branch != null && !branch.isBlank()) ? branch : "main";
        Path tempDir;

        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            log.info("Created temporary directory for cloning: {}", tempDir);
        } catch (IOException e) {
            throw new GitOperationException("Failed to create temporary directory for cloning", e);
        }

        try {
            log.info("Starting shallow clone for: {} (branch: {}) into {}", repoUrl, branch != null ? branch : "DEFAULT", tempDir);

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(tempDir.toFile())
                    .setDepth(1)
                    .setCloneAllBranches(false);

            if (branch != null && !branch.isBlank()) {
                cloneCommand.setBranch(branch);
            }

            String commitHash;
            String resolvedBranch;
            try (Git git = cloneCommand.call()) {
                ObjectId head = git.getRepository().resolve("HEAD");
                commitHash = (head != null) ? head.getName() : "HEAD";
                resolvedBranch = git.getRepository().getBranch();
                log.info("Shallow clone completed. Commit hash: {}, Branch: {}", commitHash, resolvedBranch);
            }

            return new ClonedRepository(tempDir, commitHash, resolvedBranch);

        } catch (GitAPIException | IOException e) {
            log.error("Error during Git clone for repository {}: {}", repoUrl, e.getMessage());
            // Clone එක අසාර්ථක වුවහොත් temp directory එක වහාම delete කරමු (Resource leak prevention)
            new ClonedRepository(tempDir, null, null).close();
            throw new GitOperationException("Failed to clone repository from " + repoUrl + ": " + e.getMessage(), e);
        }
    }
}
