'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../../lib/hooks/useAuth';
import { useContributions } from '../../../lib/hooks/useContributions';

export default function NewContributionPage() {
  const { user, loading: authLoading } = useAuth();
  const { submitContribution } = useContributions();
  const router = useRouter();

  const [sourceText, setSourceText] = useState('');
  const [targetText, setTargetText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  if (authLoading) {
    return (
      <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
        <p>Loading...</p>
      </div>
    );
  }

  if (!user) {
    router.push('/login');
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (!sourceText.trim() || !targetText.trim()) {
      setError('Both source and target text are required');
      setLoading(false);
      return;
    }

    try {
      await submitContribution({
        source_text: sourceText.trim(),
        target_text: targetText.trim(),
        language: 'kikuyu'
      });
      setSuccess(true);
      setTimeout(() => {
        router.push('/contributions');
      }, 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit contribution');
    }
    setLoading(false);
  };

  if (success) {
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
          maxWidth: '500px',
          padding: '2rem',
          border: '1px solid #10b981',
          borderRadius: '8px',
          backgroundColor: '#ecfdf5',
          textAlign: 'center'
        }}>
          <h1 style={{ color: '#059669', marginBottom: '1rem' }}>Success!</h1>
          <p>Your translation has been submitted for review. Redirecting to contributions...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ 
      padding: '2rem', 
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '800px',
      margin: '0 auto'
    }}>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Submit New Translation</h1>
        <p style={{ color: '#6b7280' }}>
          Contribute a new Kikuyu-English translation to help expand our language database.
        </p>
      </header>

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        border: '1px solid #e5e5e5',
        borderRadius: '8px'
      }}>
        <div style={{ marginBottom: '1.5rem' }}>
          <label htmlFor="sourceText" style={{ 
            display: 'block', 
            marginBottom: '0.5rem',
            fontWeight: '500'
          }}>
            Kikuyu Text (Source)
          </label>
          <textarea
            id="sourceText"
            value={sourceText}
            onChange={(e) => setSourceText(e.target.value)}
            required
            placeholder="Enter the Kikuyu text or phrase..."
            style={{
              width: '100%',
              minHeight: '100px',
              padding: '0.75rem',
              border: '1px solid #d1d5db',
              borderRadius: '4px',
              fontSize: '1rem',
              fontFamily: 'inherit',
              resize: 'vertical'
            }}
          />
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label htmlFor="targetText" style={{ 
            display: 'block', 
            marginBottom: '0.5rem',
            fontWeight: '500'
          }}>
            English Translation (Target)
          </label>
          <textarea
            id="targetText"
            value={targetText}
            onChange={(e) => setTargetText(e.target.value)}
            required
            placeholder="Enter the English translation..."
            style={{
              width: '100%',
              minHeight: '100px',
              padding: '0.75rem',
              border: '1px solid #d1d5db',
              borderRadius: '4px',
              fontSize: '1rem',
              fontFamily: 'inherit',
              resize: 'vertical'
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

        <div style={{ 
          display: 'flex', 
          gap: '1rem',
          justifyContent: 'flex-end'
        }}>
          <button
            type="button"
            onClick={() => router.back()}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#f3f4f6',
              color: '#374151',
              border: '1px solid #d1d5db',
              borderRadius: '4px',
              fontSize: '1rem',
              cursor: 'pointer'
            }}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: loading ? '#9ca3af' : '#10b981',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '1rem',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Submitting...' : 'Submit Translation'}
          </button>
        </div>
      </form>

      <div style={{ 
        marginTop: '2rem', 
        padding: '1rem',
        backgroundColor: '#f9fafb',
        borderRadius: '4px',
        border: '1px solid #e5e5e5'
      }}>
        <h3 style={{ marginBottom: '0.5rem', fontSize: '1rem' }}>📋 Guidelines</h3>
        <ul style={{ 
          margin: 0,
          paddingLeft: '1.5rem',
          color: '#6b7280',
          fontSize: '0.9rem'
        }}>
          <li>Ensure accuracy in both Kikuyu text and English translation</li>
          <li>Use proper spelling and grammar in both languages</li>
          <li>Provide context if the translation depends on specific situations</li>
          <li>Your submission will be reviewed by moderators before approval</li>
        </ul>
      </div>
    </div>
  );
}