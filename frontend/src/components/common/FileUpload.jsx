import { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, X } from 'lucide-react';
import './FileUpload.css';

export default function FileUpload({ onFile, file, onRemove, accept = { 'application/pdf': ['.pdf'] }, label = 'Drop your PDF here or click to browse' }) {
  const onDrop = useCallback((accepted) => { if (accepted.length) onFile(accepted[0]); }, [onFile]);
  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop, accept, maxFiles: 1 });

  if (file) {
    return (
      <div className="file-preview">
        <FileText size={20} className="file-preview-icon" />
        <div className="file-preview-info">
          <span className="file-preview-name">{file.name}</span>
          <span className="file-preview-size">{(file.size / 1024).toFixed(1)} KB</span>
        </div>
        <button className="file-preview-remove" onClick={onRemove}><X size={16} /></button>
      </div>
    );
  }

  return (
    <div {...getRootProps()} className={`file-drop ${isDragActive ? 'file-drop-active' : ''}`}>
      <input {...getInputProps()} />
      <Upload size={28} className="file-drop-icon" />
      <p className="file-drop-label">{label}</p>
      <span className="file-drop-hint">PDF files only, up to 10MB</span>
    </div>
  );
}
