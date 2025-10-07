'use client';

import { useAuth } from '../hooks/useAuth';
import { logout, isAdmin, isModerator } from '../auth';
import { useRouter } from 'next/navigation';

export default function Navigation() {
  const { user, loading } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    await logout();
    router.push('/');
  };

  if (loading) {
    return (
      <nav className="nav">
        <div className="container">
          <div className="nav-brand">KikuyuHub</div>
          <div className="spinner"></div>
        </div>
      </nav>
    );
  }

  return (
    <nav className="nav">
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <a href="/" className="nav-brand">
          🌍 KikuyuHub
        </a>
        
        <div className="nav-links">
          {!user ? (
            <>
              <a href="/login" className="nav-link">Login</a>
              <a href="/signup" className="nav-link">Sign Up</a>
            </>
          ) : (
            <>
              <a href="/dashboard" className="nav-link">Dashboard</a>
              <a href="/contributions" className="nav-link">My Contributions</a>
              <a href="/contributions/new" className="nav-link">+ Submit</a>
              
              {isModerator(user) && (
                <a href="/moderator" className="nav-link">
                  ⚖️ Moderation
                </a>
              )}
              
              {isAdmin(user) && (
                <a href="/admin" className="nav-link">
                  ⚙️ Admin
                </a>
              )}
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span className={`badge ${
                  user.role === 'admin' ? 'badge-rejected' : 
                  user.role === 'moderator' ? 'badge-pending' : 
                  'badge-approved'
                }`}>
                  {user.role}
                </span>
                <span className="nav-link">{user.email}</span>
                <button 
                  onClick={handleLogout}
                  className="btn btn-secondary"
                  style={{ padding: '0.5rem 0.75rem', fontSize: '0.8rem' }}
                >
                  Logout
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}