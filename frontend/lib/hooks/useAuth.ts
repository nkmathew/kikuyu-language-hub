'use client';

import { useState, useEffect } from 'react';
import { User } from '../types';
import { getCurrentUser, isAuthenticated } from '../auth';

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadUser() {
      if (isAuthenticated()) {
        try {
          const currentUser = await getCurrentUser();
          setUser(currentUser);
        } catch (error) {
          console.error('Failed to load user:', error);
          setUser(null);
        }
      }
      setLoading(false);
    }

    loadUser();
  }, []);

  const refreshUser = async () => {
    setLoading(true);
    try {
      const currentUser = await getCurrentUser();
      setUser(currentUser);
    } catch (error) {
      console.error('Failed to refresh user:', error);
      setUser(null);
    }
    setLoading(false);
  };

  return {
    user,
    loading,
    isAuthenticated: !!user,
    refreshUser,
  };
}