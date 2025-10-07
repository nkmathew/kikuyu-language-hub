'use client';

import { useState } from 'react';
import { signup } from '../../lib/auth';

interface SignupPageProps {
  onNavigate: (path: string) => void;
}

export default function SignupPage({ onNavigate }: SignupPageProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      setLoading(false);
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      setLoading(false);
      return;
    }

    try {
      await signup({ email, password });
      setSuccess(true);
      setTimeout(() => {
        onNavigate('/login');
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Signup failed');
    }
    setLoading(false);
  };

  if (success) {
    return (
      <div className="container" style={{ 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center'
      }}>
        <div className="card" style={{
          width: '100%',
          maxWidth: '400px',
          backgroundColor: 'var(--muted)',
          border: '1px solid var(--primary)',
          textAlign: 'center'
        }}>
          <h1 style={{ color: 'var(--primary)', marginBottom: '1rem' }}>Success!</h1>
          <p style={{ color: 'var(--foreground)' }}>Your account has been created. Redirecting to login...</p>
        </div>
      </div>
    );
  }

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
          Sign up for Kikuyu Language Hub
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
              minLength={6}
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword" className="form-label">
              Confirm Password
            </label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
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
            {loading ? 'Creating account...' : 'Sign up'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem', color: 'var(--muted-foreground)' }}>
          <p>
            Already have an account?{' '}
            <button
              onClick={() => onNavigate('/login')}
              style={{ 
                background: 'none', 
                border: 'none', 
                color: 'var(--primary)', 
                textDecoration: 'underline',
                cursor: 'pointer'
              }}
            >
              Login
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}