import { createContext, useContext, useState, useCallback } from 'react';
import { authAPI } from '../api/auth';
import API from '../api/client';

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
      // Step 1: Login returns only { token }
      const { data } = await authAPI.login(credentials);
      const jwt = data.token;
      localStorage.setItem('smarthire_token', jwt);
      setToken(jwt);

      // Step 2: Fetch user info using getAllUsers and find by username
      // (backend LoginResponse only contains token, no user data)
      API.defaults.headers.common['Authorization'] = `Bearer ${jwt}`;
      const { data: users } = await authAPI.getAllUsers();
      const userData = users.find(u => u.username === credentials.username);

      if (!userData) {
        throw new Error('User data not found after login');
      }

      localStorage.setItem('smarthire_user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } catch (err) {
      // If login fails, clean up
      localStorage.removeItem('smarthire_token');
      setToken(null);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (formData) => {
    setLoading(true);
    try {
      // Backend register returns 201 with no body
      await authAPI.register(formData);
      return { success: true };
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
