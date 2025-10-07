'use client';

import { useAuth } from '../../lib/hooks/useAuth';
import { logout, isAdmin, isModerator } from '../../lib/auth';

interface DashboardPageProps {
  onNavigate: (path: string) => void;
}

export default function DashboardPage({ onNavigate }: DashboardPageProps) {
  const { user, loading } = useAuth();

  const handleLogout = async () => {
    await logout();
    onNavigate('/');
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '2rem' }}>
        <p style={{ color: 'var(--foreground)' }}>Loading...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <header style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid var(--border)'
      }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Dashboard</h1>
          <p style={{ color: 'var(--muted-foreground)' }}>Welcome back, {user.email}</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <span className={`badge ${
            user.role === 'admin' ? 'badge-rejected' : 
            user.role === 'moderator' ? 'badge-pending' : 
            'badge-approved'
          }`}>
            {user.role}
          </span>
          <button
            onClick={handleLogout}
            className="btn btn-destructive"
          >
            Logout
          </button>
        </div>
      </header>

      <div className="grid grid-auto-fit" style={{ 
        gap: '2rem',
        marginBottom: '3rem'
      }}>
        {/* Contributor Features */}
        <div className="card">
          <h3 style={{ marginBottom: '1rem', color: 'var(--primary)' }}>📝 Contribute</h3>
          <p style={{ marginBottom: '1.5rem', color: 'var(--muted-foreground)' }}>
            Submit new Kikuyu-English translations
          </p>
          <button
            onClick={() => onNavigate('/contributions/new')}
            className="btn btn-primary"
          >
            Submit Translation
          </button>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '1rem', color: 'var(--accent-foreground)' }}>📋 My Contributions</h3>
          <p style={{ marginBottom: '1.5rem', color: 'var(--muted-foreground)' }}>
            View and manage your translation submissions
          </p>
          <button
            onClick={() => onNavigate('/contributions')}
            className="btn btn-secondary"
            style={{
              backgroundColor: 'var(--accent)',
              color: 'var(--accent-foreground)'
            }}
          >
            View Contributions
          </button>
        </div>

        {/* Moderator Features */}
        {isModerator(user) && (
          <div className="card" style={{
            backgroundColor: 'var(--muted)',
            borderColor: 'var(--accent)'
          }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--accent-foreground)' }}>⚖️ Moderation</h3>
            <p style={{ marginBottom: '1.5rem', color: 'var(--muted-foreground)' }}>
              Review pending translations
            </p>
            <button
              onClick={() => onNavigate('/moderator')}
              className="btn btn-secondary"
              style={{
                backgroundColor: 'var(--accent)',
                color: 'var(--accent-foreground)'
              }}
            >
              Review Queue
            </button>
          </div>
        )}

        {/* Admin Features */}
        {isAdmin(user) && (
          <div className="card" style={{
            backgroundColor: 'var(--muted)',
            borderColor: 'var(--destructive)'
          }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--destructive)' }}>⚙️ Admin</h3>
            <p style={{ marginBottom: '1.5rem', color: 'var(--muted-foreground)' }}>
              System administration and user management
            </p>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                onClick={() => onNavigate('/admin/users')}
                className="btn btn-destructive"
                style={{ fontSize: '0.9rem', padding: '0.5rem 1rem' }}
              >
                Users
              </button>
              <button
                onClick={() => onNavigate('/admin/audit')}
                className="btn btn-destructive"
                style={{ fontSize: '0.9rem', padding: '0.5rem 1rem' }}
              >
                Audit Log
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Quick Stats */}
      <div className="card" style={{
        backgroundColor: 'var(--muted)'
      }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>📊 Platform Statistics</h3>
        <div className="grid grid-auto-fit" style={{ gap: '1rem' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--primary)' }}>-</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Total Translations</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--accent-foreground)' }}>-</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Pending Review</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--accent)' }}>-</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Active Contributors</div>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <div style={{ 
        marginTop: '2rem', 
        textAlign: 'center',
        paddingTop: '2rem',
        borderTop: '1px solid var(--border)'
      }}>
        <button 
          onClick={() => onNavigate('/')}
          style={{ 
            background: 'none', 
            border: 'none', 
            color: 'var(--muted-foreground)', 
            textDecoration: 'underline',
            cursor: 'pointer'
          }}
        >
          ← Back to Home
        </button>
      </div>
    </div>
  );
}