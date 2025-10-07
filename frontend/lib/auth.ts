import { apiPost, apiGet } from './api/client';
import { getToken, setToken, removeToken } from './api/client';
import { LoginRequest, SignupRequest, Token, User } from './types';

export async function login(credentials: LoginRequest): Promise<{ user: User; token: Token }> {
  const response = await apiPost<{ access_token: string; token_type: string; user: User }>('/auth/login', credentials);
  
  const token = {
    access_token: response.access_token,
    token_type: response.token_type
  };
  
  setToken(response.access_token);
  
  return {
    user: response.user,
    token
  };
}

export async function signup(data: SignupRequest): Promise<{ user: User; token: Token }> {
  const response = await apiPost<{ access_token: string; token_type: string; user: User }>('/auth/register', data);
  
  const token = {
    access_token: response.access_token,
    token_type: response.token_type
  };
  
  setToken(response.access_token);
  
  return {
    user: response.user,
    token
  };
}

export async function getCurrentUser(): Promise<User | null> {
  try {
    const user = await apiGet<User>('/auth/me');
    return user;
  } catch (error) {
    console.error('Failed to get current user:', error);
    return null;
  }
}

export function isAuthenticated(): boolean {
  return !!getToken();
}

export function logout(): void {
  removeToken();
  if (typeof window !== 'undefined') {
    window.location.href = '/login';
  }
}

export function isAdmin(user: User | null): boolean {
  return user?.role === 'admin';
}

export function isModerator(user: User | null): boolean {
  return user?.role === 'moderator' || user?.role === 'admin';
}