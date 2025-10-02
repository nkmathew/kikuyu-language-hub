'use client';

import { useState } from 'react';
import { useAuth } from '../../lib/hooks/useAuth';
import { useContributions } from '../../lib/hooks/useContributions';
import { isModerator } from '../../lib/auth';

interface ModeratorPageProps {
  onNavigate: (path: string) => void;
}

export default function ModeratorPage({ onNavigate }: ModeratorPageProps) {
  const { user, loading: authLoading } = useAuth();
  const { contributions, loading, error, approveContribution, rejectContribution } = useContributions('pending');
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [showRejectModal, setShowRejectModal] = useState<number | null>(null);

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

  if (!isModerator(user)) {
    return (
      <div className="container" style={{ 
        padding: '2rem', 
        textAlign: 'center'
      }}>
        <h1 style={{ color: 'var(--destructive)' }}>Access Denied</h1>
        <p style={{ color: 'var(--muted-foreground)' }}>You don't have permission to access this page.</p>
        <button 
          onClick={() => onNavigate('/dashboard')}
          style={{ 
            background: 'none', 
            border: 'none', 
            color: 'var(--primary)', 
            textDecoration: 'underline',
            cursor: 'pointer'
          }}
        >
          ← Back to Dashboard
        </button>
      </div>
    );
  }

  const handleApprove = async (id: number) => {
    setActionLoading(id);
    try {
      await approveContribution(id);
    } catch (err) {
      console.error('Failed to approve contribution:', err);
    }
    setActionLoading(null);
  };

  const handleReject = async (id: number) => {
    setActionLoading(id);
    try {
      await rejectContribution(id, rejectReason || 'No reason provided');
      setShowRejectModal(null);
      setRejectReason('');
    } catch (err) {
      console.error('Failed to reject contribution:', err);
    }
    setActionLoading(null);
  };

  const openRejectModal = (id: number) => {
    setShowRejectModal(id);
    setRejectReason('');
  };

  const closeRejectModal = () => {
    setShowRejectModal(null);
    setRejectReason('');
  };

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <header className="nav" style={{ 
        marginBottom: '2rem',
        paddingBottom: '1rem'
      }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⚖️ Moderation Queue</h1>
        <p style={{ color: 'var(--muted-foreground)' }}>
          Review and approve pending translation submissions
        </p>
      </header>

      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--foreground)' }}>
          <p>Loading pending contributions...</p>
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
          <h3 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>🎉 All caught up!</h3>
          <p style={{ color: 'var(--muted-foreground)' }}>
            No pending contributions to review at the moment.
          </p>
        </div>
      )}

      {!loading && !error && contributions.length > 0 && (
        <div>
          <div className="alert alert-info" style={{ 
            marginBottom: '1.5rem'
          }}>
            <p style={{ margin: 0, color: 'var(--foreground)' }}>
              📋 <strong>{contributions.length}</strong> contribution{contributions.length !== 1 ? 's' : ''} pending review
            </p>
          </div>

          <div className="grid grid-cols-1" style={{ gap: '1.5rem' }}>
            {contributions.map((contribution) => (
              <div key={contribution.id} className="card">
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '1rem'
                }}>
                  <div>
                    <h3 style={{ 
                      fontSize: '1.1rem', 
                      marginBottom: '0.5rem',
                      color: 'var(--foreground)'
                    }}>
                      Translation #{contribution.id}
                    </h3>
                    <div style={{ fontSize: '0.9rem', color: 'var(--muted-foreground)' }}>
                      Submitted: {new Date(contribution.created_at).toLocaleDateString()}
                      <br />
                      By: {contribution.created_by?.email || 'Unknown user'}
                    </div>
                  </div>
                  <span className="badge badge-pending">
                    ⏳ PENDING
                  </span>
                </div>

                <div className="grid grid-cols-2" style={{ marginBottom: '1.5rem' }}>
                  <div>
                    <div style={{ 
                      fontSize: '0.8rem', 
                      color: 'var(--muted-foreground)', 
                      marginBottom: '0.5rem',
                      textTransform: 'uppercase',
                      fontWeight: '500'
                    }}>
                      Kikuyu (Source)
                    </div>
                    <div style={{
                      padding: '1rem',
                      backgroundColor: 'var(--muted)',
                      borderRadius: 'var(--radius)',
                      border: '1px solid var(--border)',
                      minHeight: '80px'
                    }}>
                      {contribution.source_text}
                    </div>
                  </div>
                  <div>
                    <div style={{ 
                      fontSize: '0.8rem', 
                      color: 'var(--muted-foreground)', 
                      marginBottom: '0.5rem',
                      textTransform: 'uppercase',
                      fontWeight: '500'
                    }}>
                      English (Target)
                    </div>
                    <div style={{
                      padding: '1rem',
                      backgroundColor: 'var(--muted)',
                      borderRadius: 'var(--radius)',
                      border: '1px solid var(--border)',
                      minHeight: '80px'
                    }}>
                      {contribution.target_text}
                    </div>
                  </div>
                </div>

                <div style={{ 
                  display: 'flex', 
                  gap: '1rem',
                  justifyContent: 'flex-end'
                }}>
                  <button
                    onClick={() => openRejectModal(contribution.id)}
                    disabled={actionLoading === contribution.id}
                    className="btn btn-destructive"
                  >
                    {actionLoading === contribution.id ? 'Processing...' : '❌ Reject'}
                  </button>
                  <button
                    onClick={() => handleApprove(contribution.id)}
                    disabled={actionLoading === contribution.id}
                    className="btn btn-primary"
                  >
                    {actionLoading === contribution.id ? 'Processing...' : '✅ Approve'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div className="card" style={{
            width: '100%',
            maxWidth: '500px',
            margin: '1rem'
          }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>Reject Translation</h3>
            <p style={{ marginBottom: '1rem', color: 'var(--muted-foreground)' }}>
              Please provide a reason for rejecting this translation. This will help the contributor understand what needs to be improved.
            </p>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Enter rejection reason..."
              className="form-textarea"
              style={{
                marginBottom: '1rem'
              }}
            />
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button
                onClick={closeRejectModal}
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
                onClick={() => handleReject(showRejectModal)}
                disabled={actionLoading === showRejectModal}
                className="btn btn-destructive"
              >
                {actionLoading === showRejectModal ? 'Rejecting...' : 'Confirm Reject'}
              </button>
            </div>
          </div>
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