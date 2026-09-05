package com.architect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.architect.backend.model.RepositoryEntity;
import com.architect.backend.model.RepositoryStatus;

/**
 * RepositoryEntity සඳහා Database Queries සිදුකරන Spring Data JPA Repository එක.
 * JpaRepository මඟින් save(), findById(), findAll(), delete() වැනි මූලික CRUD ක්‍රම නොමිලේ ලැබේ.
 */
@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, UUID> {

    /**
     * GitHub URL එකක් මඟින් Repository එකක් සොයාගැනීම.
     * Spring Data JPA මඟින් method එකේ නම අනුව (Derived Query) SQL එක generate කරයි:
     * "SELECT * FROM repositories WHERE repo_url = ?"
     */
    Optional<RepositoryEntity> findByRepoUrl(String repoUrl);

    /**
     * අදාළ User හට අයත් සියලුම Repositories ලබාගැනීම.
     */
    List<RepositoryEntity> findByUserId(UUID userId);

    /**
     * Status එක (උදා: PENDING) අනුව Repositories ලබාගැනීම (Background task worker එකට ප්‍රයෝජනවත් වේ).
     */
    List<RepositoryEntity> findByStatus(RepositoryStatus status);
}
