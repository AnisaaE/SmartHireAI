import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { jobsAPI } from '../../api/jobs';
import { applicationsAPI } from '../../api/applications';
import { analysisAPI } from '../../api/analysis';
import { Briefcase, Users, Brain, TrendingUp, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import '../Dashboard.css';

export default function RecruiterDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ jobs: 0, applications: 0, analyses: 0, active: 0 });
  const [recentJobs, setRecentJobs] = useState([]);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        const { data: jobs } = await jobsAPI.getByRecruiter(user.id);
        const recruiterJobs = jobs || [];
        const activeJobs = recruiterJobs.filter(j => j.status === 'OPEN');

        const [applicationResults, analysisResults] = await Promise.all([
          Promise.allSettled(
            recruiterJobs.map(job => applicationsAPI.getByJob(job.id))
          ),
          Promise.allSettled(
            recruiterJobs.map(job => analysisAPI.getReport(job.id))
          ),
        ]);

        const applicationCount = applicationResults.reduce((total, result) => {
          if (result.status !== 'fulfilled') {
            return total;
          }
          return total + (result.value.data?.length || 0);
        }, 0);

        const analysisCount = analysisResults.reduce((total, result) => {
          if (result.status !== 'fulfilled' || !result.value.data?.analysisId) {
            return total;
          }
          return total + 1;
        }, 0);

        if (cancelled) {
          return;
        }

        setRecentJobs(recruiterJobs.slice(0, 5));
        setStats({
          jobs: recruiterJobs.length,
          applications: applicationCount,
          analyses: analysisCount,
          active: activeJobs.length,
        });
      } catch { /* backend may not be running */ }
    };

    load();

    return () => {
      cancelled = true;
    };
  }, [user.id]);

  const statusVariant = (s) => {
    const map = { OPEN: 'success', DRAFT: 'default', CLOSED: 'danger', ARCHIVED: 'warning' };
    return map[s] || 'default';
  };

  return (
    <div className="animate-fade">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">Welcome back, {user?.username}. Here's your overview.</p>
      </div>

      <div className="stat-grid">
        <div className="stat-card"><div className="stat-icon purple"><Briefcase size={20} /></div><div className="stat-info"><div className="stat-label">Total Jobs</div><div className="stat-value">{stats.jobs}</div></div></div>
        <div className="stat-card"><div className="stat-icon teal"><TrendingUp size={20} /></div><div className="stat-info"><div className="stat-label">Active Positions</div><div className="stat-value">{stats.active}</div></div></div>
        <div className="stat-card"><div className="stat-icon blue"><Users size={20} /></div><div className="stat-info"><div className="stat-label">Applications</div><div className="stat-value">{stats.applications}</div></div></div>
        <div className="stat-card"><div className="stat-icon green"><Brain size={20} /></div><div className="stat-info"><div className="stat-label">AI Analyses</div><div className="stat-value">{stats.analyses}</div></div></div>
      </div>

      <div className="page-header-row" style={{ marginBottom: 16 }}>
        <h2 className="section-title" style={{ marginBottom: 0 }}>Recent Jobs</h2>
        <Link to="/recruiter/jobs"><Button variant="ghost" size="sm" icon={ArrowRight}>View All</Button></Link>
      </div>

      {recentJobs.length > 0 ? (
        <div className="data-table" style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead><tr><th style={{ textAlign: 'left', padding: '12px 16px', fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', background: 'var(--bg-tertiary)', borderBottom: '1px solid var(--border)' }}>Title</th><th style={{ textAlign: 'left', padding: '12px 16px', fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', background: 'var(--bg-tertiary)', borderBottom: '1px solid var(--border)' }}>Status</th><th style={{ textAlign: 'left', padding: '12px 16px', fontSize: '0.75rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', background: 'var(--bg-tertiary)', borderBottom: '1px solid var(--border)' }}>Location</th></tr></thead>
            <tbody>
              {recentJobs.map(job => (
                <tr key={job.id} style={{ cursor: 'pointer' }} onClick={() => window.location.href = `/recruiter/jobs/${job.id}`}>
                  <td style={{ padding: '14px 16px', fontSize: '0.875rem', borderBottom: '1px solid var(--border)' }}><span style={{ fontWeight: 600 }}>{job.title}</span></td>
                  <td style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)' }}><Badge variant={statusVariant(job.status)} dot>{job.status}</Badge></td>
                  <td style={{ padding: '14px 16px', fontSize: '0.875rem', color: 'var(--text-secondary)', borderBottom: '1px solid var(--border)' }}>{job.location || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
          <p>No jobs yet. <Link to="/recruiter/jobs">Create your first job posting</Link>.</p>
        </div>
      )}
    </div>
  );
}
