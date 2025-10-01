import { apiPost, apiGet, getToken, setToken, removeToken } from '../api/client';
import { LoginRequest, SignupRequest, Token, User } from '../types';

// Legacy token functions (keeping for compatibility)
export function getAccessToken(): string | null {
  return getToken();
}

export function setAccessToken(token: string) {
  setToken(token);
}

export function clearAccessToken() {
  removeToken();
}

// Auth API functions
export async function login(credentials: LoginRequest): Promise<Token> {
  const token = await apiPost<Token>('/auth/login', credentials);
  setToken(token.access_token);
  return token;
}

export async function signup(userData: SignupRequest): Promise<User> {
  return apiPost<User>('/auth/signup', userData);
}

export async function logout(): Promise<void> {
  removeToken();
  if (typeof window !== 'undefined') {
    window.location.href = '/login';
  }
}

export async function getCurrentUser(): Promise<User | null> {
  const token = getToken();
  if (!token) return null;
  
  try {
    return await apiGet<User>('/auth/me');
  } catch (error) {
    removeToken();
    return null;
  }
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}

export function hasRole(user: User | null, roles: string[]): boolean {
  if (!user) return false;
  return roles.includes(user.role);
}

export function isAdmin(user: User | null): boolean {
  return hasRole(user, ['admin']);
}

export function isModerator(user: User | null): boolean {
  return hasRole(user, ['admin', 'moderator']);
}

export function isContributor(user: User | null): boolean {
  return hasRole(user, ['admin', 'moderator', 'contributor']);
}


