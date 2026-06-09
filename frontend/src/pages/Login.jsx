import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { User, Lock, Zap, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import CustomGraphics from '../components/common/CustomGraphics';
import toast from 'react-hot-toast';
import './Auth.css';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [errors, setErrors] = useState({});
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  const validate = () => {
    const e = {};
    if (!form.username) e.username = 'Username is required';
    if (!form.password) e.password = 'Password is required';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      const user = await login(form);
      toast.success(`Welcome back, ${user.username}!`);
      navigate(user.role === 'RECRUITER' ? '/recruiter/dashboard' : '/candidate/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials');
    }
  };

  const handleChange = (field) => (e) => {
    setForm({ ...form, [field]: e.target.value });
    if (errors[field]) setErrors({ ...errors, [field]: '' });
  };

  return (
    <div className="auth-page">
      <button className="auth-theme-toggle" onClick={toggleTheme} title="Toggle theme">
        {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
      </button>
      <div className="auth-shell">
        <div className="auth-hero">
          <CustomGraphics variant="auth" className="custom-graphics-auth" />
          <div className="auth-hero-content">
            <div className="auth-hero-kicker">Interactive Hiring Console</div>
            <h1 className="auth-hero-title">Track talent flow through a custom graphics driven interface.</h1>
            <p className="auth-hero-copy">
              Same SmartHire workflow, redesigned as a richer control surface with layered visuals and dashboard framing.
            </p>
            <div className="auth-hero-metrics">
              <div className="auth-metric"><span className="auth-metric-value">01</span><span className="auth-metric-label">Login</span></div>
              <div className="auth-metric"><span className="auth-metric-value">02</span><span className="auth-metric-label">Route</span></div>
              <div className="auth-metric"><span className="auth-metric-value">03</span><span className="auth-metric-label">Operate</span></div>
            </div>
          </div>
        </div>
        <div className="auth-card animate-scale">
          <div className="auth-header">
            <div className="auth-logo"><Zap size={24} /></div>
            <h1 className="auth-title">Welcome Back</h1>
            <p className="auth-subtitle">Sign in to SmartHireAI to continue</p>
          </div>
          <form onSubmit={handleSubmit} className="auth-form">
            <Input label="Username" placeholder="Enter your username" icon={User}
              value={form.username} onChange={handleChange('username')} error={errors.username} />
            <Input label="Password" type="password" placeholder="Enter your password" icon={Lock}
              value={form.password} onChange={handleChange('password')} error={errors.password} />
            <Button type="submit" fullWidth loading={loading} size="lg">Sign In</Button>
          </form>
          <p className="auth-footer">
            Don't have an account? <Link to="/register">Create one</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
