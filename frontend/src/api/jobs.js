import API from './client';

export const jobsAPI = {
  create: (data) => API.post('/api/jobs', data),
  getAll: (params) => API.get('/api/jobs', { params }),
  getById: (id) => API.get(`/api/jobs/${id}`),
  getByRecruiter: (recruiterId) => API.get(`/api/jobs/recruiter/${recruiterId}`),
  update: (id, data) => API.put(`/api/jobs/${id}`, data),
  updateStatus: (id, status) => API.put(`/api/jobs/${id}/status`, { status }),
  remove: (id) => API.delete(`/api/jobs/${id}`),
};
