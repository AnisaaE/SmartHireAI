import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Sun, Moon, LogOut, User, Zap } from 'lucide-react';
import './Navbar.css';

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">
          <div className="navbar-logo"><Zap size={20} /></div>
          <div className="navbar-brand-copy">
            <span className="navbar-brand-text">SmartHire<span className="navbar-brand-ai">AI</span></span>
            <span className="navbar-brand-subtitle">Talent Operations Interface</span>
          </div>
        </Link>
        <div className="navbar-actions">
          <div className="navbar-system-pill">
            <span className="navbar-system-dot" />
            <span>Live Session</span>
          </div>
          <button className="navbar-icon-btn" onClick={toggleTheme} title="Toggle theme">
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          {isAuthenticated && (
            <>
              <div className="navbar-user">
                <div className="navbar-avatar"><User size={16} /></div>
                <div className="navbar-user-info">
                  <span className="navbar-username">{user?.username}</span>
                  <span className="navbar-role">{user?.role}</span>
                </div>
              </div>
              <button className="navbar-icon-btn navbar-logout" onClick={handleLogout} title="Log out">
                <LogOut size={18} />
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
