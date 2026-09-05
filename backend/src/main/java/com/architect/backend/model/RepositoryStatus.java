package com.architect.backend.model;

/**
 * GitHub Repository එකක Ingestion & Analysis තත්ත්වය (Status).
 */
public enum RepositoryStatus {
    /**
     * User විසින් URL එක ලබා දී ඇත. Ingestion queue එකේ පවතී.
     */
    PENDING,

    /**
     * Repository එක clone වෙමින් සහ AST parse වෙමින් පවතී.
     */
    PARSING,

    /**
     * Gemini AI මඟින් Architecture Graph එක සාර්ථකව සාදා නිම කර ඇත.
     */
    COMPLETED,

    /**
     * Clone කිරීමට හෝ Parse කිරීමට නොහැකි වී Error එකක් සිදුවී ඇත.
     */
    FAILED
}
