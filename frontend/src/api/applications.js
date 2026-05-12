import API from './client';

export const applicationsAPI = {
  apply: (data) => API.post('/api/applications', data),
  getById: (id) => API.get(`/api/applications/${id}`),
  getByJob: (jobId) => API.get(`/api/applications/job/${jobId}`),
  getByCandidate: (candidateId) => API.get(`/api/applications/candidate/${candidateId}`),
  update: (id, data) => API.put(`/api/applications/${id}`, data),
  updateStatus: (id, status) => API.put(`/api/applications/${id}/status`, { status }),
  remove: (id) => API.delete(`/api/applications/${id}`),
};
