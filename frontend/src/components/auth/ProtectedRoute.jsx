import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ allowedRole }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRole && user?.role !== allowedRole) {
    const redirect = user?.role === 'RECRUITER' ? '/recruiter/dashboard' : '/candidate/dashboard';
    return <Navigate to={redirect} replace />;
  }
  return <Outlet />;
}
