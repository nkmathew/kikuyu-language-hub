'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { login } from '../../lib/auth';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await login({ email, password });
      router.push('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    }
    setLoading(false);
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      padding: '1rem',
      fontFamily: 'system-ui, sans-serif'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '400px',
        padding: '2rem',
        border: '1px solid #e5e5e5',
        borderRadius: '8px',
        backgroundColor: 'white'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>
          Login to Kikuyu Language Hub
        </h1>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="email" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '4px',
                fontSize: '1rem'
              }}
            />
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label htmlFor="password" style={{ display: 'block', marginBottom: '0.5rem' }}>
              Password
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '4px',
                fontSize: '1rem'
              }}
            />
          </div>

          {error && (
            <div style={{
              padding: '0.75rem',
              marginBottom: '1rem',
              backgroundColor: '#fee2e2',
              border: '1px solid #fecaca',
              borderRadius: '4px',
              color: '#dc2626'
            }}>
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: loading ? '#9ca3af' : '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1rem',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <p>
            Don&apos;t have an account?{' '}
            <a href="/signup" style={{ color: '#3b82f6', textDecoration: 'none' }}>
              Sign up
            </a>
          </p>
        </div>

        <div style={{ marginTop: '2rem', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '4px' }}>
          <h3 style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>Test Accounts:</h3>
          <div style={{ fontSize: '0.8rem', color: '#6b7280' }}>
            <p><strong>Admin:</strong> admin@kikuyu.hub / admin123</p>
            <p><strong>Moderator:</strong> moderator@kikuyu.hub / mod123</p>
            <p><strong>Contributor:</strong> contributor@kikuyu.hub / contrib123</p>
          </div>
        </div>
      </div>
    </div>
  );
}