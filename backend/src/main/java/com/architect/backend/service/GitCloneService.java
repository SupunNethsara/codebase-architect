package com.architect.backend.service;

import com.architect.backend.dto.ClonedRepository;

/**
 * GitHub repository එකක් shallow clone (--depth 1) කර තාවකාලික නාමාවලියක (Temp Directory)
 * රඳවා තබාගැනීම සිදුකරන Service interface එක.
 */
public interface GitCloneService {

    /**
     * ලබාදෙන Git URL සහ branch එක සඳහා shallow clone එකක් සිදුකර ClonedRepository එකක් ලබා දේ.
     *
     * @param repoUrl GitHub repository URL (e.g. https://github.com/owner/repo)
     * @param branch  Clone කළ යුතු branch එක (e.g. "main", null නම් default branch)
     * @return ClonedRepository (AutoCloseable temp directory wrapper)
     */
    ClonedRepository cloneRepository(String repoUrl, String branch);
}
