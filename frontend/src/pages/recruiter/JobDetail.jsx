import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { jobsAPI } from '../../api/jobs';
import { applicationsAPI } from '../../api/applications';
import { analysisAPI } from '../../api/analysis';
import { ArrowLeft, Edit2, Trash2, Brain, MapPin, Briefcase, Clock, User } from 'lucide-react';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

const statusVariant = (s) => ({ OPEN:'success', DRAFT:'default', CLOSED:'danger', ARCHIVED:'warning', APPLIED:'info', UNDER_REVIEW:'warning', SHORTLISTED:'accent', REJECTED:'danger', HIRED:'success', WITHDRAWN:'default' }[s] || 'default');
const defaultAnalysisConfiguration = {
  scoringWeights: {
    skills: 0.4,
    experience: 0.35,
    education: 0.15,
    keywords: 0.1,
  },
  evaluationCriteria: ['skills', 'experience', 'education', 'keywords'],
};

export default function JobDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const [job, setJob] = useState(null);
  const [apps, setApps] = useState([]);
  const [tab, setTab] = useState('details');
  const [loading, setLoading] = useState(true);
  const [editModal, setEditModal] = useState(false);
  const [form, setForm] = useState({});
  const [analysisLoading, setAnalysisLoading] = useState(false);
  const [report, setReport] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const { data } = await jobsAPI.getById(id);
        setJob(data); setForm({ title: data.title, description: data.description, location: data.location, employmentType: data.employmentType });
        try { const { data: a } = await applicationsAPI.getByJob(id); setApps(a || []); } catch {}
        try { const { data: r } = await analysisAPI.getReport(id); setReport(r); } catch {}
      } catch { toast.error('Job not found'); nav('/recruiter/jobs'); }
      setLoading(false);
    };
    load();
  }, [id]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try { await jobsAPI.update(id, form); setJob({ ...job, ...form }); setEditModal(false); toast.success('Updated!'); } catch { toast.error('Update failed'); }
  };

  const handleStatusChange = async (status) => {
    try { await jobsAPI.updateStatus(id, status); setJob({ ...job, status }); toast.success(`Job ${status.toLowerCase()}`); } catch { toast.error('Status update failed'); }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this job?')) return;
    try { await jobsAPI.remove(id); toast.success('Deleted'); nav('/recruiter/jobs'); } catch { toast.error('Delete failed'); }
  };

  const handleStartAnalysis = async () => {
    setAnalysisLoading(true);
    try {
      const applicationDetails = await Promise.all(
        apps.map(async (app) => {
          const { data } = await applicationsAPI.getById(app.id);
          return {
            applicationId: data.id,
            candidateId: data.candidateId,
            cvDocumentId: data.cvDocumentId,
            candidateLabel: `Candidate #${data.candidateId}`,
          };
        })
      );

      const payload = {
        jobId: String(id),
        jobTitle: job.title || '',
        jobDescription: job.description || '',
        applications: applicationDetails,
        configuration: defaultAnalysisConfiguration,
      };

      const { data } = await analysisAPI.start(payload);
      toast.success('Analysis started!');
      setReport(data);
    } catch (e) { toast.error(e.response?.data?.message || 'Analysis failed'); }
    setAnalysisLoading(false);
  };

  const handleAppStatus = async (appId, status) => {
    try { await applicationsAPI.updateStatus(appId, status); setApps(apps.map(a => a.id === appId ? { ...a, status } : a)); toast.success('Status updated'); } catch { toast.error('Failed'); }
  };

  if (loading) return <PageLoader />;
  if (!job) return null;

  return (
    <div className="animate-fade">
      <div style={{ marginBottom: 20 }}><Button variant="ghost" size="sm" icon={ArrowLeft} onClick={() => nav('/recruiter/jobs')}>Back to Jobs</Button></div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <h1 className="page-title">{job.title}</h1>
              <Badge variant={statusVariant(job.status)} dot>{job.status}</Badge>
            </div>
            <div className="job-card-meta" style={{ marginTop: 8 }}>
              {job.location && <span><MapPin size={14} />{job.location}</span>}
              <span><Briefcase size={14} />{job.employmentType?.replace('_',' ')}</span>
              <span><Clock size={14} />{job.createdAt ? new Date(job.createdAt).toLocaleDateString() : ''}</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <Button variant="secondary" size="sm" icon={Edit2} onClick={() => setEditModal(true)}>Edit</Button>
            <Button variant="danger" size="sm" icon={Trash2} onClick={handleDelete}>Delete</Button>
          </div>
        </div>
      </div>

      {/* Status Actions */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
        {job.status !== 'OPEN' && <Button variant="outline" size="sm" onClick={() => handleStatusChange('OPEN')}>Publish</Button>}
        {job.status !== 'CLOSED' && <Button variant="outline" size="sm" onClick={() => handleStatusChange('CLOSED')}>Close</Button>}
        {job.status !== 'ARCHIVED' && <Button variant="outline" size="sm" onClick={() => handleStatusChange('ARCHIVED')}>Archive</Button>}
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'details' ? 'active' : ''}`} onClick={() => setTab('details')}>Details</button>
        <button className={`tab ${tab === 'applications' ? 'active' : ''}`} onClick={() => setTab('applications')}>Applications ({apps.length})</button>
        <button className={`tab ${tab === 'analysis' ? 'active' : ''}`} onClick={() => setTab('analysis')}>AI Analysis</button>
      </div>

      {tab === 'details' && (
        <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 24 }}>
          <h3 style={{ fontWeight: 600, marginBottom: 12 }}>Job Description</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: 1.7, whiteSpace: 'pre-wrap' }}>{job.description || 'No description provided.'}</p>
        </div>
      )}

      {tab === 'applications' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {apps.length > 0 ? apps.map(app => (
            <div className="app-card" key={app.id}>
              <div className="app-card-avatar"><User size={18} /></div>
              <div className="app-card-info">
                <div className="app-card-name">Candidate #{app.candidateId}</div>
                <div className="app-card-sub">Applied {app.appliedAt ? new Date(app.appliedAt).toLocaleDateString() : '—'}</div>
              </div>
              <Badge variant={statusVariant(app.status)} dot size="sm">{app.status}</Badge>
              <div className="app-card-actions">
                <Button variant="ghost" size="sm" onClick={() => handleAppStatus(app.id, 'SHORTLISTED')}>Shortlist</Button>
                <Button variant="ghost" size="sm" onClick={() => handleAppStatus(app.id, 'REJECTED')}>Reject</Button>
              </div>
            </div>
          )) : <p style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>No applications yet.</p>}
        </div>
      )}

      {tab === 'analysis' && (
        <div>
          <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
            <Button icon={Brain} loading={analysisLoading} onClick={handleStartAnalysis}>Start AI Analysis</Button>
          </div>
          {report ? (
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', padding: 24 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}><h3 style={{ fontWeight: 600 }}>Analysis Report</h3><Badge variant={statusVariant(report.status)} dot>{report.status}</Badge></div>
              {report.summary && <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>{report.summary}</p>}
              {report.applicationScores && Object.entries(report.applicationScores).sort((a, b) => b[1] - a[1]).map(([appId, score]) => (
                <div key={appId} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 0', borderBottom: '1px solid var(--border)' }}>
                  <span style={{ fontWeight: 600, minWidth: 120 }}>App #{appId}</span>
                  <div style={{ flex: 1, height: 8, background: 'var(--bg-tertiary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                    <div style={{ width: `${score}%`, height: '100%', background: 'var(--gradient-primary)', borderRadius: 'var(--radius-full)' }} />
                  </div>
                  <span style={{ fontWeight: 700, color: 'var(--accent)', minWidth: 50, textAlign: 'right' }}>{score}%</span>
                </div>
              ))}
            </div>
          ) : <p style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>No analysis results. Start an analysis to rank candidates.</p>}
        </div>
      )}

      <Modal open={editModal} onClose={() => setEditModal(false)} title="Edit Job">
        <form onSubmit={handleUpdate}>
          <div className="form-grid">
            <div className="full-width"><Input label="Title" value={form.title || ''} onChange={e => setForm({ ...form, title: e.target.value })} /></div>
            <Input label="Location" value={form.location || ''} onChange={e => setForm({ ...form, location: e.target.value })} />
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}><label className="input-label">Type</label><select className="select-field" value={form.employmentType || ''} onChange={e => setForm({ ...form, employmentType: e.target.value })}><option value="FULL_TIME">Full Time</option><option value="PART_TIME">Part Time</option><option value="CONTRACT">Contract</option><option value="INTERNSHIP">Internship</option></select></div>
            <div className="full-width" style={{ display: 'flex', flexDirection: 'column', gap: 6 }}><label className="input-label">Description</label><textarea className="textarea-field" rows={5} value={form.description || ''} onChange={e => setForm({ ...form, description: e.target.value })} /></div>
          </div>
          <div className="form-actions"><Button variant="secondary" type="button" onClick={() => setEditModal(false)}>Cancel</Button><Button type="submit">Save Changes</Button></div>
        </form>
      </Modal>
    </div>
  );
}
