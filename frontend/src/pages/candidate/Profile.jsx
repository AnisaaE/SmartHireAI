import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { authAPI } from '../../api/auth';
import { User, Mail, Lock } from 'lucide-react';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import toast from 'react-hot-toast';
import '../Dashboard.css';

export default function CandidateProfile() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({ username: user?.username || '', email: user?.email || '' });
  const [pw, setPw] = useState({ current: '', newPassword: '', confirm: '' });
  const [saving, setSaving] = useState(false);
  const [savingPw, setSavingPw] = useState(false);

  const handleProfile = async (e) => {
    e.preventDefault(); setSaving(true);
    try { await authAPI.updateUser(user.id, form); updateUser(form); toast.success('Profile updated'); } catch { toast.error('Failed'); }
    setSaving(false);
  };

  const handlePassword = async (e) => {
    e.preventDefault();
    if (pw.newPassword !== pw.confirm) return toast.error('Passwords don\'t match');
    setSavingPw(true);
    try { await authAPI.changePassword(user.id, { currentPassword: pw.current, newPassword: pw.newPassword }); setPw({ current: '', newPassword: '', confirm: '' }); toast.success('Password changed'); } catch { toast.error('Failed'); }
    setSavingPw(false);
  };

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">Settings</h1><p className="page-subtitle">Manage your account</p></div>
      <div style={{ maxWidth: 560 }}>
        <div className="profile-section">
          <h2 className="profile-section-title">Profile Information</h2>
          <form onSubmit={handleProfile} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Input label="Username" icon={User} value={form.username} onChange={e => setForm({...form, username: e.target.value})} />
            <Input label="Email" icon={Mail} type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
            <Button type="submit" loading={saving} style={{ alignSelf: 'flex-start' }}>Save Changes</Button>
          </form>
        </div>
        <div className="profile-section">
          <h2 className="profile-section-title">Change Password</h2>
          <form onSubmit={handlePassword} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <Input label="Current Password" type="password" icon={Lock} value={pw.current} onChange={e => setPw({...pw, current: e.target.value})} />
            <Input label="New Password" type="password" icon={Lock} value={pw.newPassword} onChange={e => setPw({...pw, newPassword: e.target.value})} />
            <Input label="Confirm" type="password" icon={Lock} value={pw.confirm} onChange={e => setPw({...pw, confirm: e.target.value})} />
            <Button type="submit" loading={savingPw} style={{ alignSelf: 'flex-start' }}>Change Password</Button>
          </form>
        </div>
      </div>
    </div>
  );
}
