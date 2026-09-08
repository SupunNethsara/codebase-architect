import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export const api = {
  // 1. Register a new repository
  createRepository: async (repoUrl, defaultBranch = 'main') => {
    // Default system user UUID
    const userId = '11111111-1111-1111-1111-111111111111';
    const response = await apiClient.post('/repositories', {
      userId,
      repoUrl,
      defaultBranch,
    });
    return response.data;
  },

  // 2. Fetch repository status
  getRepository: async (id) => {
    const response = await apiClient.get(`/repositories/${id}`);
    return response.data;
  },

  // 3. Trigger asynchronous background analysis
  triggerAnalysis: async (id) => {
    const response = await apiClient.post(`/repositories/${id}/analyze`);
    return response.data;
  },

  // 4. Fetch architecture snapshot (React Flow nodes & edges)
  getSnapshot: async (id) => {
    const response = await apiClient.get(`/repositories/${id}/snapshot`);
    return response.data;
  },
};
