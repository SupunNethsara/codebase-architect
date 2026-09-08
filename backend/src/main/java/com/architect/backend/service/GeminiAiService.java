package com.architect.backend.service;

import com.architect.backend.dto.ArchitectureGraphDto;
import com.architect.backend.dto.CodebaseOutline;

/**
 * Google Gemini 1.5 API මඟින් Codebase Outline එකක් Structured Architecture Graph එකක් බවට
 * පරිවර්තනය කරන AI Service interface එක (FR-4).
 */
public interface GeminiAiService {

    /**
     * Codebase Outline එක යොදාගෙන strict JSON schema එකකට අනුකූල Architecture Graph එකක් generate කරයි.
     *
     * @param outline AST Parser මඟින් extract කරන ලද Codebase Blueprint එක
     * @return React Flow canvas එකට කෙලින්ම render කළ හැකි ArchitectureGraphDto එකක්
     */
    ArchitectureGraphDto synthesizeArchitecture(CodebaseOutline outline);
}
