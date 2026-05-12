import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import './Input.css';

export default function Input({ label, error, icon: Icon, type = 'text', className = '', ...props }) {
  const [showPw, setShowPw] = useState(false);
  const isPassword = type === 'password';
  return (
    <div className={`input-group ${error ? 'input-error' : ''} ${className}`}>
      {label && <label className="input-label">{label}</label>}
      <div className="input-wrapper">
        {Icon && <Icon size={18} className="input-icon" />}
        <input
          type={isPassword && showPw ? 'text' : type}
          className={`input-field ${Icon ? 'has-icon' : ''}`}
          {...props}
        />
        {isPassword && (
          <button type="button" className="input-toggle" onClick={() => setShowPw(!showPw)}>
            {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        )}
      </div>
      {error && <span className="input-error-text">{error}</span>}
    </div>
  );
}
