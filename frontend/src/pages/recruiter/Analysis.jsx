import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { analysisAPI } from '../../api/analysis';
import { jobsAPI } from '../../api/jobs';
import { Brain, Play, RefreshCw, Trash2 } from 'lucide-react';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import EmptyState from '../../components/common/EmptyState';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

const sv = (s) => ({ QUEUED:'info', RUNNING:'warning', COMPLETED:'success', FAILED:'danger', CANCELLED:'default' }[s] || 'default');

export default function RecruiterAnalysis() {
  const { user } = useAuth();
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState('');
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [starting, setStarting] = useState(false);

  useEffect(() => {
    (async () => {
      try { const { data } = await jobsAPI.getByRecruiter(user.id); setJobs(data || []); } catch {}
      setLoading(false);
    })();
  }, [user.id]);

  const start = async () => {
    if (!selectedJob) return toast.error('Select a job first');
    setStarting(true);
    try { const { data } = await analysisAPI.start(selectedJob); setReport(data); toast.success('Analysis started!'); } catch (e) { toast.error(e.response?.data?.message || 'Failed'); }
    setStarting(false);
  };

  const loadReport = async (jobId) => {
    try { const { data } = await analysisAPI.getReport(jobId); setReport(data); } catch { setReport(null); }
  };

  useEffect(() => { if (selectedJob) loadReport(selectedJob); }, [selectedJob]);

  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">AI Analysis</h1><p className="page-subtitle">Use AI to rank candidates for your job postings</p></div>

      <div style={{ display: 'flex', gap: 12, marginBottom: 24, alignItems: 'flex-end' }}>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
          <label className="input-label">Select Job</label>
          <select className="select-field" value={selectedJob} onChange={e => setSelectedJob(e.target.value)}>
            <option value="">Choose a job...</option>
            {jobs.map(j => <option key={j.id} value={j.id}>{j.title}</option>)}
          </select>
        </div>
        <Button icon={Play} loading={starting} onClick={start}>Start Analysis</Button>
      </div>

      {report ? (
        <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 24 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <Brain size={22} style={{ color: 'var(--accent)' }} />
              <h3 style={{ fontWeight: 600 }}>Analysis Results</h3>
              <Badge variant={sv(report.status)} dot>{report.status}</Badge>
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              {report.analysisId && <Button variant="ghost" size="sm" icon={RefreshCw} onClick={async () => { try { await analysisAPI.restart(report.analysisId); toast.success('Restarting...'); loadReport(selectedJob); } catch { toast.error('Failed'); } }}>Restart</Button>}
              {report.analysisId && <Button variant="ghost" size="sm" icon={Trash2} onClick={async () => { try { await analysisAPI.remove(report.analysisId); setReport(null); toast.success('Deleted'); } catch { toast.error('Failed'); } }} />}
            </div>
          </div>
          {report.summary && <p style={{ color: 'var(--text-secondary)', marginBottom: 20, lineHeight: 1.6 }}>{report.summary}</p>}
          {report.applicationScores && Object.entries(report.applicationScores).sort((a,b)=>b[1]-a[1]).map(([appId, score], i) => (
            <div key={appId} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '14px 0', borderBottom: '1px solid var(--border)' }}>
              <span style={{ width: 28, height: 28, borderRadius: '50%', background: i===0?'var(--gradient-primary)':'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.75rem', fontWeight: 700, color: i===0?'#fff':'var(--text-secondary)' }}>{i+1}</span>
              <span style={{ fontWeight: 600, flex: 1 }}>Application #{appId}</span>
              <div style={{ width: 200, height: 8, background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                <div style={{ width: `${score}%`, height: '100%', background: 'var(--gradient-primary)', borderRadius: 'var(--radius-full)', transition: 'width 0.5s ease' }} />
              </div>
              <span style={{ fontWeight: 700, color: 'var(--accent)', minWidth: 50, textAlign: 'right' }}>{score}%</span>
            </div>
          ))}
          {report.applicationReasoning && Object.entries(report.applicationReasoning).map(([appId, reason]) => (
            <div key={appId} style={{ marginTop: 16, padding: 14, background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontWeight: 600, fontSize: '0.8125rem', marginBottom: 6 }}>Reasoning — App #{appId}</div>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>{reason}</p>
            </div>
          ))}
        </div>
      ) : selectedJob ? (
        <EmptyState icon={Brain} title="No analysis yet" description="Start an AI analysis to rank candidates for this job." />
      ) : (
        <EmptyState icon={Brain} title="Select a Job" description="Choose a job posting above to analyze candidates." />
      )}
    </div>
  );
}
