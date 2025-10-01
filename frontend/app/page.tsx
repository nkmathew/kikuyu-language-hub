'use client';

import { useEffect, useState } from 'react';

export default function HomePage() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simple check for authentication token
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    setAuthenticated(!!token);
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <main style={{ padding: '2rem', textAlign: 'center', fontFamily: 'system-ui, sans-serif' }}>
        <p>Loading...</p>
      </main>
    );
  }

  return (
    <main style={{ 
      padding: '2rem', 
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '1200px',
      margin: '0 auto'
    }}>
      {/* Navigation */}
      <nav style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '3rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid #e5e5e5'
      }}>
        <h2 style={{ color: '#10b981', margin: 0 }}>🌍 KikuyuHub</h2>
        <div style={{ display: 'flex', gap: '1rem' }}>
          {authenticated ? (
            <>
              <a href="/dashboard" style={{ color: '#6b7280', textDecoration: 'none' }}>Dashboard</a>
              <a href="/contributions" style={{ color: '#6b7280', textDecoration: 'none' }}>Contributions</a>
              <button 
                onClick={() => {
                  localStorage.removeItem('token');
                  window.location.reload();
                }}
                style={{
                  background: 'none',
                  border: '1px solid #dc2626',
                  color: '#dc2626',
                  padding: '0.5rem 1rem',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <a href="/login" style={{ color: '#6b7280', textDecoration: 'none' }}>Login</a>
              <a href="/signup" style={{ color: '#6b7280', textDecoration: 'none' }}>Sign Up</a>
            </>
          )}
        </div>
      </nav>

      {/* Header */}
      <header style={{ textAlign: 'center', marginBottom: '4rem' }}>
        <h1 style={{ 
          fontSize: '3rem', 
          marginBottom: '1rem',
          color: '#10b981',
          fontWeight: 700
        }}>
          Kikuyu Language Hub
        </h1>
        <p style={{ 
          fontSize: '1.25rem', 
          color: '#6b7280',
          maxWidth: '600px',
          margin: '0 auto'
        }}>
          Collaborative Kikuyu-English Translation Platform
        </p>
      </header>

      {/* Feature Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '2rem',
        marginBottom: '4rem'
      }}>
        <div style={{
          padding: '2rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: 'white',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#10b981' }}>
            🌍 Contribute Translations
          </h3>
          <p style={{ color: '#6b7280' }}>
            Help expand the Kikuyu-English translation database by contributing new translations.
          </p>
        </div>

        <div style={{
          padding: '2rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: 'white',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#10b981' }}>
            ✅ Quality Review
          </h3>
          <p style={{ color: '#6b7280' }}>
            Moderators review and approve translations to ensure accuracy and quality.
          </p>
        </div>

        <div style={{
          padding: '2rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: 'white',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#10b981' }}>
            📱 Mobile Ready
          </h3>
          <p style={{ color: '#6b7280' }}>
            Progressive Web App that works seamlessly on mobile devices.
          </p>
        </div>
      </div>

      {/* Call to Action */}
      <div style={{ textAlign: 'center', marginBottom: '4rem' }}>
        {authenticated ? (
          <div>
            <p style={{ marginBottom: '2rem', fontSize: '1.1rem' }}>Welcome back!</p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <a href="/dashboard" style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#10b981',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: '500'
              }}>
                📊 Go to Dashboard
              </a>
              <a href="/contributions" style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: '500'
              }}>
                📋 View Contributions
              </a>
              <a href="/contributions/new" style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#10b981',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: '500'
              }}>
                ✏️ Submit Translation
              </a>
            </div>
          </div>
        ) : (
          <div>
            <p style={{ marginBottom: '2rem', fontSize: '1.1rem' }}>Get started today</p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <a href="/login" style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#10b981',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: '500'
              }}>
                🔑 Login
              </a>
              <a href="/signup" style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: '500'
              }}>
                📝 Sign up
              </a>
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <footer style={{ 
        marginTop: '6rem', 
        paddingTop: '2rem', 
        borderTop: '1px solid #e5e5e5',
        textAlign: 'center',
        color: '#6b7280'
      }}>
        <div style={{ 
          marginBottom: '2rem', 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', 
          gap: '2rem' 
        }}>
          <div>
            <h4 style={{ marginBottom: '0.5rem', color: '#374151' }}>Platform</h4>
            <p><strong>Backend:</strong> FastAPI + SQLite</p>
            <p><strong>Frontend:</strong> Next.js + TypeScript</p>
          </div>
          <div>
            <h4 style={{ marginBottom: '0.5rem', color: '#374151' }}>Features</h4>
            <p>Role-based access control</p>
            <p>Real-time collaboration</p>
          </div>
          <div>
            <h4 style={{ marginBottom: '0.5rem', color: '#374151' }}>Technology</h4>
            <p>Progressive Web App</p>
            <p>Offline functionality</p>
          </div>
        </div>
        <p style={{ 
          fontSize: '0.9rem', 
          borderTop: '1px solid #e5e5e5', 
          paddingTop: '1rem',
          marginTop: '2rem'
        }}>
          🌍 Building the future of Kikuyu language preservation
        </p>
      </footer>
    </main>
  );
}


