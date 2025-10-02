'use client';

import { useAuth } from '../lib/hooks/useAuth';
import { logout, isAdmin, isModerator } from '../lib/auth';
import { useState } from 'react';

interface NavigationProps {
  currentPath: string;
  onNavigate: (path: string) => void;
}

export default function Navigation({ currentPath, onNavigate }: NavigationProps) {
  const { user } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    onNavigate('/');
  };

  const isActive = (path: string) => currentPath === path;

  return (
    <nav className="nav" style={{ 
      position: 'sticky', 
      top: 0, 
      zIndex: 100,
      backgroundColor: 'var(--card)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <div 
          className="nav-brand" 
          style={{ cursor: 'pointer' }}
          onClick={() => onNavigate('/')}
        >
          🌍 Kikuyu Language Hub
        </div>
        
        {/* Mobile menu toggle */}
        <button
          className="btn btn-secondary"
          style={{
            marginLeft: '1rem',
            padding: '0.5rem',
            display: 'none',
            border: 'none',
            background: 'none',
            color: 'var(--foreground)'
          }}
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        >
          ☰
        </button>
      </div>

      {/* Desktop navigation */}
      <div className="nav-links" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {user ? (
          <>
            <a
              href="#"
              className={`nav-link ${isActive('/dashboard') ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onNavigate('/dashboard');
              }}
              style={{ color: 'var(--muted-foreground)', textDecoration: 'none' }}
            >
              Dashboard
            </a>
            
            <a
              href="#"
              className={`nav-link ${isActive('/contributions') ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onNavigate('/contributions');
              }}
              style={{ color: 'var(--muted-foreground)', textDecoration: 'none' }}
            >
              My Contributions
            </a>

            <a
              href="#"
              className={`nav-link ${isActive('/contributions/new') ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onNavigate('/contributions/new');
              }}
              style={{ color: 'var(--muted-foreground)', textDecoration: 'none' }}
            >
              Submit Translation
            </a>

            {isModerator(user) && (
              <a
                href="#"
                className={`nav-link ${isActive('/moderator') ? 'active' : ''}`}
                onClick={(e) => {
                  e.preventDefault();
                  onNavigate('/moderator');
                }}
                style={{ color: 'var(--accent)', textDecoration: 'none' }}
              >
                📋 Review Queue
              </a>
            )}

            {isAdmin(user) && (
              <>
                <a
                  href="#"
                  className={`nav-link ${isActive('/admin/users') ? 'active' : ''}`}
                  onClick={(e) => {
                    e.preventDefault();
                    onNavigate('/admin/users');
                  }}
                  style={{ color: 'var(--destructive)', textDecoration: 'none' }}
                >
                  👥 Users
                </a>
                <a
                  href="#"
                  className={`nav-link ${isActive('/admin/audit') ? 'active' : ''}`}
                  onClick={(e) => {
                    e.preventDefault();
                    onNavigate('/admin/audit');
                  }}
                  style={{ color: 'var(--destructive)', textDecoration: 'none' }}
                >
                  📋 Audit
                </a>
              </>
            )}

            <span className={`badge ${
              user.role === 'admin' ? 'badge-rejected' : 
              user.role === 'moderator' ? 'badge-pending' : 
              'badge-approved'
            }`} style={{ marginLeft: '1rem' }}>
              {user.role}
            </span>

            <button
              onClick={handleLogout}
              className="btn btn-destructive"
              style={{ marginLeft: '1rem' }}
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <a
              href="#"
              className={`nav-link ${isActive('/login') ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onNavigate('/login');
              }}
              style={{ color: 'var(--muted-foreground)', textDecoration: 'none' }}
            >
              Login
            </a>
            <a
              href="#"
              className={`nav-link ${isActive('/signup') ? 'active' : ''}`}
              onClick={(e) => {
                e.preventDefault();
                onNavigate('/signup');
              }}
              style={{ color: 'var(--muted-foreground)', textDecoration: 'none' }}
            >
              Sign Up
            </a>
          </>
        )}
      </div>

      {/* Mobile navigation */}
      {isMobileMenuOpen && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          backgroundColor: 'var(--card)',
          border: '1px solid var(--border)',
          borderTop: 'none',
          padding: '1rem',
          display: 'none'
        }}>
          {user ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setIsMobileMenuOpen(false);
                  onNavigate('/dashboard');
                }}
                style={{ color: 'var(--foreground)', textDecoration: 'none', padding: '0.5rem 0' }}
              >
                Dashboard
              </a>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setIsMobileMenuOpen(false);
                  onNavigate('/contributions');
                }}
                style={{ color: 'var(--foreground)', textDecoration: 'none', padding: '0.5rem 0' }}
              >
                My Contributions
              </a>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setIsMobileMenuOpen(false);
                  onNavigate('/contributions/new');
                }}
                style={{ color: 'var(--foreground)', textDecoration: 'none', padding: '0.5rem 0' }}
              >
                Submit Translation
              </a>
              {isModerator(user) && (
                <a
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    setIsMobileMenuOpen(false);
                    onNavigate('/moderator');
                  }}
                  style={{ color: 'var(--accent)', textDecoration: 'none', padding: '0.5rem 0' }}
                >
                  Review Queue
                </a>
              )}
              <button
                onClick={handleLogout}
                className="btn btn-destructive"
                style={{ width: '100%', marginTop: '1rem' }}
              >
                Logout
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setIsMobileMenuOpen(false);
                  onNavigate('/login');
                }}
                style={{ color: 'var(--foreground)', textDecoration: 'none', padding: '0.5rem 0' }}
              >
                Login
              </a>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setIsMobileMenuOpen(false);
                  onNavigate('/signup');
                }}
                style={{ color: 'var(--foreground)', textDecoration: 'none', padding: '0.5rem 0' }}
              >
                Sign Up
              </a>
            </div>
          )}
        </div>
      )}
    </nav>
  );
}