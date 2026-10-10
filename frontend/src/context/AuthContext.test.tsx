import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext';

const { login, register, logout } = vi.hoisted(() => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
}));

vi.mock('../services/api', () => ({ api: { login, register, logout } }));

function Probe() {
  const auth = useAuth();
  return (
    <>
      <button onClick={() => auth.login('e', 'p')}>login</button>
      <button onClick={() => auth.register('e', 'p', 'R')}>register</button>
      <button onClick={() => auth.logout()}>logout</button>
      <span>{auth.user?.name ?? 'none'}</span>
    </>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    login.mockResolvedValue({ token: 't', user: { id: '1', email: 'e', name: 'N', role: 'GUEST' } });
    register.mockResolvedValue({ token: 't', user: { id: '2', email: 'e', name: 'R', role: 'GUEST' } });
    logout.mockResolvedValue(undefined);
  });

  it('persists login/register state and clears on logout', async () => {
    const user = userEvent.setup();
    // AuthProvider logs in after successful registration, so return the appropriate
    // user for each login call instead of returning the same user every time.
    login
      .mockResolvedValueOnce({ token: 't1', user: { id: '1', email: 'e', name: 'N', role: 'GUEST' } })
      .mockResolvedValueOnce({ token: 't2', user: { id: '2', email: 'e', name: 'R', role: 'GUEST' } });

    render(<AuthProvider><Probe /></AuthProvider>);

    await user.click(screen.getByRole('button', { name: 'login' }));
    expect(await screen.findByText('N')).toBeInTheDocument();
    expect(localStorage.getItem('stayease_token')).toBe('t1');

    await user.click(screen.getByRole('button', { name: 'logout' }));
    expect(screen.getByText('none')).toBeInTheDocument();
    expect(localStorage.getItem('stayease_token')).toBeNull();

    await user.click(screen.getByRole('button', { name: 'register' }));
    expect(await screen.findByText('R')).toBeInTheDocument();
    expect(register).toHaveBeenCalledWith({ email: 'e', password: 'p', name: 'R' });
    expect(localStorage.getItem('stayease_user_id')).toBe('2');
  });
});
