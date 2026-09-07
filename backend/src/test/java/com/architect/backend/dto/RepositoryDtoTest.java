package com.architect.backend.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;
import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Request & Response DTOs සඳහා Unit Tests.
 * මෙහිදී Spring Boot server එක හෝ Database එකක් අවශ්‍ය නොවන අතර,
 * Jakarta Bean Validator එක direct use කර වේගවත් Unit Tests සිදුකෙරේ.
 */
class RepositoryDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Jakarta Bean Validator එක initialize කරගැනීම
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("නිවැරදි data සහිත CreateRepositoryRequest එකක් validation pass විය යුතුය")
    void shouldPassValidationWithValidRequest() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .userId(UUID.randomUUID())
                .repoUrl("https://github.com/spring-projects/spring-boot")
                .defaultBranch("main")
                .build();

        Set<ConstraintViolation<CreateRepositoryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("අවලංගු GitHub URL එකක් හෝ හිස් URL එකක් ලබාදුන් විට validation fail විය යුතුය")
    void shouldFailValidationWithInvalidRepoUrl() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .userId(UUID.randomUUID())
                .repoUrl("not-a-valid-url")
                .build();

        Set<ConstraintViolation<CreateRepositoryRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("repoUrl"));
    }

    @Test
    @DisplayName("UserId null වූ විට validation fail විය යුතුය")
    void shouldFailValidationWhenUserIdIsNull() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .repoUrl("https://github.com/google/guava")
                .build();

        Set<ConstraintViolation<CreateRepositoryRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    @Test
    @DisplayName("RepositoryEntity එකක් සාර්ථකව RepositoryResponse DTO එකක් බවට convert විය යුතුය")
    void shouldMapEntityToResponseSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        RepositoryEntity entity = RepositoryEntity.builder()
                .id(id)
                .userId(userId)
                .repoUrl("https://github.com/spring-projects/spring-boot")
                .defaultBranch("main")
                .status(RepositoryStatus.PENDING)
                .createdAt(now)
                .build();

        RepositoryResponse response = RepositoryResponse.fromEntity(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRepoUrl()).isEqualTo("https://github.com/spring-projects/spring-boot");
        assertThat(response.getStatus()).isEqualTo(RepositoryStatus.PENDING);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }
}
