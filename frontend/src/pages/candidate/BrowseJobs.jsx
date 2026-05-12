import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI } from '../../api/jobs';
import { applicationsAPI } from '../../api/applications';
import { documentsAPI } from '../../api/documents';
import { MapPin, Briefcase, Clock, Search, Send } from 'lucide-react';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import EmptyState from '../../components/common/EmptyState';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

export default function BrowseJobs() {
  const { user } = useAuth();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [applyModal, setApplyModal] = useState(null);
  const [cvs, setCvs] = useState([]);
  const [selectedCv, setSelectedCv] = useState('');
  const [applying, setApplying] = useState(false);

  useEffect(() => {
    (async () => {
      try { const { data } = await jobsAPI.getAll(); setJobs((data || []).filter(j => j.status === 'OPEN')); } catch {}
      try { const { data } = await documentsAPI.getByOwner(user.id); setCvs((data || []).filter(d => d.type === 'CV')); } catch {}
      setLoading(false);
    })();
  }, [user.id]);

  const handleApply = async () => {
    if (!selectedCv) return toast.error('Please select a CV');
    setApplying(true);
    try {
      await applicationsAPI.apply({ jobId: applyModal.id, candidateId: user.id, cvDocumentId: selectedCv });
      toast.success('Application submitted!');
      setApplyModal(null); setSelectedCv('');
    } catch (e) { toast.error(e.response?.data?.message || 'Failed to apply'); }
    setApplying(false);
  };

  const filtered = jobs.filter(j => !search || j.title?.toLowerCase().includes(search.toLowerCase()) || j.location?.toLowerCase().includes(search.toLowerCase()));

  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header"><h1 className="page-title">Browse Jobs</h1><p className="page-subtitle">{jobs.length} open positions</p></div>
      <div style={{ marginBottom: 20, maxWidth: 400 }}>
        <Input placeholder="Search by title or location..." icon={Search} value={search} onChange={e => setSearch(e.target.value)} />
      </div>
      {filtered.length > 0 ? (
        <div className="job-cards">
          {filtered.map(job => (
            <div className="job-card" key={job.id}>
              <div className="job-card-header"><h3 className="job-card-title">{job.title}</h3><Badge variant="success" dot size="sm">OPEN</Badge></div>
              {job.description && <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: 12, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{job.description}</p>}
              <div className="job-card-meta">
                {job.location && <span><MapPin size={14} />{job.location}</span>}
                <span><Briefcase size={14} />{job.employmentType?.replace('_', ' ') || 'Full Time'}</span>
              </div>
              <div className="job-card-footer">
                <span className="job-card-date"><Clock size={12} />{job.createdAt ? new Date(job.createdAt).toLocaleDateString() : ''}</span>
                <Button size="sm" icon={Send} onClick={() => setApplyModal(job)}>Apply</Button>
              </div>
            </div>
          ))}
        </div>
      ) : <EmptyState icon={Search} title="No jobs found" description="No open positions match your search." />}

      <Modal open={!!applyModal} onClose={() => setApplyModal(null)} title={`Apply to ${applyModal?.title}`} size="sm">
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: 16 }}>Select a CV to submit with your application.</p>
        {cvs.length > 0 ? (
          <>
            <select className="select-field" value={selectedCv} onChange={e => setSelectedCv(e.target.value)} style={{ marginBottom: 20 }}>
              <option value="">Choose a CV...</option>
              {cvs.map(cv => <option key={cv.id} value={cv.id}>{cv.title || cv.fileName}</option>)}
            </select>
            <div className="form-actions">
              <Button variant="secondary" onClick={() => setApplyModal(null)}>Cancel</Button>
              <Button loading={applying} onClick={handleApply}>Submit Application</Button>
            </div>
          </>
        ) : <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 16 }}>No CVs uploaded. Please upload a CV first.</p>}
      </Modal>
    </div>
  );
}
