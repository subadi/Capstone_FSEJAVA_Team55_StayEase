import type { ApiErrorShape } from '../types';

const base = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(public readonly details: ApiErrorShape) {
    super(details.message);
    this.name = 'ApiError';
  }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('stayease_token');
  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);

  let response: Response;
  try {
    response = await fetch(`${base}${path}`, { ...options, headers });
  } catch {
    throw new Error(`Cannot reach the StayEase API at ${base}. Start the backend and check VITE_API_BASE_URL.`);
  }

  if (response.status === 204) return undefined as T;
  const body = await response.json().catch(() => null);
  if (!response.ok) {
    const details = (body ?? {}) as Partial<ApiErrorShape>;
    const message = response.status === 401
      ? (details.message ?? 'Your session is missing or expired. Please sign in again.')
      : response.status === 403
        ? (details.message ?? 'You do not have permission to perform this action.')
        : (details.message ?? `Request failed (${response.status}).`);
    throw new ApiError({
      timestamp: details.timestamp ?? new Date().toISOString(),
      path: details.path ?? path,
      error: details.error ?? 'HTTP_ERROR',
      message,
      status: response.status,
    });
  }
  return body as T;
}
