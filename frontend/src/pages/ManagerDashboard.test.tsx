import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ManagerDashboard } from './ManagerDashboard';

const { getHotels, getManagerRooms, getManagerBookings, createRoom, updateRoom, deleteRoom, toggleRoom } = vi.hoisted(() => ({
  getHotels: vi.fn(),
  getManagerRooms: vi.fn(),
  getManagerBookings: vi.fn(),
  createRoom: vi.fn(),
  updateRoom: vi.fn(),
  deleteRoom: vi.fn(),
  toggleRoom: vi.fn(),
}));

vi.mock('../services/api', () => ({ api: { getHotels, getManagerRooms, getManagerBookings, createRoom, updateRoom, deleteRoom, toggleRoom } }));
vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'u-manager', email: 'm', name: 'Manager', role: 'MANAGER' }, login: vi.fn(), register: vi.fn(), logout: vi.fn() }),
}));

const hotel = { id: 'h1', name: 'Hotel', city: 'Goa', starRating: 3, description: '', coverImageUrl: '', managerId: 'u-manager', amenities: [] };
const room = { id: 'r1', hotelId: 'h1', roomNumber: '101', roomType: 'Double', pricePerNight: 2000, maxOccupancy: 2, description: '', imageUrl: '', isActive: true };

describe('ManagerDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getHotels.mockResolvedValue([hotel]);
    getManagerRooms.mockResolvedValue([room]);
    getManagerBookings.mockResolvedValue([]);
    createRoom.mockResolvedValue({});
    updateRoom.mockResolvedValue({});
    deleteRoom.mockResolvedValue(undefined);
    toggleRoom.mockResolvedValue({});
  });

  it('renders room and supports add', async () => {
    const user = userEvent.setup();
    render(<MemoryRouter><ManagerDashboard /></MemoryRouter>);

    expect(await screen.findByText('101')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Room number'), '102');
    await user.clear(screen.getByLabelText('Price/night'));
    await user.type(screen.getByLabelText('Price/night'), '2500');
    await user.click(screen.getByRole('button', { name: 'Add room' }));

    expect(createRoom).toHaveBeenCalledWith('h1', expect.objectContaining({
      roomNumber: '102', pricePerNight: 2500, maxOccupancy: 2,
    }));
  });
});
