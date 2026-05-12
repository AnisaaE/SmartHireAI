import API from './client';

export const analysisAPI = {
  start: (jobId) => API.post('/api/analysis/start', { jobId }),
  getById: (analysisId) => API.get(`/api/analysis/${analysisId}`),
  getReport: (jobId) => API.get(`/api/analysis/report/${jobId}`),
  getCandidates: (analysisId) => API.get(`/api/analysis/${analysisId}/candidates`),
  update: (analysisId, data) => API.put(`/api/analysis/${analysisId}`, data),
  restart: (analysisId) => API.put(`/api/analysis/${analysisId}/restart`),
  updateStatus: (analysisId, status) => API.put(`/api/analysis/${analysisId}/status`, { status }),
  remove: (analysisId) => API.delete(`/api/analysis/${analysisId}`),
  clearCache: (analysisId) => API.delete(`/api/analysis/${analysisId}/cache`),
};
