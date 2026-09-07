package com.architect.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;
import com.architect.backend.exception.ResourceNotFoundException;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;
import com.architect.backend.repository.RepositoryEntityRepository;

import lombok.RequiredArgsConstructor;

/**
 * RepositoryService Interface එකේ Implementation එක.
 * 
 * වැදගත් Spring Boot Best Practices:
 * 1. Constructor Injection (@RequiredArgsConstructor): @Autowired වෙනුවට final fields යොදා Constructor Injection භාවිතය වඩාත් ආරක්ෂිත සහ Test කිරීමට පහසු වේ.
 * 2. @Transactional: Database transactions කළමනාකරණය සඳහා. Data කියවීමේදී (read-only = true) performance වැඩි කරයි.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryEntityRepository repositoryEntityRepository;

    @Override
    @Transactional
    public RepositoryResponse createRepository(CreateRepositoryRequest request) {
        // 1. Request DTO එක Entity එකකට convert කිරීම
        RepositoryEntity entity = RepositoryEntity.builder()
                .userId(request.getUserId())
                .repoUrl(request.getRepoUrl())
                .defaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main")
                .status(RepositoryStatus.PENDING) // නව repository එකක මුල් status එක PENDING වේ
                .build();

        // 2. Database එකට save කිරීම
        RepositoryEntity saved = repositoryEntityRepository.save(entity);

        // 3. Entity එක Response DTO එකක් බවට පත්කර return කිරීම
        return RepositoryResponse.fromEntity(saved);
    }

    @Override
    public RepositoryResponse getRepositoryById(UUID id) {
        RepositoryEntity entity = repositoryEntityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + id));

        return RepositoryResponse.fromEntity(entity);
    }

    @Override
    public List<RepositoryResponse> getRepositoriesByUserId(UUID userId) {
        return repositoryEntityRepository.findByUserId(userId)
                .stream()
                .map(RepositoryResponse::fromEntity)
                .toList();
    }
}
