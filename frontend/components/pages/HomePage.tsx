'use client';

import { useAuth } from '../lib/hooks/useAuth';
import { isAdmin, isModerator } from '../lib/auth';

interface HomePageProps {
  onNavigate: (path: string) => void;
}

export default function HomePage({ onNavigate }: HomePageProps) {
  const { user } = useAuth();

  return (
    <div className="container" style={{ padding: '2rem' }}>
      <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '1rem', color: 'var(--primary)' }}>
          🌍 Kikuyu Language Hub
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--muted-foreground)', marginBottom: '2rem' }}>
          A collaborative platform for preserving and growing the Kikuyu language through community translations
        </p>
        
        {!user && (
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', marginBottom: '2rem' }}>
            <button
              onClick={() => onNavigate('/signup')}
              className="btn btn-primary"
              style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}
            >
              Get Started
            </button>
            <button
              onClick={() => onNavigate('/login')}
              className="btn btn-secondary"
              style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}
            >
              Login
            </button>
          </div>
        )}
      </div>

      {/* Features Section */}
      <div className="grid grid-auto-fit" style={{ gap: '2rem', marginBottom: '3rem' }}>
        <div className="card">
          <h3 style={{ marginBottom: '1rem', color: 'var(--primary)', fontSize: '1.5rem' }}>
            📝 Contribute Translations
          </h3>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
            Submit new Kikuyu-English translations to help grow our database and preserve the language for future generations.
          </p>
          {user && (
            <button
              onClick={() => onNavigate('/contributions/new')}
              className="btn btn-primary"
            >
              Submit Translation
            </button>
          )}
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '1rem', color: 'var(--accent-foreground)', fontSize: '1.5rem' }}>
            📚 Browse Contributions
          </h3>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
            Explore existing translations, learn Kikuyu phrases, and understand the cultural context behind each translation.
          </p>
          {user ? (
            <button
              onClick={() => onNavigate('/contributions')}
              className="btn btn-secondary"
              style={{ backgroundColor: 'var(--accent)', color: 'var(--accent-foreground)' }}
            >
              View Contributions
            </button>
          ) : (
            <button
              onClick={() => onNavigate('/signup')}
              className="btn btn-secondary"
              style={{ backgroundColor: 'var(--accent)', color: 'var(--accent-foreground)' }}
            >
              Sign Up to Browse
            </button>
          )}
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '1rem', color: 'var(--accent)', fontSize: '1.5rem' }}>
            ⚖️ Quality Assurance
          </h3>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
            Our community of moderators ensures the accuracy and cultural appropriateness of all translations through careful review.
          </p>
        </div>
      </div>

      {/* Dashboard Access for Logged-in Users */}
      {user && (
        <div className="card" style={{ backgroundColor: 'var(--muted)', textAlign: 'center' }}>
          <h2 style={{ marginBottom: '1rem', color: 'var(--foreground)' }}>
            Welcome back, {user.email}!
          </h2>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1.5rem' }}>
            Ready to continue your language preservation journey?
          </p>
          <button
            onClick={() => onNavigate('/dashboard')}
            className="btn btn-primary"
            style={{ padding: '1rem 2rem' }}
          >
            Go to Dashboard
          </button>
        </div>
      )}

      {/* Admin/Moderator Features */}
      {user && isModerator(user) && (
        <div className="grid grid-cols-1" style={{ gap: '2rem', marginTop: '3rem' }}>
          <div className="card" style={{ backgroundColor: 'var(--muted)', borderColor: 'var(--accent)' }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--accent-foreground)' }}>
              ⚖️ Moderator Tools
            </h3>
            <p style={{ color: 'var(--muted-foreground)', marginBottom: '1rem' }}>
              Help maintain translation quality by reviewing pending submissions.
            </p>
            <button
              onClick={() => onNavigate('/moderator')}
              className="btn btn-secondary"
              style={{ backgroundColor: 'var(--accent)', color: 'var(--accent-foreground)' }}
            >
              Review Queue
            </button>
          </div>
        </div>
      )}

      {user && isAdmin(user) && (
        <div className="grid grid-cols-1" style={{ gap: '2rem', marginTop: '2rem' }}>
          <div className="card" style={{ backgroundColor: 'var(--muted)', borderColor: 'var(--destructive)' }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--destructive)' }}>
              ⚙️ Admin Tools
            </h3>
            <p style={{ color: 'var(--muted-foreground)', marginBottom: '1rem' }}>
              Manage users, audit changes, and oversee the platform.
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <button
                onClick={() => onNavigate('/admin/users')}
                className="btn btn-destructive"
                style={{ padding: '0.5rem 1rem' }}
              >
                👥 Users
              </button>
              <button
                onClick={() => onNavigate('/admin/audit')}
                className="btn btn-destructive"
                style={{ padding: '0.5rem 1rem' }}
              >
                📋 Audit Log
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Stats Section */}
      <div style={{ marginTop: '3rem', textAlign: 'center' }}>
        <h3 style={{ marginBottom: '2rem', color: 'var(--foreground)' }}>Platform Impact</h3>
        <div className="grid grid-auto-fit" style={{ gap: '2rem' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>∞</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Potential Translations</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--accent)' }}>👥</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Community Driven</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--accent-foreground)' }}>🌍</div>
            <div style={{ color: 'var(--muted-foreground)' }}>Cultural Preservation</div>
          </div>
        </div>
      </div>
    </div>
  );
}