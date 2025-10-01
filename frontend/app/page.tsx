'use client';

import { useEffect, useState } from 'react';
import { isAuthenticated } from '../lib/auth';
import PWAInstaller from '../lib/components/PWAInstaller';

export default function HomePage() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setAuthenticated(isAuthenticated());
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <main style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
        <p>Loading...</p>
      </main>
    );
  }

  return (
    <main style={{ 
      padding: '2rem', 
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '800px',
      margin: '0 auto'
    }}>
      <header style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>
          Kikuyu Language Hub
        </h1>
        <p style={{ fontSize: '1.2rem', color: '#6b7280' }}>
          Collaborative Kikuyu-English Translation Platform
        </p>
      </header>

      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '2rem',
        marginBottom: '3rem'
      }}>
        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: '#f9fafb'
        }}>
          <h3>🌍 Contribute Translations</h3>
          <p>Help expand the Kikuyu-English translation database by contributing new translations.</p>
        </div>

        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: '#f9fafb'
        }}>
          <h3>✅ Quality Review</h3>
          <p>Moderators review and approve translations to ensure accuracy and quality.</p>
        </div>

        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: '#f9fafb'
        }}>
          <h3>📱 Mobile Ready</h3>
          <p>Progressive Web App that works seamlessly on mobile devices.</p>
        </div>
      </div>

      <div style={{ textAlign: 'center' }}>
        {authenticated ? (
          <div>
            <p style={{ marginBottom: '1rem' }}>Welcome back!</p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <a 
                href="/dashboard" 
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              >
                Go to Dashboard
              </a>
              <a 
                href="/contributions" 
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#10b981',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              >
                View Contributions
              </a>
            </div>
          </div>
        ) : (
          <div>
            <p style={{ marginBottom: '1rem' }}>Get started today</p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <a 
                href="/login" 
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              >
                Login
              </a>
              <a 
                href="/signup" 
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#10b981',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '1rem'
                }}
              >
                Sign up
              </a>
            </div>
          </div>
        )}
      </div>

      <footer style={{ 
        marginTop: '4rem', 
        paddingTop: '2rem', 
        borderTop: '1px solid #e5e5e5',
        textAlign: 'center',
        color: '#6b7280'
      }}>
        <div style={{ marginBottom: '1rem' }}>
          <p><strong>Backend API:</strong> {process.env.NEXT_PUBLIC_API_URL}</p>
          <p><strong>Frontend:</strong> Next.js with TypeScript</p>
        </div>
        <p style={{ fontSize: '0.9rem' }}>
          Translation Contribution Platform - Building the future of Kikuyu language preservation
        </p>
      </footer>
      <PWAInstaller />
    </main>
  );
}


