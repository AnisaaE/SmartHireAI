import './Card.css';

export default function Card({ children, className = '', hover = false, glow = false, padding = 'md', ...props }) {
  return (
    <div className={`card card-p-${padding} ${hover ? 'card-hover' : ''} ${glow ? 'card-glow' : ''} ${className}`} {...props}>
      {children}
    </div>
  );
}

export function CardHeader({ children, className = '' }) {
  return <div className={`card-header ${className}`}>{children}</div>;
}

export function CardBody({ children, className = '' }) {
  return <div className={`card-body ${className}`}>{children}</div>;
}
