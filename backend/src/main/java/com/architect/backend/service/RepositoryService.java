package com.architect.backend.service;

import java.util.List;
import java.util.UUID;

import com.architect.backend.dto.request.CreateRepositoryRequest;
import com.architect.backend.dto.response.RepositoryResponse;

/**
 * Repository management සම්බන්ධ business logic මෙහෙයවන Service Interface එක.
 * 
 * ඇයි Interface එකක් ලියන්නේ?
 * 1. Loose Coupling: Controller එක Service එකේ Implementation එක මත කෙලින්ම depend නොවී Interface එක මත depend වීම.
 * 2. Maintainability & Testing: පසුව වෙනත් implementation එකක් (උදා: CachedRepositoryService) පහසුවෙන් ආදේශ කිරීමට හැකි වීම.
 */
public interface RepositoryService {

    /**
     * අලුත් Repository එකක් ලියාපදිංචි කර PENDING status එකෙන් save කිරීම.
     */
    RepositoryResponse createRepository(CreateRepositoryRequest request);

    /**
     * ID එක මඟින් Repository එකක් සොයාගැනීම (හමුනොවුනහොත් ResourceNotFoundException throw වේ).
     */
    RepositoryResponse getRepositoryById(UUID id);

    /**
     * අදාළ User හට අයත් සියලුම Repositories ලබාගැනීම.
     */
    List<RepositoryResponse> getRepositoriesByUserId(UUID userId);
}
