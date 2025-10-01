'use client';

import { useState, useEffect } from 'react';

interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export default function PWAInstaller() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showInstallBanner, setShowInstallBanner] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);

  useEffect(() => {
    // Register service worker
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
          .then((registration) => {
            console.log('SW registered: ', registration);
          })
          .catch((registrationError) => {
            console.log('SW registration failed: ', registrationError);
          });
      });
    }

    // Check if app is already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true);
    }

    // Listen for beforeinstallprompt event
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      const beforeInstallEvent = e as BeforeInstallPromptEvent;
      setDeferredPrompt(beforeInstallEvent);
      setShowInstallBanner(true);
    };

    // Listen for app installation
    const handleAppInstalled = () => {
      setIsInstalled(true);
      setShowInstallBanner(false);
      setDeferredPrompt(null);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);

  const handleInstallClick = async () => {
    if (!deferredPrompt) return;

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    
    if (outcome === 'accepted') {
      console.log('User accepted the install prompt');
    } else {
      console.log('User dismissed the install prompt');
    }
    
    setDeferredPrompt(null);
    setShowInstallBanner(false);
  };

  const handleDismiss = () => {
    setShowInstallBanner(false);
  };

  if (isInstalled || !showInstallBanner) {
    return null;
  }

  return (
    <div style={{
      position: 'fixed',
      bottom: '1rem',
      left: '1rem',
      right: '1rem',
      backgroundColor: '#10b981',
      color: 'white',
      padding: '1rem',
      borderRadius: '8px',
      boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      zIndex: 1000,
      display: 'flex',
      alignItems: 'center',
      gap: '1rem'
    }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: '500', marginBottom: '0.25rem' }}>
          📱 Install Kikuyu Language Hub
        </div>
        <div style={{ fontSize: '0.875rem', opacity: 0.9 }}>
          Get quick access and work offline
        </div>
      </div>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <button
          onClick={handleDismiss}
          style={{
            backgroundColor: 'transparent',
            color: 'white',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '4px',
            padding: '0.5rem 0.75rem',
            fontSize: '0.875rem',
            cursor: 'pointer'
          }}
        >
          Later
        </button>
        <button
          onClick={handleInstallClick}
          style={{
            backgroundColor: 'rgba(255, 255, 255, 0.2)',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            padding: '0.5rem 0.75rem',
            fontSize: '0.875rem',
            cursor: 'pointer'
          }}
        >
          Install
        </button>
      </div>
    </div>
  );
}