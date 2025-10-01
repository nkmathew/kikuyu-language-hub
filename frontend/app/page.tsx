'use client';

import { useEffect, useState } from 'react';
import { isAuthenticated } from '../lib/auth';
import PWAInstaller from '../lib/components/PWAInstaller';
import Navigation from '../lib/components/Navigation';

export default function HomePage() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setAuthenticated(isAuthenticated());
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <div>
        <Navigation />
        <main className="container" style={{ padding: '2rem 0', textAlign: 'center' }}>
          <div className="spinner" style={{ margin: '0 auto' }}></div>
          <p style={{ marginTop: '1rem' }}>Loading...</p>
        </main>
      </div>
    );
  }

  return (
    <div>
      <Navigation />
      <main className="container" style={{ padding: '2rem 0' }}>
        <header style={{ textAlign: 'center', marginBottom: '4rem' }}>
          <h1 style={{ 
            fontSize: '3rem', 
            marginBottom: '1rem', 
            background: 'linear-gradient(135deg, var(--primary), #059669)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            fontWeight: 700
          }}>
            Kikuyu Language Hub
          </h1>
          <p style={{ 
            fontSize: '1.25rem', 
            color: 'var(--muted-foreground)',
            maxWidth: '600px',
            margin: '0 auto'
          }}>
            Collaborative Kikuyu-English Translation Platform
          </p>
        </header>

        <div className="grid grid-auto-fit" style={{ marginBottom: '4rem' }}>
          <div className="card">
            <h3 style={{ marginBottom: '1rem', color: 'var(--primary)' }}>
              🌍 Contribute Translations
            </h3>
            <p style={{ color: 'var(--muted-foreground)' }}>
              Help expand the Kikuyu-English translation database by contributing new translations.
            </p>
          </div>

          <div className="card">
            <h3 style={{ marginBottom: '1rem', color: 'var(--primary)' }}>
              ✅ Quality Review
            </h3>
            <p style={{ color: 'var(--muted-foreground)' }}>
              Moderators review and approve translations to ensure accuracy and quality.
            </p>
          </div>

          <div className="card">
            <h3 style={{ marginBottom: '1rem', color: 'var(--primary)' }}>
              📱 Mobile Ready
            </h3>
            <p style={{ color: 'var(--muted-foreground)' }}>
              Progressive Web App that works seamlessly on mobile devices.
            </p>
          </div>
        </div>

        <div style={{ textAlign: 'center' }}>
          {authenticated ? (
            <div>
              <p style={{ marginBottom: '2rem', fontSize: '1.1rem' }}>Welcome back!</p>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                <a href="/dashboard" className="btn btn-primary">
                  📊 Go to Dashboard
                </a>
                <a href="/contributions" className="btn btn-secondary">
                  📋 View Contributions
                </a>
                <a href="/contributions/new" className="btn btn-primary">
                  ✏️ Submit Translation
                </a>
              </div>
            </div>
          ) : (
            <div>
              <p style={{ marginBottom: '2rem', fontSize: '1.1rem' }}>Get started today</p>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                <a href="/login" className="btn btn-primary">
                  🔑 Login
                </a>
                <a href="/signup" className="btn btn-secondary">
                  📝 Sign up
                </a>
              </div>
            </div>
          )}
        </div>

        <footer style={{ 
          marginTop: '6rem', 
          paddingTop: '2rem', 
          borderTop: '1px solid var(--border)',
          textAlign: 'center',
          color: 'var(--muted-foreground)'
        }}>
          <div style={{ marginBottom: '2rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem' }}>
            <div>
              <h4 style={{ marginBottom: '0.5rem', color: 'var(--foreground)' }}>Platform</h4>
              <p><strong>Backend:</strong> FastAPI + PostgreSQL</p>
              <p><strong>Frontend:</strong> Next.js + TypeScript</p>
            </div>
            <div>
              <h4 style={{ marginBottom: '0.5rem', color: 'var(--foreground)' }}>Features</h4>
              <p>Role-based access control</p>
              <p>Real-time collaboration</p>
            </div>
            <div>
              <h4 style={{ marginBottom: '0.5rem', color: 'var(--foreground)' }}>Technology</h4>
              <p>Progressive Web App</p>
              <p>Offline functionality</p>
            </div>
          </div>
          <p style={{ fontSize: '0.9rem', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
            🌍 Building the future of Kikuyu language preservation
          </p>
        </footer>
      </main>
      <PWAInstaller />
    </div>
  );
}


