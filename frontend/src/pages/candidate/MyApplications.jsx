import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { applicationsAPI } from '../../api/applications';
import { Send, XCircle } from 'lucide-react';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

const sv = s => ({ APPLIED:'info', UNDER_REVIEW:'warning', SHORTLISTED:'accent', REJECTED:'danger', HIRED:'success', WITHDRAWN:'default' }[s] || 'default');

export default function MyApplications() {
  const { user } = useAuth();
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    (async () => {
      try { const { data } = await applicationsAPI.getByCandidate(user.id); setApps(data || []); } catch {}
      setLoading(false);
    })();
  }, [user.id]);

  const withdraw = async (id) => {
    if (!window.confirm('Withdraw this application?')) return;
    try { await applicationsAPI.remove(id); setApps(apps.filter(a => a.id !== id)); toast.success('Withdrawn'); } catch { toast.error('Failed'); }
  };

  const f = filter === 'ALL' ? apps : apps.filter(a => a.status === filter);
  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">My Applications</h1><p className="page-subtitle">{apps.length} applications</p></div>
      <div className="filter-bar">
        {['ALL','APPLIED','UNDER_REVIEW','SHORTLISTED','REJECTED','HIRED'].map(s => <button key={s} className={`filter-chip ${filter===s?'active':''}`} onClick={()=>setFilter(s)}>{s.replace('_',' ')}</button>)}
      </div>
      {f.length > 0 ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {f.map(a => (
            <div className="app-card" key={a.id}>
              <div className="app-card-avatar"><Send size={16} /></div>
              <div className="app-card-info">
                <div className="app-card-name">Job #{a.jobId}</div>
                <div className="app-card-sub">Applied {a.appliedAt ? new Date(a.appliedAt).toLocaleDateString() : '—'}</div>
              </div>
              <Badge variant={sv(a.status)} dot size="sm">{a.status?.replace('_',' ')}</Badge>
              {a.status === 'APPLIED' && <Button variant="ghost" size="sm" icon={XCircle} onClick={() => withdraw(a.id)}>Withdraw</Button>}
            </div>
          ))}
        </div>
      ) : <EmptyState icon={Send} title="No applications" description="You haven't applied to any jobs yet." />}
    </div>
  );
}
