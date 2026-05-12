import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI } from '../../api/jobs';
import { Link } from 'react-router-dom';
import { Plus, MapPin, Clock, Briefcase } from 'lucide-react';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import EmptyState from '../../components/common/EmptyState';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

const statuses = ['ALL', 'DRAFT', 'OPEN', 'CLOSED', 'ARCHIVED'];
const statusVariant = (s) => ({ OPEN: 'success', DRAFT: 'default', CLOSED: 'danger', ARCHIVED: 'warning' }[s] || 'default');

export default function RecruiterJobs() {
  const { user } = useAuth();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', location: '', employmentType: 'FULL_TIME' });
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try { const { data } = await jobsAPI.getByRecruiter(user.id); setJobs(data || []); } catch {}
    setLoading(false);
  };

  useEffect(() => { load(); }, [user.id]);

  const filtered = filter === 'ALL' ? jobs : jobs.filter(j => j.status === filter);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!form.title) return toast.error('Title is required');
    setSaving(true);
    try {
      await jobsAPI.create({ ...form, recruiterId: user.id, status: 'DRAFT' });
      toast.success('Job created!');
      setShowCreate(false);
      setForm({ title: '', description: '', location: '', employmentType: 'FULL_TIME' });
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to create'); }
    setSaving(false);
  };

  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header">
        <div className="page-header-row">
          <div><h1 className="page-title">Job Postings</h1><p className="page-subtitle">{jobs.length} total jobs</p></div>
          <Button icon={Plus} onClick={() => setShowCreate(true)}>New Job</Button>
        </div>
      </div>

      <div className="filter-bar">
        {statuses.map(s => <button key={s} className={`filter-chip ${filter === s ? 'active' : ''}`} onClick={() => setFilter(s)}>{s}</button>)}
      </div>

      {filtered.length > 0 ? (
        <div className="job-cards">
          {filtered.map(job => (
            <Link to={`/recruiter/jobs/${job.id}`} key={job.id} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="job-card">
                <div className="job-card-header">
                  <h3 className="job-card-title">{job.title}</h3>
                  <Badge variant={statusVariant(job.status)} dot size="sm">{job.status}</Badge>
                </div>
                <div className="job-card-meta">
                  {job.location && <span><MapPin size={14} />{job.location}</span>}
                  <span><Briefcase size={14} />{job.employmentType?.replace('_', ' ') || 'Full Time'}</span>
                </div>
                <div className="job-card-footer">
                  <span className="job-card-date"><Clock size={12} style={{ marginRight: 4 }} />{job.createdAt ? new Date(job.createdAt).toLocaleDateString() : '—'}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <EmptyState icon={Briefcase} title="No jobs found" description={filter !== 'ALL' ? 'Try a different filter.' : 'Create your first job posting to get started.'} action={<Button icon={Plus} onClick={() => setShowCreate(true)}>Create Job</Button>} />
      )}

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Create New Job" size="md">
        <form onSubmit={handleCreate}>
          <div className="form-grid">
            <div className="full-width"><Input label="Job Title" placeholder="e.g. Senior Software Engineer" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} /></div>
            <Input label="Location" placeholder="e.g. Istanbul, Remote" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <label className="input-label">Employment Type</label>
              <select className="select-field" value={form.employmentType} onChange={e => setForm({ ...form, employmentType: e.target.value })}>
                <option value="FULL_TIME">Full Time</option><option value="PART_TIME">Part Time</option>
                <option value="CONTRACT">Contract</option><option value="INTERNSHIP">Internship</option>
              </select>
            </div>
            <div className="full-width" style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <label className="input-label">Description</label>
              <textarea className="textarea-field" placeholder="Describe the role, responsibilities, requirements..." rows={5} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
            </div>
          </div>
          <div className="form-actions">
            <Button variant="secondary" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button type="submit" loading={saving}>Create Job</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
