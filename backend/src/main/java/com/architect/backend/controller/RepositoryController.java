package com.architect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;
import com.architect.backend.service.RepositoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Repositories සඳහා වන REST API Endpoints මෙහෙයවන Controller එක.
 * Base Path: "/api/v1/repositories"
 * 
 * වැදගත් Spring Boot සංකල්ප:
 * 1. @RestController: HTTP requests භාරගෙන return කරන objects කෙලින්ම JSON බවට convert කර යවයි.
 * 2. @RequestMapping: මෙම controller එකේ සියලුම endpoints සඳහා base URL prefix එක සකසයි.
 * 3. @Valid: Request body එකේ data (DTO annotations) validate කිරීමට භාවිතා කරයි.
 * 4. @RequiredArgsConstructor: RepositoryService එක Constructor Injection මඟින් inject කරගැනීමට.
 */
@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    /**
     * අලුත් Repository එකක් create කිරීම.
     * POST /api/v1/repositories
     * Status: 201 CREATED
     */
    @PostMapping
    public ResponseEntity<RepositoryResponse> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request) {
        RepositoryResponse response = repositoryService.createRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ID එක මඟින් Repository විස්තර ලබාගැනීම.
     * GET /api/v1/repositories/{id}
     * Status: 200 OK (හමුනොවුනහොත් 404 NOT_FOUND)
     */
    @GetMapping("/{id}")
    public ResponseEntity<RepositoryResponse> getRepositoryById(
            @PathVariable UUID id) {
        RepositoryResponse response = repositoryService.getRepositoryById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * User කෙනෙකුගේ සියලුම Repositories ලබාගැනීම.
     * GET /api/v1/repositories?userId={userId}
     * Status: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> getRepositoriesByUserId(
            @RequestParam UUID userId) {
        List<RepositoryResponse> responses = repositoryService.getRepositoriesByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}
