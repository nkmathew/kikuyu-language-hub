'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../lib/hooks/useAuth';
import { logout, isAdmin, isModerator } from '../../lib/auth';

export default function DashboardPage() {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) {
      router.push('/login');
    }
  }, [user, loading, router]);

  const handleLogout = async () => {
    await logout();
    router.push('/');
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
        <p>Loading...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

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
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Dashboard</h1>
          <p style={{ color: '#6b7280' }}>Welcome back, {user.email}</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <span style={{
            padding: '0.25rem 0.75rem',
            backgroundColor: user.role === 'admin' ? '#dc2626' : user.role === 'moderator' ? '#f59e0b' : '#10b981',
            color: 'white',
            borderRadius: '1rem',
            fontSize: '0.8rem',
            textTransform: 'capitalize'
          }}>
            {user.role}
          </span>
          <button
            onClick={handleLogout}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#dc2626',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '2rem',
        marginBottom: '3rem'
      }}>
        {/* Contributor Features */}
        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: 'white'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#10b981' }}>📝 Contribute</h3>
          <p style={{ marginBottom: '1.5rem', color: '#6b7280' }}>
            Submit new Kikuyu-English translations
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
            Submit Translation
          </a>
        </div>

        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e5e5',
          borderRadius: '8px',
          backgroundColor: 'white'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#3b82f6' }}>📋 My Contributions</h3>
          <p style={{ marginBottom: '1.5rem', color: '#6b7280' }}>
            View and manage your translation submissions
          </p>
          <a
            href="/contributions"
            style={{
              display: 'inline-block',
              padding: '0.75rem 1.5rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px'
            }}
          >
            View Contributions
          </a>
        </div>

        {/* Moderator Features */}
        {isModerator(user) && (
          <div style={{
            padding: '1.5rem',
            border: '1px solid #f59e0b',
            borderRadius: '8px',
            backgroundColor: '#fffbeb'
          }}>
            <h3 style={{ marginBottom: '1rem', color: '#f59e0b' }}>⚖️ Moderation</h3>
            <p style={{ marginBottom: '1.5rem', color: '#6b7280' }}>
              Review pending translations
            </p>
            <a
              href="/moderator"
              style={{
                display: 'inline-block',
                padding: '0.75rem 1.5rem',
                backgroundColor: '#f59e0b',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px'
              }}
            >
              Review Queue
            </a>
          </div>
        )}

        {/* Admin Features */}
        {isAdmin(user) && (
          <div style={{
            padding: '1.5rem',
            border: '1px solid #dc2626',
            borderRadius: '8px',
            backgroundColor: '#fef2f2'
          }}>
            <h3 style={{ marginBottom: '1rem', color: '#dc2626' }}>⚙️ Admin</h3>
            <p style={{ marginBottom: '1.5rem', color: '#6b7280' }}>
              System administration and user management
            </p>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <a
                href="/admin/users"
                style={{
                  display: 'inline-block',
                  padding: '0.5rem 1rem',
                  backgroundColor: '#dc2626',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '0.9rem'
                }}
              >
                Users
              </a>
              <a
                href="/admin/audit"
                style={{
                  display: 'inline-block',
                  padding: '0.5rem 1rem',
                  backgroundColor: '#dc2626',
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '0.9rem'
                }}
              >
                Audit Log
              </a>
            </div>
          </div>
        )}
      </div>

      {/* Quick Stats */}
      <div style={{
        padding: '1.5rem',
        border: '1px solid #e5e5e5',
        borderRadius: '8px',
        backgroundColor: '#f9fafb'
      }}>
        <h3 style={{ marginBottom: '1rem' }}>📊 Platform Statistics</h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem'
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#10b981' }}>-</div>
            <div style={{ color: '#6b7280' }}>Total Translations</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#3b82f6' }}>-</div>
            <div style={{ color: '#6b7280' }}>Pending Review</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#f59e0b' }}>-</div>
            <div style={{ color: '#6b7280' }}>Active Contributors</div>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <div style={{ 
        marginTop: '2rem', 
        textAlign: 'center',
        paddingTop: '2rem',
        borderTop: '1px solid #e5e5e5'
      }}>
        <a href="/" style={{ color: '#6b7280', textDecoration: 'none' }}>
          ← Back to Home
        </a>
      </div>
    </div>
  );
}