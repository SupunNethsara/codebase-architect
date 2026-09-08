package com.architect.backend.service;

import java.nio.file.Path;
import java.util.List;

import com.architect.backend.dto.CodebaseOutline;
import com.architect.backend.dto.ExtractedComponent;
import com.architect.backend.dto.SourceFileInfo;

/**
 * Filter කරන ලද source code files කියවා Architectural Components හඳුනාගෙන
 * සම්පූර්ණ Codebase Outline එකක් (AST Blueprint) සකස් කරන Service interface එක (FR-3).
 */
public interface CodeParserService {

    /**
     * Source files ලැයිස්තුවක් ලබා දී සම්පූර්ණ Codebase Outline එකක් නිර්මාණය කරයි.
     *
     * @param rootDirectory Repository එකේ base root path එක
     * @param sourceFiles   FileFilterService මඟින් ලබාගත් clean source files
     * @return Gemini AI එකට යැවීමට සුදුසු CodebaseOutline එකක්
     */
    CodebaseOutline extractOutline(Path rootDirectory, List<SourceFileInfo> sourceFiles);

    /**
     * තනි source code file එකක් කියවා එහි ඇති component එක සහ startLine හඳුනාගැනීම.
     */
    ExtractedComponent parseFile(Path rootDirectory, SourceFileInfo fileInfo);
}
