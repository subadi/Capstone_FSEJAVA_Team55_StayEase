import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AdminDashboard } from './AdminDashboard';

const { getHotels, adminCreateHotel, adminUpdateHotel, adminDeleteHotel } = vi.hoisted(() => ({
  getHotels: vi.fn(),
  adminCreateHotel: vi.fn(),
  adminUpdateHotel: vi.fn(),
  adminDeleteHotel: vi.fn(),
}));

vi.mock('../services/api', () => ({ api: { getHotels, adminCreateHotel, adminUpdateHotel, adminDeleteHotel } }));
vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'a', email: 'a', name: 'Admin', role: 'ADMIN' }, login: vi.fn(), register: vi.fn(), logout: vi.fn() }),
}));

const initialHotel = {
  id: 'h1', name: 'City Inn', city: 'Goa', starRating: 3, description: '',
  coverImageUrl: 'https://example.com/cover.jpg', managerId: 'u-manager', amenities: [], imageUrls: [],
};

describe('AdminDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getHotels.mockResolvedValue([initialHotel]);
    adminCreateHotel.mockResolvedValue({});
    adminUpdateHotel.mockResolvedValue({});
    adminDeleteHotel.mockResolvedValue(undefined);
  });

  it('renders hotels and creates a hotel', async () => {
    const user = userEvent.setup();
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);

    expect(await screen.findByText('City Inn')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Hotel name'), 'New Hotel');
    await user.click(screen.getByRole('button', { name: 'Add hotel' }));

    expect(adminCreateHotel).toHaveBeenCalledWith(expect.objectContaining({
      name: 'New Hotel', city: 'Goa', managerId: 'u-manager',
    }));
  });

  it('keeps city independent when editing hotel name and submits multiple gallery URLs', async () => {
    const user = userEvent.setup();
    render(<MemoryRouter><AdminDashboard /></MemoryRouter>);
    expect(await screen.findByText('City Inn')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Edit' }));
    const name = screen.getByLabelText('Hotel name');
    await user.clear(name);
    await user.type(name, 'Mumbai Grand');
    expect(screen.getByLabelText('City')).toHaveValue('Goa');

    const gallery = screen.getByLabelText(/Gallery image URLs/);
    await user.clear(gallery);
    await user.type(gallery, 'https://example.com/pool.jpg\nhttps://example.com/room.jpg');
    await user.click(screen.getByRole('button', { name: 'Update hotel' }));

    expect(adminUpdateHotel).toHaveBeenCalledWith('h1', expect.objectContaining({
      name: 'Mumbai Grand', city: 'Goa',
      imageUrls: ['https://example.com/pool.jpg', 'https://example.com/room.jpg'],
    }));
  });
});
