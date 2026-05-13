import API from './client';

export const documentsAPI = {
  upload: (file, ownerId, type, title) => {
    const formData = new FormData();
    formData.append('file', file);
    const params = new URLSearchParams({
      ownerId: String(ownerId),
      type: String(type),
      title: title || file.name,
    });
    return API.post(`/api/documents/upload?${params.toString()}`, formData);
  },
  getById: (id) => API.get(`/api/documents/${id}`),
  getByOwner: (userId) => API.get(`/api/documents/owner/${userId}`),
  getContent: (id) => API.get(`/api/documents/content/${id}`),
  getCvByCandidate: (candidateId) => API.get(`/api/documents/cv/${candidateId}`),
  update: (id, data) => API.put(`/api/documents/${id}`, data),
  reprocess: (id) => API.put(`/api/documents/${id}/reprocess`),
  remove: (id) => API.delete(`/api/documents/${id}`),
};
