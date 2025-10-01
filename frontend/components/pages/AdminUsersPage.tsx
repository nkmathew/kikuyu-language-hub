'use client';

import { useAuth } from '../../hooks/useAuth';
import { isAdmin } from '../../lib/auth';

interface AdminUsersPageProps {
  onNavigate: (path: string) => void;
}

export default function AdminUsersPage({ onNavigate }: AdminUsersPageProps) {
  const { user } = useAuth();

  if (!user || !isAdmin(user)) {
    return (
      <div className="container" style={{ padding: '2rem', textAlign: 'center' }}>
        <h1 style={{ color: 'var(--destructive)' }}>Access Denied</h1>
        <p style={{ color: 'var(--muted-foreground)' }}>Admin access required.</p>
        <button 
          onClick={() => onNavigate('/dashboard')}
          style={{ 
            background: 'none', 
            border: 'none', 
            color: 'var(--primary)', 
            textDecoration: 'underline',
            cursor: 'pointer'
          }}
        >
          ← Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <h1 style={{ marginBottom: '2rem', color: 'var(--destructive)' }}>👥 User Management</h1>
      
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>🚧 Under Development</h3>
        <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
          User management interface is coming soon. This will include user listings, role management, and activity monitoring.
        </p>
        <button 
          onClick={() => onNavigate('/dashboard')}
          className="btn btn-secondary"
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}