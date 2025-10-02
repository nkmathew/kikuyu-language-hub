import { useState, useEffect } from 'react';

export interface User {
  id: number;
  email: string;
  name: string;
  role: 'admin' | 'moderator' | 'contributor';
  isActive: boolean;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  token: string | null;
}

export const useAuth = () => {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    token: null,
  });

  useEffect(() => {
    // Check for existing token in localStorage
    const token = localStorage.getItem('auth_token');
    if (token) {
      // In a real app, you'd validate the token with the backend
      // For now, we'll create a mock user
      setAuthState({
        user: {
          id: 1,
          email: 'demo@kikuyu.hub',
          name: 'Demo User',
          role: 'contributor',
          isActive: true,
        },
        isAuthenticated: true,
        isLoading: false,
        token,
      });
    } else {
      setAuthState(prev => ({ ...prev, isLoading: false }));
    }
  }, []);

  const login = async (email: string, password: string) => {
    try {
      // Mock login - in real app, this would call the backend API
      const mockToken = 'mock-jwt-token';
      localStorage.setItem('auth_token', mockToken);
      
      setAuthState({
        user: {
          id: 1,
          email,
          name: email.split('@')[0],
          role: 'contributor',
          isActive: true,
        },
        isAuthenticated: true,
        isLoading: false,
        token: mockToken,
      });
      
      return { success: true };
    } catch (error) {
      return { success: false, error: 'Login failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    setAuthState({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      token: null,
    });
  };

  const register = async (email: string, password: string, name: string) => {
    try {
      // Mock registration - in real app, this would call the backend API
      const mockToken = 'mock-jwt-token';
      localStorage.setItem('auth_token', mockToken);
      
      setAuthState({
        user: {
          id: Date.now(), // Mock ID
          email,
          name,
          role: 'contributor',
          isActive: true,
        },
        isAuthenticated: true,
        isLoading: false,
        token: mockToken,
      });
      
      return { success: true };
    } catch (error) {
      return { success: false, error: 'Registration failed' };
    }
  };

  return {
    ...authState,
    login,
    logout,
    register,
  };
};

export default useAuth;