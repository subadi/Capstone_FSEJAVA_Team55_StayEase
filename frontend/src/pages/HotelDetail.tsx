import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Modal } from '../components/Modal';
import type { Hotel, Room } from '../types';

export function HotelDetail() {
  const { id = '' } = useParams();
  const [params] = useSearchParams();
  const location = useLocation();
  const [hotel, setHotel] = useState<Hotel | null>(null);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [selected, setSelected] = useState<Room | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [booking, setBooking] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const checkIn = params.get('checkIn') ?? '';
  const checkOut = params.get('checkOut') ?? '';
  const validDates = Boolean(checkIn && checkOut && checkIn < checkOut);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    setHotel(null);
    setRooms([]);
    setSelected(null);
    setSuccess('');
    const load = async () => {
      try {
        const hotels = await api.getHotels('');
        const found = hotels.find((item) => item.id === id);
        if (!found) throw new Error('Hotel not found. It may have been removed.');
        if (!cancelled) setHotel(found);
        if (validDates) {
          const available = await api.getAvailableRooms(id, { checkIn, checkOut });
          if (!cancelled) setRooms(available);
        }
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Could not load hotel details.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    void load();
    return () => { cancelled = true; };
  }, [id, checkIn, checkOut, validDates]);

  const book = async () => {
    if (!selected || !validDates) return;
    if (!user) {
      navigate(`/login?returnTo=${encodeURIComponent(location.pathname + location.search)}`);
      return;
    }
    if (user.role !== 'GUEST') {
      setError('Only guest accounts can create bookings. Please sign in with a guest account.');
      return;
    }
    setBooking(true);
    setError('');
    try {
      const created = await api.createBooking({ roomId: selected.id, checkInDate: checkIn, checkOutDate: checkOut });
      setSuccess(`Booking confirmed. Your booking reference is ${created.bookingRef}.`);
      setSelected(null);
      const updatedRooms = await api.getAvailableRooms(id, { checkIn, checkOut });
      setRooms(updatedRooms);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Booking failed. Please try again.');
    } finally {
      setBooking(false);
    }
  };

  if (loading) return <div className="loading">Loading hotel details…</div>;
  if (!hotel) return <section>{error && <div className="alert error" role="alert">{error}</div>}</section>;
  const nights = validDates ? Math.ceil((Date.parse(checkOut) - Date.parse(checkIn)) / 86400000) : 0;

  return (
    <section>
      <img className="detail-cover" src={hotel.coverImageUrl || '/hotel-placeholder.svg'} alt={`${hotel.name} cover`} onError={(event) => { event.currentTarget.src = '/hotel-placeholder.svg'; }} />
      <div className="detail-header"><div><div className="stars">{'★'.repeat(hotel.starRating)}</div><h1>{hotel.name}</h1><p className="muted">{hotel.city} · {hotel.description}</p></div></div>
      <div className="two-col"><div><h2>Amenities</h2><div className="chips">{hotel.amenities.map((amenity) => <span key={amenity}>{amenity}</span>)}</div></div><div className="date-summary"><strong>Selected dates</strong><span>{checkIn || '—'} → {checkOut || '—'}</span></div></div>
      {error && <div className="alert error" role="alert">{error}</div>}
      {success && <div className="alert success" role="status">{success} <button className="link-btn" onClick={() => navigate('/stays')}>Go to My Stays</button></div>}
      <div className="section-head"><h2>Available Rooms</h2><span>{rooms.length} available</span></div>
      {!validDates ? <div className="empty">Choose valid check-in and check-out dates from hotel search to see room availability.</div> : <div className="room-list">
        {rooms.map((room) => <article className="room-row" key={room.id}>
          <img src={room.imageUrl || '/room-placeholder.svg'} alt={`${room.roomType} room`} onError={(event) => { event.currentTarget.src = '/room-placeholder.svg'; }} />
          <div><h3>{room.roomType} · Room {room.roomNumber}</h3><p>{room.description}</p><span>Up to {room.maxOccupancy} guests</span></div>
          <div className="price"><strong>₹{room.pricePerNight.toLocaleString('en-IN')}</strong><small>per night</small><button className="btn" onClick={() => { setError(''); setSelected(room); }}>Book</button></div>
        </article>)}
        {!rooms.length && <div className="empty">No rooms are available for these dates.</div>}
      </div>}
      {selected && <Modal title="Review booking" onClose={() => setSelected(null)}><p>{selected.roomType} · {checkIn} to {checkOut}</p><p>{nights} night(s) × ₹{selected.pricePerNight.toLocaleString('en-IN')}</p><h3>Total: ₹{(nights * selected.pricePerNight).toLocaleString('en-IN')}</h3><button className="btn full" onClick={book} disabled={booking}>{booking ? 'Confirming…' : 'Confirm Booking'}</button></Modal>}
    </section>
  );
}
