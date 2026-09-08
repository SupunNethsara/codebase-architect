package com.architect.backend.model;

/**
 * Source Code එකක ඇති ප්‍රධාන Architectural Component වර්ග.
 * React Flow Nodes සහ Gemini AI Schema සඳහා භාවිතා වේ.
 */
public enum CodeComponentType {
    /**
     * REST Endpoints, API Routes, සහ Web Controllers.
     */
    CONTROLLER,

    /**
     * Core Business Logic, Services, සහ Use-Cases.
     */
    SERVICE,

    /**
     * Spring Data Repositories, DAOs, සහ Data Access layer.
     */
    REPOSITORY,

    /**
     * Database Entities, Tables, සහ ORM Models.
     */
    ENTITY,

    /**
     * API Gateways, Filters, සහ Middleware.
     */
    GATEWAY,

    /**
     * Security, CORS, Database, සහ System Configurations.
     */
    CONFIG,

    /**
     * සාමාන්‍ය Helper methods සහ Utility classes.
     */
    UTILITY,

    /**
     * නිශ්චිතව හඳුනාගත නොහැකි වූ Component එකක්.
     */
    UNKNOWN
}
