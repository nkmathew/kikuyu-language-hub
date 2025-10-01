'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../lib/hooks/useAuth';
import { useContributions } from '../../lib/hooks/useContributions';
import { ContributionStatus } from '../../lib/types';

export default function ContributionsPage() {
  const { user, loading: authLoading } = useAuth();
  const [statusFilter, setStatusFilter] = useState<ContributionStatus | ''>('');
  const { contributions, loading, error, refetch } = useContributions(statusFilter || undefined);
  const router = useRouter();

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

  const getStatusColor = (status: ContributionStatus) => {
    switch (status) {
      case 'approved': return '#10b981';
      case 'rejected': return '#dc2626';
      case 'pending': return '#f59e0b';
      default: return '#6b7280';
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
    <div style={{ 
      padding: '2rem', 
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '1200px',
      margin: '0 auto'
    }}>
      <header style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid #e5e5e5'
      }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>My Contributions</h1>
          <p style={{ color: '#6b7280' }}>View and manage your translation submissions</p>
        </div>
        <a
          href="/contributions/new"
          style={{
            display: 'inline-block',
            padding: '0.75rem 1.5rem',
            backgroundColor: '#10b981',
            color: 'white',
            textDecoration: 'none',
            borderRadius: '4px',
            fontSize: '1rem'
          }}
        >
          + New Translation
        </a>
      </header>

      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <label htmlFor="statusFilter" style={{ fontWeight: '500' }}>
            Filter by status:
          </label>
          <select
            id="statusFilter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ContributionStatus | '')}
            style={{
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '4px',
              fontSize: '1rem'
            }}
          >
            <option value="">All</option>
            <option value="pending">Pending</option>
            <option value="approved">Approved</option>
            <option value="rejected">Rejected</option>
          </select>
          <button
            onClick={refetch}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Refresh
          </button>
        </div>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p>Loading contributions...</p>
        </div>
      )}

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          border: '1px solid #fecaca',
          borderRadius: '4px',
          color: '#dc2626',
          marginBottom: '2rem'
        }}>
          Error: {error}
        </div>
      )}

      {!loading && !error && contributions.length === 0 && (
        <div style={{
          textAlign: 'center',
          padding: '3rem',
          backgroundColor: '#f9fafb',
          borderRadius: '8px',
          border: '1px solid #e5e5e5'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>No contributions found</h3>
          <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
            {statusFilter 
              ? `No contributions with status "${statusFilter}"` 
              : 'You haven\'t submitted any translations yet.'
            }
          </p>
          <a
            href="/contributions/new"
            style={{
              display: 'inline-block',
              padding: '0.75rem 1.5rem',
              backgroundColor: '#10b981',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px'
            }}
          >
            Submit Your First Translation
          </a>
        </div>
      )}

      {!loading && !error && contributions.length > 0 && (
        <div style={{
          display: 'grid',
          gap: '1rem'
        }}>
          {contributions.map((contribution) => (
            <div
              key={contribution.id}
              style={{
                padding: '1.5rem',
                backgroundColor: 'white',
                border: '1px solid #e5e5e5',
                borderRadius: '8px'
              }}
            >
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
                    color: '#111827'
                  }}>
                    Translation #{contribution.id}
                  </h3>
                  <div style={{ fontSize: '0.9rem', color: '#6b7280' }}>
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

              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1rem',
                marginBottom: '1rem'
              }}>
                <div>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    color: '#6b7280', 
                    marginBottom: '0.25rem',
                    textTransform: 'uppercase',
                    fontWeight: '500'
                  }}>
                    Kikuyu (Source)
                  </div>
                  <div style={{
                    padding: '0.75rem',
                    backgroundColor: '#f9fafb',
                    borderRadius: '4px',
                    border: '1px solid #e5e5e5'
                  }}>
                    {contribution.source_text}
                  </div>
                </div>
                <div>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    color: '#6b7280', 
                    marginBottom: '0.25rem',
                    textTransform: 'uppercase',
                    fontWeight: '500'
                  }}>
                    English (Target)
                  </div>
                  <div style={{
                    padding: '0.75rem',
                    backgroundColor: '#f9fafb',
                    borderRadius: '4px',
                    border: '1px solid #e5e5e5'
                  }}>
                    {contribution.target_text}
                  </div>
                </div>
              </div>

              {contribution.reason && (
                <div style={{
                  padding: '0.75rem',
                  backgroundColor: '#fee2e2',
                  border: '1px solid #fecaca',
                  borderRadius: '4px',
                  marginTop: '1rem'
                }}>
                  <div style={{ 
                    fontSize: '0.8rem', 
                    color: '#dc2626', 
                    marginBottom: '0.25rem',
                    fontWeight: '500'
                  }}>
                    Moderator Note:
                  </div>
                  <div style={{ color: '#dc2626' }}>
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
        borderTop: '1px solid #e5e5e5'
      }}>
        <a href="/dashboard" style={{ color: '#6b7280', textDecoration: 'none' }}>
          ← Back to Dashboard
        </a>
      </div>
    </div>
  );
}