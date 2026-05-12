import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI } from '../api/auth';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('smarthire_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('smarthire_token'));
  const [loading, setLoading] = useState(false);

  const isAuthenticated = !!token && !!user;
  const isRecruiter = user?.role === 'RECRUITER';
  const isCandidate = user?.role === 'CANDIDATE';

  const login = useCallback(async (credentials) => {
    setLoading(true);
    try {
      const { data } = await authAPI.login(credentials);
      const jwt = data.token;
      const userData = data.user || data;
      localStorage.setItem('smarthire_token', jwt);
      localStorage.setItem('smarthire_user', JSON.stringify(userData));
      setToken(jwt);
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (formData) => {
    setLoading(true);
    try {
      const { data } = await authAPI.register(formData);
      return data;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('smarthire_token');
    localStorage.removeItem('smarthire_user');
    setToken(null);
    setUser(null);
  }, []);

  const updateUser = useCallback((updatedData) => {
    const newUser = { ...user, ...updatedData };
    localStorage.setItem('smarthire_user', JSON.stringify(newUser));
    setUser(newUser);
  }, [user]);

  return (
    <AuthContext.Provider value={{ user, token, loading, isAuthenticated, isRecruiter, isCandidate, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
