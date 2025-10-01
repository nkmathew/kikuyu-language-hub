const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:10000/api/v1';

// Token management
function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('access_token');
}

function setToken(token: string): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem('access_token', token);
}

function removeToken(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('access_token');
}

// Base fetch wrapper
async function apiFetch<T>(
  path: string, 
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
    cache: 'no-store',
  });

  if (!res.ok) {
    if (res.status === 401) {
      removeToken();
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
    }
    const text = await res.text();
    throw new Error(`${options.method || 'GET'} ${path} failed: ${res.status} ${text}`);
  }

  return (await res.json()) as T;
}

export async function apiGet<T>(path: string, init?: RequestInit): Promise<T> {
  return apiFetch<T>(path, { ...init, method: 'GET' });
}

export async function apiPost<T>(path: string, data?: any, init?: RequestInit): Promise<T> {
  return apiFetch<T>(path, {
    ...init,
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined,
  });
}

export async function apiPut<T>(path: string, data?: any, init?: RequestInit): Promise<T> {
  return apiFetch<T>(path, {
    ...init,
    method: 'PUT',
    body: data ? JSON.stringify(data) : undefined,
  });
}

export async function apiDelete<T>(path: string, init?: RequestInit): Promise<T> {
  return apiFetch<T>(path, { ...init, method: 'DELETE' });
}

// Auth helpers
export { getToken, setToken, removeToken };


