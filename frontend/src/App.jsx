import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import DashboardLayout from './components/layout/DashboardLayout';
import ProtectedRoute from './components/auth/ProtectedRoute';

import Login from './pages/Login';
import Register from './pages/Register';
import NotFound from './pages/NotFound';

import RecruiterDashboard from './pages/recruiter/Dashboard';
import RecruiterJobs from './pages/recruiter/Jobs';
import RecruiterJobDetail from './pages/recruiter/JobDetail';
import RecruiterApplications from './pages/recruiter/Applications';
import RecruiterDocuments from './pages/recruiter/Documents';
import RecruiterAnalysis from './pages/recruiter/Analysis';
import RecruiterProfile from './pages/recruiter/Profile';

import CandidateDashboard from './pages/candidate/Dashboard';
import BrowseJobs from './pages/candidate/BrowseJobs';
import MyApplications from './pages/candidate/MyApplications';
import CandidateDocuments from './pages/candidate/Documents';
import CandidateProfile from './pages/candidate/Profile';

function HomeRedirect() {
  const { isAuthenticated, isRecruiter } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={isRecruiter ? '/recruiter/dashboard' : '/candidate/dashboard'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Recruiter Routes */}
      <Route element={<ProtectedRoute allowedRole="RECRUITER" />}>
        <Route element={<DashboardLayout />}>
          <Route path="/recruiter/dashboard" element={<RecruiterDashboard />} />
          <Route path="/recruiter/jobs" element={<RecruiterJobs />} />
          <Route path="/recruiter/jobs/:id" element={<RecruiterJobDetail />} />
          <Route path="/recruiter/applications" element={<RecruiterApplications />} />
          <Route path="/recruiter/documents" element={<RecruiterDocuments />} />
          <Route path="/recruiter/analysis" element={<RecruiterAnalysis />} />
          <Route path="/recruiter/profile" element={<RecruiterProfile />} />
        </Route>
      </Route>

      {/* Candidate Routes */}
      <Route element={<ProtectedRoute allowedRole="CANDIDATE" />}>
        <Route element={<DashboardLayout />}>
          <Route path="/candidate/dashboard" element={<CandidateDashboard />} />
          <Route path="/candidate/jobs" element={<BrowseJobs />} />
          <Route path="/candidate/applications" element={<MyApplications />} />
          <Route path="/candidate/documents" element={<CandidateDocuments />} />
          <Route path="/candidate/profile" element={<CandidateProfile />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
