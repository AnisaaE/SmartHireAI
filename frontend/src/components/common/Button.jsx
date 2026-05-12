import './Button.css';

export default function Button({ children, variant = 'primary', size = 'md', icon: Icon, loading, disabled, fullWidth, className = '', ...props }) {
  return (
    <button
      className={`btn btn-${variant} btn-${size} ${fullWidth ? 'btn-full' : ''} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? <span className="btn-spinner" /> : Icon ? <Icon size={size === 'sm' ? 14 : 18} /> : null}
      {children && <span>{children}</span>}
    </button>
  );
}
