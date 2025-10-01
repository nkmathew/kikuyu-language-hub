'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../lib/hooks/useAuth';
import { useContributions } from '../../lib/hooks/useContributions';
import { isModerator } from '../../lib/auth';

export default function ModeratorPage() {
  const { user, loading: authLoading } = useAuth();
  const { contributions, loading, error, approveContribution, rejectContribution } = useContributions('pending');
  const router = useRouter();
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [showRejectModal, setShowRejectModal] = useState<number | null>(null);

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

  if (!isModerator(user)) {
    return (
      <div style={{ 
        padding: '2rem', 
        fontFamily: 'system-ui, sans-serif',
        textAlign: 'center'
      }}>
        <h1>Access Denied</h1>
        <p>You don't have permission to access this page.</p>
        <a href="/dashboard" style={{ color: '#3b82f6' }}>← Back to Dashboard</a>
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
    <div style={{ 
      padding: '2rem', 
      fontFamily: 'system-ui, sans-serif',
      maxWidth: '1200px',
      margin: '0 auto'
    }}>
      <header style={{ 
        marginBottom: '2rem',
        paddingBottom: '1rem',
        borderBottom: '1px solid #e5e5e5'
      }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⚖️ Moderation Queue</h1>
        <p style={{ color: '#6b7280' }}>
          Review and approve pending translation submissions
        </p>
      </header>

      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p>Loading pending contributions...</p>
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
          <h3 style={{ marginBottom: '1rem' }}>🎉 All caught up!</h3>
          <p style={{ color: '#6b7280' }}>
            No pending contributions to review at the moment.
          </p>
        </div>
      )}

      {!loading && !error && contributions.length > 0 && (
        <div>
          <div style={{ 
            marginBottom: '1.5rem',
            padding: '1rem',
            backgroundColor: '#f0f9ff',
            border: '1px solid #e0f2fe',
            borderRadius: '4px'
          }}>
            <p style={{ margin: 0, color: '#0369a1' }}>
              📋 <strong>{contributions.length}</strong> contribution{contributions.length !== 1 ? 's' : ''} pending review
            </p>
          </div>

          <div style={{
            display: 'grid',
            gap: '1.5rem'
          }}>
            {contributions.map((contribution) => (
              <div
                key={contribution.id}
                style={{
                  padding: '1.5rem',
                  backgroundColor: 'white',
                  border: '1px solid #e5e5e5',
                  borderRadius: '8px',
                  boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)'
                }}
              >
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
                      color: '#111827'
                    }}>
                      Translation #{contribution.id}
                    </h3>
                    <div style={{ fontSize: '0.9rem', color: '#6b7280' }}>
                      Submitted: {new Date(contribution.created_at).toLocaleDateString()}
                      <br />
                      By: {contribution.created_by?.email || 'Unknown user'}
                    </div>
                  </div>
                  <span style={{
                    padding: '0.25rem 0.75rem',
                    backgroundColor: '#fef3c7',
                    color: '#f59e0b',
                    borderRadius: '1rem',
                    fontSize: '0.8rem',
                    fontWeight: '500'
                  }}>
                    ⏳ PENDING
                  </span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '1rem',
                  marginBottom: '1.5rem'
                }}>
                  <div>
                    <div style={{ 
                      fontSize: '0.8rem', 
                      color: '#6b7280', 
                      marginBottom: '0.5rem',
                      textTransform: 'uppercase',
                      fontWeight: '500'
                    }}>
                      Kikuyu (Source)
                    </div>
                    <div style={{
                      padding: '1rem',
                      backgroundColor: '#f9fafb',
                      borderRadius: '4px',
                      border: '1px solid #e5e5e5',
                      minHeight: '80px'
                    }}>
                      {contribution.source_text}
                    </div>
                  </div>
                  <div>
                    <div style={{ 
                      fontSize: '0.8rem', 
                      color: '#6b7280', 
                      marginBottom: '0.5rem',
                      textTransform: 'uppercase',
                      fontWeight: '500'
                    }}>
                      English (Target)
                    </div>
                    <div style={{
                      padding: '1rem',
                      backgroundColor: '#f9fafb',
                      borderRadius: '4px',
                      border: '1px solid #e5e5e5',
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
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: actionLoading === contribution.id ? '#fca5a5' : '#dc2626',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      fontSize: '1rem',
                      cursor: actionLoading === contribution.id ? 'not-allowed' : 'pointer'
                    }}
                  >
                    {actionLoading === contribution.id ? 'Processing...' : '❌ Reject'}
                  </button>
                  <button
                    onClick={() => handleApprove(contribution.id)}
                    disabled={actionLoading === contribution.id}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: actionLoading === contribution.id ? '#a7f3d0' : '#10b981',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      fontSize: '1rem',
                      cursor: actionLoading === contribution.id ? 'not-allowed' : 'pointer'
                    }}
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
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '8px',
            width: '100%',
            maxWidth: '500px',
            margin: '1rem'
          }}>
            <h3 style={{ marginBottom: '1rem' }}>Reject Translation</h3>
            <p style={{ marginBottom: '1rem', color: '#6b7280' }}>
              Please provide a reason for rejecting this translation. This will help the contributor understand what needs to be improved.
            </p>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Enter rejection reason..."
              style={{
                width: '100%',
                minHeight: '100px',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '4px',
                fontSize: '1rem',
                marginBottom: '1rem',
                resize: 'vertical'
              }}
            />
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button
                onClick={closeRejectModal}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#f3f4f6',
                  color: '#374151',
                  border: '1px solid #d1d5db',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
              <button
                onClick={() => handleReject(showRejectModal)}
                disabled={actionLoading === showRejectModal}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: actionLoading === showRejectModal ? '#fca5a5' : '#dc2626',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: actionLoading === showRejectModal ? 'not-allowed' : 'pointer'
                }}
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
        borderTop: '1px solid #e5e5e5'
      }}>
        <a href="/dashboard" style={{ color: '#6b7280', textDecoration: 'none' }}>
          ← Back to Dashboard
        </a>
      </div>
    </div>
  );
}