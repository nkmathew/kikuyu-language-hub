'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import Navigation from '../components/Navigation';
import HomePage from '../components/pages/HomePage';
import LoginPage from '../components/pages/LoginPage';
import SignupPage from '../components/pages/SignupPage';
import DashboardPage from '../components/pages/DashboardPage';
import ContributionsPage from '../components/pages/ContributionsPage';
import NewContributionPage from '../components/pages/NewContributionPage';
import ModeratorPage from '../components/pages/ModeratorPage';
import AdminUsersPage from '../components/pages/AdminUsersPage';
import AdminAuditPage from '../components/pages/AdminAuditPage';

export default function App() {
  const { user, loading } = useAuth();
  const [currentPath, setCurrentPath] = useState('/');
  const [isInitialized, setIsInitialized] = useState(false);

  // Initialize from URL on first load
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const path = window.location.pathname || '/';
      setCurrentPath(path);
      setIsInitialized(true);
    }
  }, []);

  // Update URL when path changes
  useEffect(() => {
    if (isInitialized && typeof window !== 'undefined') {
      window.history.pushState({}, '', currentPath);
    }
  }, [currentPath, isInitialized]);

  // Handle browser back/forward buttons
  useEffect(() => {
    if (!isInitialized) return;

    const handlePopState = () => {
      setCurrentPath(window.location.pathname);
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [isInitialized]);

  const handleNavigate = (path: string) => {
    setCurrentPath(path);
  };

  // Route protection logic
  const getProtectedRoute = () => {
    if (!user && !loading) {
      // User is not authenticated, redirect to home
      if (currentPath !== '/' && currentPath !== '/login' && currentPath !== '/signup') {
        handleNavigate('/');
        return 'home';
      }
    }
    return null;
  };

  const protectedRoute = getProtectedRoute();

  // Show loading screen while checking auth
  if (loading || !isInitialized) {
    return (
      <div className="container" style={{ 
        padding: '2rem', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        minHeight: '100vh' 
      }}>
        <div style={{ textAlign: 'center' }}>
          <div className="spinner" style={{ margin: '0 auto 1rem' }}></div>
          <p style={{ color: 'var(--muted-foreground)' }}>Loading Kikuyu Language Hub...</p>
        </div>
      </div>
    );
  }

  // Render current route
  const renderCurrentRoute = () => {
    if (protectedRoute === 'home') {
      return <HomePage onNavigate={handleNavigate} />;
    }

    switch (currentPath) {
      case '/':
        return <HomePage onNavigate={handleNavigate} />;
      
      case '/login':
        return <LoginPage onNavigate={handleNavigate} />;
      
      case '/signup':
        return <SignupPage onNavigate={handleNavigate} />;
      
      case '/dashboard':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <DashboardPage onNavigate={handleNavigate} />;
      
      case '/contributions':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <ContributionsPage onNavigate={handleNavigate} />;
      
      case '/contributions/new':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <NewContributionPage onNavigate={handleNavigate} />;
      
      case '/moderator':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <ModeratorPage onNavigate={handleNavigate} />;
      
      case '/admin/users':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <AdminUsersPage onNavigate={handleNavigate} />;
      
      case '/admin/audit':
        if (!user) return <HomePage onNavigate={handleNavigate} />;
        return <AdminAuditPage onNavigate={handleNavigate} />;
      
      default:
        // 404 - Unknown route
        return (
          <div className="container" style={{ padding: '2rem', textAlign: 'center' }}>
            <h1 style={{ color: 'var(--destructive)', marginBottom: '1rem' }}>404 - Page Not Found</h1>
            <p style={{ color: 'var(--muted-foreground)', marginBottom: '2rem' }}>
              The page you're looking for doesn't exist.
            </p>
            <button 
              onClick={() => handleNavigate('/')}
              className="btn btn-primary"
            >
              Go Home
            </button>
          </div>
        );
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--background)' }}>
      {/* Show navigation on all pages except login/signup */}
      {currentPath !== '/login' && currentPath !== '/signup' && (
        <Navigation currentPath={currentPath} onNavigate={handleNavigate} />
      )}
      
      {/* Main content */}
      <main>
        {renderCurrentRoute()}
      </main>

      {/* Footer */}
      {currentPath !== '/login' && currentPath !== '/signup' && (
        <footer style={{
          marginTop: '4rem',
          padding: '2rem',
          borderTop: '1px solid var(--border)',
          textAlign: 'center',
          backgroundColor: 'var(--card)'
        }}>
          <p style={{ color: 'var(--muted-foreground)', marginBottom: '1rem' }}>
            © 2024 Kikuyu Language Hub - Preserving language through community collaboration
          </p>
          <div style={{ display: 'flex', gap: '2rem', justifyContent: 'center', flexWrap: 'wrap' }}>
            <button 
              onClick={() => handleNavigate('/')}
              style={{ 
                background: 'none', 
                border: 'none', 
                color: 'var(--muted-foreground)', 
                textDecoration: 'underline',
                cursor: 'pointer'
              }}
            >
              Home
            </button>
            <button 
              onClick={() => handleNavigate('/contributions')}
              style={{ 
                background: 'none', 
                border: 'none', 
                color: 'var(--muted-foreground)', 
                textDecoration: 'underline',
                cursor: 'pointer'
              }}
            >
              Contribute
            </button>
            <button 
              onClick={() => window.open('https://github.com', '_blank')}
              style={{ 
                background: 'none', 
                border: 'none', 
                color: 'var(--muted-foreground)', 
                textDecoration: 'underline',
                cursor: 'pointer'
              }}
            >
              GitHub
            </button>
          </div>
        </footer>
      )}
    </div>
  );
}