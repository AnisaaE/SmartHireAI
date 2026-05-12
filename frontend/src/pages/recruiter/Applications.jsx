import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI } from '../../api/jobs';
import { applicationsAPI } from '../../api/applications';
import { User } from 'lucide-react';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import { PageLoader } from '../../components/common/Spinner';
import EmptyState from '../../components/common/EmptyState';
import toast from 'react-hot-toast';
import '../Dashboard.css';

const sv = (s) => ({ APPLIED:'info', UNDER_REVIEW:'warning', SHORTLISTED:'accent', REJECTED:'danger', HIRED:'success', WITHDRAWN:'default' }[s] || 'default');

export default function RecruiterApplications() {
  const { user } = useAuth();
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    (async () => {
      try {
        const { data: jobs } = await jobsAPI.getByRecruiter(user.id);
        const all = [];
        for (const j of (jobs || [])) {
          try { const { data } = await applicationsAPI.getByJob(j.id); (data||[]).forEach(a => all.push({...a, jobTitle:j.title})); } catch {}
        }
        setApps(all);
      } catch {}
      setLoading(false);
    })();
  }, [user.id]);

  const hs = async (id, st) => {
    try { await applicationsAPI.updateStatus(id, st); setApps(apps.map(a => a.id===id?{...a,status:st}:a)); toast.success('Updated'); } catch { toast.error('Failed'); }
  };

  const f = filter==='ALL'? apps : apps.filter(a=>a.status===filter);
  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">Applications</h1><p className="page-subtitle">{apps.length} total</p></div>
      <div className="filter-bar">
        {['ALL','APPLIED','UNDER_REVIEW','SHORTLISTED','REJECTED','HIRED'].map(s=><button key={s} className={`filter-chip ${filter===s?'active':''}`} onClick={()=>setFilter(s)}>{s.replace('_',' ')}</button>)}
      </div>
      {f.length>0?(
        <div style={{display:'flex',flexDirection:'column',gap:10}}>
          {f.map(a=>(
            <div className="app-card" key={a.id}>
              <div className="app-card-avatar"><User size={18}/></div>
              <div className="app-card-info"><div className="app-card-name">Candidate #{a.candidateId}</div><div className="app-card-sub">{a.jobTitle}</div></div>
              <Badge variant={sv(a.status)} dot size="sm">{a.status?.replace('_',' ')}</Badge>
              <div className="app-card-actions">
                <Button variant="ghost" size="sm" onClick={()=>hs(a.id,'SHORTLISTED')}>Shortlist</Button>
                <Button variant="ghost" size="sm" onClick={()=>hs(a.id,'REJECTED')}>Reject</Button>
              </div>
            </div>
          ))}
        </div>
      ):<EmptyState icon={User} title="No applications" description="Applications will appear here." />}
    </div>
  );
}
