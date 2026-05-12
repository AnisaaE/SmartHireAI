import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard, Briefcase, FileText, Users, Brain, Settings,
  Search, Send, FolderOpen
} from 'lucide-react';
import './Sidebar.css';

const recruiterLinks = [
  { to: '/recruiter/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/recruiter/jobs', icon: Briefcase, label: 'Jobs' },
  { to: '/recruiter/applications', icon: Users, label: 'Applications' },
  { to: '/recruiter/documents', icon: FolderOpen, label: 'Documents' },
  { to: '/recruiter/analysis', icon: Brain, label: 'AI Analysis' },
  { to: '/recruiter/profile', icon: Settings, label: 'Settings' },
];

const candidateLinks = [
  { to: '/candidate/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/candidate/jobs', icon: Search, label: 'Browse Jobs' },
  { to: '/candidate/applications', icon: Send, label: 'My Applications' },
  { to: '/candidate/documents', icon: FileText, label: 'My CVs' },
  { to: '/candidate/profile', icon: Settings, label: 'Settings' },
];

export default function Sidebar() {
  const { isRecruiter } = useAuth();
  const links = isRecruiter ? recruiterLinks : candidateLinks;

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        <div className="sidebar-section-label">{isRecruiter ? 'Recruiter' : 'Candidate'}</div>
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
