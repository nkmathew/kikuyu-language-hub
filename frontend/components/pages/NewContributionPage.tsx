'use client';

import { useState } from 'react';
import { useAuth } from '../../lib/hooks/useAuth';
import { useContributions } from '../../hooks/useContributions';

interface NewContributionPageProps {
  onNavigate: (path: string) => void;
}

export default function NewContributionPage({ onNavigate }: NewContributionPageProps) {
  const { user } = useAuth();
  const { createContribution } = useContributions();
  const [sourceText, setSourceText] = useState('');
  const [targetText, setTargetText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (!sourceText.trim() || !targetText.trim()) {
      setError('Both Kikuyu and English translations are required');
      setLoading(false);
      return;
    }

    try {
      await createContribution({
        source_text: sourceText.trim(),
        target_text: targetText.trim(),
        language: 'kikuyu'
      });
      setSuccess(true);
      setTimeout(() => {
        onNavigate('/contributions');
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit translation');
    }
    setLoading(false);
  };

  if (!user) {
    return null;
  }

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
          maxWidth: '600px',
          backgroundColor: 'var(--muted)',
          border: '1px solid var(--primary)',
          textAlign: 'center'
        }}>
          <h1 style={{ color: 'var(--primary)', marginBottom: '1rem' }}>✅ Translation Submitted!</h1>
          <p style={{ color: 'var(--foreground)', marginBottom: '1.5rem' }}>
            Your translation has been submitted for review. It will be checked by our moderators before being published.
          </p>
          <p style={{ color: 'var(--muted-foreground)' }}>
            Redirecting to your contributions...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <div style={{ maxWidth: '600px', margin: '0 auto' }}>
        <header style={{ marginBottom: '2rem', textAlign: 'center' }}>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem', color: 'var(--primary)' }}>
            Submit New Translation
          </h1>
          <p style={{ color: 'var(--muted-foreground)' }}>
            Help preserve the Kikuyu language by contributing new translations
          </p>
        </header>

        <form onSubmit={handleSubmit} className="card">
          <div className="form-group">
            <label htmlFor="sourceText" className="form-label">
              Kikuyu (Source Text) *
            </label>
            <textarea
              id="sourceText"
              value={sourceText}
              onChange={(e) => setSourceText(e.target.value)}
              required
              placeholder="Enter the Kikuyu word or phrase..."
              className="form-textarea"
              style={{ minHeight: '120px' }}
            />
          </div>

          <div className="form-group">
            <label htmlFor="targetText" className="form-label">
              English Translation *
            </label>
            <textarea
              id="targetText"
              value={targetText}
              onChange={(e) => setTargetText(e.target.value)}
              required
              placeholder="Enter the English translation..."
              className="form-textarea"
              style={{ minHeight: '120px' }}
            />
          </div>

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'space-between', marginTop: '2rem' }}>
            <button
              type="button"
              onClick={() => onNavigate('/contributions')}
              className="btn btn-secondary"
              style={{
                backgroundColor: 'var(--secondary)',
                color: 'var(--secondary-foreground)',
                borderColor: 'var(--border)'
              }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{
                padding: '0.75rem 2rem',
                fontSize: '1.1rem'
              }}
            >
              {loading ? 'Submitting...' : 'Submit Translation'}
            </button>
          </div>
        </form>

        <div className="card" style={{ 
          marginTop: '2rem', 
          padding: '1.5rem', 
          backgroundColor: 'var(--muted)',
          border: '1px solid var(--border)'
        }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '1rem', color: 'var(--foreground)' }}>📝 Guidelines:</h3>
          <ul style={{ color: 'var(--muted-foreground)', paddingLeft: '1.5rem', lineHeight: '1.6' }}>
            <li style={{ marginBottom: '0.5rem' }}>Ensure accurate spelling in both Kikuyu and English</li>
            <li style={{ marginBottom: '0.5rem' }}>Provide context-specific translations when possible</li>
            <li style={{ marginBottom: '0.5rem' }}>Consider cultural nuances in your translations</li>
            <li style={{ marginBottom: '0.5rem' }}>Be respectful and inclusive in your language choices</li>
            <li>Submit only original translations or public domain content</li>
          </ul>
        </div>

        <div style={{ textAlign: 'center', marginTop: '2rem' }}>
          <button 
            onClick={() => onNavigate('/contributions')}
            style={{ 
              background: 'none', 
              border: 'none', 
              color: 'var(--muted-foreground)', 
              textDecoration: 'underline',
              cursor: 'pointer'
            }}
          >
            ← Back to My Contributions
          </button>
        </div>
      </div>
    </div>
  );
}