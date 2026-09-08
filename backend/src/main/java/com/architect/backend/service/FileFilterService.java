package com.architect.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.architect.backend.dto.SourceFileInfo;

/**
 * Cloned repository එකක ඇති non-source files (.git, node_modules, target, lockfiles, media)
 * ඉවත් කර අත්‍යවශ්‍ය source code files පමණක් වෙන්කර දෙන Service interface එක.
 */
public interface FileFilterService {

    /**
     * Root directory එකේ ඇති සියලුම files scan කර source code files ලැයිස්තුවක් ලබා දේ.
     *
     * @param rootDirectory Repository එක clone කළ base directory path එක
     * @return Source code files ලැයිස්තුව
     * @throws IOException Directory traverse කිරීමේදී දෝෂයක් ආවොත්
     */
    List<SourceFileInfo> filterSourceFiles(Path rootDirectory) throws IOException;
}
