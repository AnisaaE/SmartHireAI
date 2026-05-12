import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User, Zap } from 'lucide-react';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import toast from 'react-hot-toast';
import './Auth.css';

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '', role: 'CANDIDATE' });
  const [errors, setErrors] = useState({});
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const validate = () => {
    const e = {};
    if (!form.username || form.username.length < 3) e.username = 'Username must be at least 3 characters';
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Valid email is required';
    if (!form.password || form.password.length < 6) e.password = 'Password must be at least 6 characters';
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
      <div className="auth-bg-effects">
        <div className="auth-orb auth-orb-1" />
        <div className="auth-orb auth-orb-2" />
        <div className="auth-orb auth-orb-3" />
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
          <Input label="Password" type="password" placeholder="Min. 6 characters" icon={Lock}
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
  );
}
