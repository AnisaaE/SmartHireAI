import API from './client';

export const documentsAPI = {
  upload: (file, type, title) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    if (title) formData.append('title', title);
    return API.post('/api/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getById: (id) => API.get(`/api/documents/${id}`),
  getByOwner: (userId) => API.get(`/api/documents/owner/${userId}`),
  getContent: (id) => API.get(`/api/documents/content/${id}`),
  getCvByCandidate: (candidateId) => API.get(`/api/documents/cv/${candidateId}`),
  update: (id, data) => API.put(`/api/documents/${id}`, data),
  reprocess: (id) => API.put(`/api/documents/${id}/reprocess`),
  remove: (id) => API.delete(`/api/documents/${id}`),
};
