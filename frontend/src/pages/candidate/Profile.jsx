import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { authAPI } from '../../api/auth';
import { User, Mail } from 'lucide-react';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import toast from 'react-hot-toast';
import '../Dashboard.css';

export default function CandidateProfile() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    username: user?.username || '',
    email: user?.email || '',
  });
  const [saving, setSaving] = useState(false);

  const handleProfile = async (e) => {
    e.preventDefault();
    if (!form.username || form.username.length < 3) return toast.error('Username must be at least 3 characters');
    if (!form.email) return toast.error('Email is required');
    setSaving(true);
    try {
      // Backend UpdateUserRequest requires: username, email, role, active
      const { data } = await authAPI.updateUser(user.id, {
        username: form.username,
        email: form.email,
        role: user.role,
        active: user.active !== undefined ? user.active : true,
      });
      updateUser(data);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update');
    }
    setSaving(false);
  };

  return (
    <div className="animate-fade">
      <div className="page-header">
        <h1 className="page-title">Settings</h1>
        <p className="page-subtitle">Manage your account</p>
      </div>
      <div style={{ maxWidth: 560 }}>
        <div className="profile-section">
          <h2 className="profile-section-title">Profile Information</h2>
          <form onSubmit={handleProfile} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Input label="Username" icon={User} value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })} />
            <Input label="Email" icon={Mail} type="email" value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })} />
            <div style={{ padding: '12px 14px', background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-md)', fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
              <strong style={{ color: 'var(--text-primary)' }}>Role:</strong> {user?.role}
            </div>
            <Button type="submit" loading={saving} style={{ alignSelf: 'flex-start' }}>Save Changes</Button>
          </form>
        </div>
      </div>
    </div>
  );
}
