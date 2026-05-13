import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { documentsAPI } from '../../api/documents';
import { FileText, Upload, RefreshCw, Trash2, Eye, FolderOpen } from 'lucide-react';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import FileUpload from '../../components/common/FileUpload';
import EmptyState from '../../components/common/EmptyState';
import { PageLoader } from '../../components/common/Spinner';
import toast from 'react-hot-toast';
import '../Dashboard.css';

export default function RecruiterDocuments() {
  const { user } = useAuth();
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [previewDoc, setPreviewDoc] = useState(null);

  const load = async () => {
    setLoading(true);
    try { const { data } = await documentsAPI.getByOwner(user.id); setDocs(data || []); } catch {}
    setLoading(false);
  };
  useEffect(() => { load(); }, [user.id]);

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    try { await documentsAPI.upload(file, user.id, 'JOB', file.name); toast.success('Uploaded!'); setShowUpload(false); setFile(null); load(); }
    catch { toast.error('Upload failed'); }
    setUploading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this document?')) return;
    try { await documentsAPI.remove(id); toast.success('Deleted'); load(); } catch { toast.error('Failed'); }
  };

  const handleReprocess = async (id) => {
    try { await documentsAPI.reprocess(id); toast.success('Reprocessing started'); } catch { toast.error('Failed'); }
  };

  const handlePreview = async (id) => {
    try { const { data } = await documentsAPI.getContent(id); setPreviewDoc(data); } catch { toast.error('Could not load content'); }
  };

  if (loading) return <PageLoader />;

  return (
    <div className="animate-fade">
      <div className="page-header"><div className="page-header-row">
        <div><h1 className="page-title">Documents</h1><p className="page-subtitle">{docs.length} documents</p></div>
        <Button icon={Upload} onClick={() => setShowUpload(true)}>Upload</Button>
      </div></div>

      {docs.length > 0 ? (
        <div className="doc-cards">
          {docs.map(doc => (
            <div className="doc-card" key={doc.id}>
              <div className="doc-card-icon"><FileText size={22} /></div>
              <div className="doc-card-title">{doc.title || doc.fileName}</div>
              <div className="doc-card-filename">{doc.type} • {doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : ''}</div>
              <div className="doc-card-actions">
                <Button variant="ghost" size="sm" icon={Eye} onClick={() => handlePreview(doc.id)}>View</Button>
                <Button variant="ghost" size="sm" icon={RefreshCw} onClick={() => handleReprocess(doc.id)} />
                <Button variant="ghost" size="sm" icon={Trash2} onClick={() => handleDelete(doc.id)} />
              </div>
            </div>
          ))}
        </div>
      ) : <EmptyState icon={FolderOpen} title="No documents" description="Upload job description documents." action={<Button icon={Upload} onClick={() => setShowUpload(true)}>Upload</Button>} />}

      <Modal open={showUpload} onClose={() => { setShowUpload(false); setFile(null); }} title="Upload Document">
        <FileUpload file={file} onFile={setFile} onRemove={() => setFile(null)} />
        <div className="form-actions"><Button variant="secondary" onClick={() => setShowUpload(false)}>Cancel</Button><Button onClick={handleUpload} loading={uploading} disabled={!file}>Upload</Button></div>
      </Modal>

      <Modal open={!!previewDoc} onClose={() => setPreviewDoc(null)} title="Document Content" size="lg">
        <pre style={{ whiteSpace: 'pre-wrap', fontSize: '0.8125rem', color: 'var(--text-secondary)', maxHeight: 400, overflow: 'auto', background: 'var(--bg-tertiary)', padding: 16, borderRadius: 'var(--radius-md)' }}>
          {typeof previewDoc === 'string' ? previewDoc : previewDoc?.rawTextContent || 'No content available.'}
        </pre>
      </Modal>
    </div>
  );
}
