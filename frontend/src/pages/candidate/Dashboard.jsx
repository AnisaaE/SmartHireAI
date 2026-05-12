import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { applicationsAPI } from '../../api/applications';
import { documentsAPI } from '../../api/documents';
import { Send, FileText, Search, TrendingUp } from 'lucide-react';
import { Link } from 'react-router-dom';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import '../Dashboard.css';

const sv = s => ({ APPLIED:'info', UNDER_REVIEW:'warning', SHORTLISTED:'accent', REJECTED:'danger', HIRED:'success', WITHDRAWN:'default' }[s] || 'default');

export default function CandidateDashboard() {
  const { user } = useAuth();
  const [apps, setApps] = useState([]);
  const [docs, setDocs] = useState([]);

  useEffect(() => {
    (async () => {
      try { const { data } = await applicationsAPI.getByCandidate(user.id); setApps(data || []); } catch {}
      try { const { data } = await documentsAPI.getByOwner(user.id); setDocs(data || []); } catch {}
    })();
  }, [user.id]);

  const shortlisted = apps.filter(a => a.status === 'SHORTLISTED').length;

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">Dashboard</h1><p className="page-subtitle">Welcome back, {user?.username}!</p></div>
      <div className="stat-grid">
        <div className="stat-card"><div className="stat-icon purple"><Send size={20}/></div><div className="stat-info"><div className="stat-label">Applications</div><div className="stat-value">{apps.length}</div></div></div>
        <div className="stat-card"><div className="stat-icon teal"><TrendingUp size={20}/></div><div className="stat-info"><div className="stat-label">Shortlisted</div><div className="stat-value">{shortlisted}</div></div></div>
        <div className="stat-card"><div className="stat-icon blue"><FileText size={20}/></div><div className="stat-info"><div className="stat-label">CVs Uploaded</div><div className="stat-value">{docs.length}</div></div></div>
      </div>
      <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
        <Link to="/candidate/jobs"><Button icon={Search}>Browse Jobs</Button></Link>
        <Link to="/candidate/documents"><Button variant="secondary" icon={FileText}>Manage CVs</Button></Link>
      </div>
      <h2 className="section-title">Recent Applications</h2>
      {apps.length > 0 ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {apps.slice(0, 5).map(a => (
            <div className="app-card" key={a.id}>
              <div className="app-card-info"><div className="app-card-name">Job #{a.jobId}</div><div className="app-card-sub">{a.appliedAt ? new Date(a.appliedAt).toLocaleDateString() : ''}</div></div>
              <Badge variant={sv(a.status)} dot size="sm">{a.status?.replace('_',' ')}</Badge>
            </div>
          ))}
        </div>
      ) : <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 32 }}>No applications yet. <Link to="/candidate/jobs">Browse jobs</Link> to apply.</p>}
    </div>
  );
}
