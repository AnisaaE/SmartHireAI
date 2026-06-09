import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User, Zap, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import CustomGraphics from '../components/common/CustomGraphics';
import toast from 'react-hot-toast';
import './Auth.css';

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '', role: 'CANDIDATE' });
  const [errors, setErrors] = useState({});
  const { register, loading } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  const validate = () => {
    const e = {};
    if (!form.username || form.username.length < 3) e.username = 'Username must be at least 3 characters';
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Valid email is required';
    if (!form.password || form.password.length < 8) e.password = 'Password must be at least 8 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      const { confirmPassword, ...data } = form;
      await register(data);
      toast.success('Account created! Please sign in.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
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
            <div className="auth-hero-kicker">Recruiter and Candidate Entry</div>
            <h1 className="auth-hero-title">Create an account inside the same hiring flow, wrapped in a richer UI layer.</h1>
            <p className="auth-hero-copy">
              Registration logic stays the same while the interface adds visual depth, motion, and custom dashboard graphics.
            </p>
            <div className="auth-hero-metrics">
              <div className="auth-metric"><span className="auth-metric-value">ROLE</span><span className="auth-metric-label">Pick</span></div>
              <div className="auth-metric"><span className="auth-metric-value">FORM</span><span className="auth-metric-label">Submit</span></div>
              <div className="auth-metric"><span className="auth-metric-value">FLOW</span><span className="auth-metric-label">Continue</span></div>
            </div>
          </div>
        </div>
        <div className="auth-card animate-scale">
          <div className="auth-header">
            <div className="auth-logo"><Zap size={24} /></div>
            <h1 className="auth-title">Create Account</h1>
            <p className="auth-subtitle">Join SmartHireAI and get started</p>
          </div>
          <form onSubmit={handleSubmit} className="auth-form">
            <Input label="Username" placeholder="johndoe" icon={User}
              value={form.username} onChange={handleChange('username')} error={errors.username} />
            <Input label="Email" type="email" placeholder="you@example.com" icon={Mail}
              value={form.email} onChange={handleChange('email')} error={errors.email} />
            <Input label="Password" type="password" placeholder="Min. 8 characters" icon={Lock}
              value={form.password} onChange={handleChange('password')} error={errors.password} />
            <Input label="Confirm Password" type="password" placeholder="Repeat password" icon={Lock}
              value={form.confirmPassword} onChange={handleChange('confirmPassword')} error={errors.confirmPassword} />
            <div className="auth-role-select">
              <label className="input-label">I am a</label>
              <div className="role-options">
                <button type="button" className={`role-option ${form.role === 'CANDIDATE' ? 'active' : ''}`}
                  onClick={() => setForm({ ...form, role: 'CANDIDATE' })}>
                  <User size={18} /><span>Candidate</span>
                </button>
                <button type="button" className={`role-option ${form.role === 'RECRUITER' ? 'active' : ''}`}
                  onClick={() => setForm({ ...form, role: 'RECRUITER' })}>
                  <Zap size={18} /><span>Recruiter</span>
                </button>
              </div>
            </div>
            <Button type="submit" fullWidth loading={loading} size="lg">Create Account</Button>
          </form>
          <p className="auth-footer">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
