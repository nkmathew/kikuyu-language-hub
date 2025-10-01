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
    <div className="container" style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center'
    }}>
      <div className="card" style={{
        width: '100%',
        maxWidth: '400px'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '2rem', color: 'var(--primary)' }}>
          Login to Kikuyu Language Hub
        </h1>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">
              Password
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="form-input"
            />
          </div>

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{
              width: '100%'
            }}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem', color: 'var(--muted-foreground)' }}>
          <p>
            Don&apos;t have an account?{' '}
            <a href="/signup" style={{ color: 'var(--primary)', textDecoration: 'none' }}>
              Sign up
            </a>
          </p>
        </div>

        <div className="card" style={{ 
          marginTop: '2rem', 
          padding: '1rem', 
          backgroundColor: 'var(--muted)',
          border: '1px solid var(--border)'
        }}>
          <h3 style={{ fontSize: '0.9rem', marginBottom: '0.5rem', color: 'var(--foreground)' }}>Test Accounts:</h3>
          <div style={{ fontSize: '0.8rem', color: 'var(--muted-foreground)' }}>
            <p><strong>Admin:</strong> admin@kikuyu.hub / admin123</p>
            <p><strong>Moderator:</strong> moderator@kikuyu.hub / mod123</p>
            <p><strong>Contributor:</strong> contributor@kikuyu.hub / contrib123</p>
          </div>
        </div>
      </div>
    </div>
  );
}