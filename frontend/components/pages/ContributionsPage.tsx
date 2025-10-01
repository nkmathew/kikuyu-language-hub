'use client';

import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { useContributions } from '../../hooks/useContributions';
import { ContributionStatus } from '../../lib/types';

interface ContributionsPageProps {
  onNavigate: (path: string) => void;
}

export default function ContributionsPage({ onNavigate }: ContributionsPageProps) {
  const { user, loading: authLoading } = useAuth();
  const [statusFilter, setStatusFilter] = useState<ContributionStatus | ''>('');
  const { contributions, loading, error, refetch } = useContributions(statusFilter || undefined);

  if (authLoading) {
    return (
      <div className="container" style={{ padding: '2rem' }}>
        <p style={{ color: 'var(--foreground)' }}>Loading...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  const getStatusColor = (status: ContributionStatus) => {
    switch (status) {
      case 'approved': return 'var(--primary)';
      case 'rejected': return 'var(--destructive)';
      case 'pending': return 'var(--accent)';
      default: return 'var(--muted-foreground)';
    }
  };

  const getStatusIcon = (status: ContributionStatus) => {
    switch (status) {
      case 'approved': return '✅';
      case 'rejected': return '❌';
      case 'pending': return '⏳';
      default: return '📝';
    }
  };

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <header className="nav" style={{ 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '2rem',
        paddingBottom: '1rem'
      }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>My Contributions</h1>
          <p style={{ color: 'var(--muted-foreground)' }}>View and manage your translation submissions</p>
        </div>
        <button
          onClick={() => onNavigate('/contributions/new')}
          className="btn btn-primary"
          style={{
            display: 'inline-block',
            textDecoration: 'none'
          }}
        >
          + New Translation
        </button>
      </header>

      <div style={{ marginBottom: '2rem' }}>
        <div className="grid grid-cols-1" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <label htmlFor="statusFilter" className="form-label" style={{ fontWeight: '500', margin: 0 }}>
            Filter by status:
          </label>
          <select
            id="statusFilter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ContributionStatus | '')}
            className="form-input"
            style={{
              padding: '0.5rem',
              width: 'auto'
            }}
          >
            <option value="">All</option>
            <option value="pending">Pending</option>
            <option value="approved">Approved</option>
            <option value="rejected">Rejected</option>
          </select>
          <button
            onClick={refetch}
            className="btn btn-secondary"
            style={{
              backgroundColor: 'var(--accent)',
              color: 'var(--accent-foreground)'
            }}
          >
            Refresh
          </button>
        </div>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--foreground)' }}>
          <p>Loading contributions...</p>
        </div>
      )}

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '2rem' }}>
          Error: {error}
        </div>
      )}

      {!loading && !error && contributions.length === 0 && (
        <div className="card" style={{
          textAlign: 'center',
          padding: '3rem',
          backgroundColor: 'var(--muted)'
        }}>
          <h3 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>No contributions found</h3>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
            {statusFilter 
              ? `No contributions with status "${statusFilter}"` 
              : 'You haven\'t submitted any translations yet.'
            }
          </p>
          <button
            onClick={() => onNavigate('/contributions/new')}
            className="btn btn-primary"
            style={{
              display: 'inline-block',
              textDecoration: 'none'
            }}
          >
            Submit Your First Translation
          </button>
        </div>
      )}

      {!loading && !error && contributions.length > 0 && (
        <div className="grid grid-cols-1" style={{ gap: '1rem' }}>
          {contributions.map((contribution) => (
            <div key={contribution.id} className="card">
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                marginBottom: '1rem'
              }}>
                <div style={{ flex: 1 }}>
                  <h3 style={{ 
                    fontSize: '1.1rem', 
                    marginBottom: '0.5rem',
                    color: 'var(--foreground)'
                  }}>
                    Translation #{contribution.id}
                  </h3>
                  <div style={{ fontSize: '0.9rem', color: 'var(--muted-foreground)' }}>
                    Submitted: {new Date(contribution.created_at).toLocaleDateString()}
                    {contribution.updated_at !== contribution.created_at && (
                      <span> • Updated: {new Date(contribution.updated_at).toLocaleDateString()}</span>
                    )}
                  </div>
                </div>
                <span style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.25rem 0.75rem',
                  backgroundColor: getStatusColor(contribution.status) + '20',
                  color: getStatusColor(contribution.status),
                  borderRadius: '1rem',
                  fontSize: '0.8rem',
                  fontWeight: '500'
                }}>
                  {getStatusIcon(contribution.status)} {contribution.status.toUpperCase()}
                </span>
              </div>

              <div className="grid grid-cols-2" style={{
                marginBottom: '1rem'
              }}>
                <div>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    color: 'var(--muted-foreground)', 
                    marginBottom: '0.25rem',
                    textTransform: 'uppercase',
                    fontWeight: '500'
                  }}>
                    Kikuyu (Source)
                  </div>
                  <div style={{
                    padding: '0.75rem',
                    backgroundColor: 'var(--muted)',
                    borderRadius: 'var(--radius)',
                    border: '1px solid var(--border)'
                  }}>
                    {contribution.source_text}
                  </div>
                </div>
                <div>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    color: 'var(--muted-foreground)', 
                    marginBottom: '0.25rem',
                    textTransform: 'uppercase',
                    fontWeight: '500'
                  }}>
                    English (Target)
                  </div>
                  <div style={{
                    padding: '0.75rem',
                    backgroundColor: 'var(--muted)',
                    borderRadius: 'var(--radius)',
                    border: '1px solid var(--border)'
                  }}>
                    {contribution.target_text}
                  </div>
                </div>
              </div>

              {contribution.reason && (
                <div className="alert alert-error" style={{ marginTop: '1rem' }}>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    marginBottom: '0.25rem',
                    fontWeight: '500'
                  }}>
                    Moderator Note:
                  </div>
                  <div>
                    {contribution.reason}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      <div style={{ 
        marginTop: '2rem', 
        textAlign: 'center',
        paddingTop: '2rem',
        borderTop: '1px solid var(--border)'
      }}>
        <button 
          onClick={() => onNavigate('/dashboard')}
          style={{ 
            background: 'none', 
            border: 'none', 
            color: 'var(--muted-foreground)', 
            textDecoration: 'underline',
            cursor: 'pointer'
          }}
        >
          ← Back to Dashboard
        </button>
      </div>
    </div>
  );
}