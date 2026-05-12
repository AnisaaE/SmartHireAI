import { Link } from 'react-router-dom';
import Button from '../components/common/Button';
import { Home } from 'lucide-react';
import './NotFound.css';

export default function NotFound() {
  return (
    <div className="notfound-page animate-fade">
      <h1 className="notfound-code">404</h1>
      <p className="notfound-text">The page you're looking for doesn't exist.</p>
      <Link to="/"><Button icon={Home}>Back to Home</Button></Link>
    </div>
  );
}
