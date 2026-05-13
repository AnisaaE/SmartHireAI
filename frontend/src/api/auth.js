import API from './client';

export const authAPI = {
  register: (data) => API.post('/api/auth/register', data),
  login: (data) => API.post('/api/auth/login', data),
  validate: () => API.get('/api/auth/validate'),
  getAllUsers: () => API.get('/api/auth/users'),
  getUser: (id) => API.get(`/api/auth/users/${id}`),
  updateUser: (id, data) => API.put(`/api/auth/users/${id}`, data),
  deleteUser: (id) => API.delete(`/api/auth/users/${id}`),
};
