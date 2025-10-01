// Auth types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
}

export interface Token {
  access_token: string;
  token_type: string;
}

// User types
export type UserRole = 'admin' | 'moderator' | 'contributor';

export interface User {
  id: number;
  email: string;
  role: UserRole;
  created_at: string;
  updated_at: string;
}

// Contribution types
export type ContributionStatus = 'pending' | 'approved' | 'rejected';

export interface ContributionCreate {
  source_text: string;
  target_text: string;
  language?: string;
}

export interface Contribution {
  id: number;
  source_text: string;
  target_text: string;
  status: ContributionStatus;
  language: string;
  created_by_id: number;
  created_at: string;
  updated_at: string;
}

export interface ContributionUpdate {
  status: ContributionStatus;
  reason?: string;
}

// Export types
export interface TranslationsExport {
  translations: Record<string, string>;
  count: number;
  language: string;
}

// API Error type
export interface ApiError {
  detail: string;
}