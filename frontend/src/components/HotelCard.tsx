import { Link } from 'react-router-dom';
import type { Hotel, Room } from '../types';

type Props = {
  hotel: Hotel;
  rooms: Room[];
  checkIn?: string;
  checkOut?: string;
};

export function HotelCard({ hotel, rooms, checkIn, checkOut }: Props) {
  // With selected dates, show the cheapest currently available room.
  // Before dates are selected, use the hotel's seeded/API-provided starting price.
  const from = rooms.length
    ? Math.min(...rooms.map((room) => room.pricePerNight))
    : hotel.startingPrice;

  const qs = new URLSearchParams();
  if (checkIn) qs.set('checkIn', checkIn);
  if (checkOut) qs.set('checkOut', checkOut);

  return (
    <article className="card hotel-card">
      <img src={hotel.coverImageUrl || '/hotel-placeholder.svg'} alt={`${hotel.name} cover`} loading="lazy" onError={(e) => { e.currentTarget.src = '/hotel-placeholder.svg'; }} />
      <div className="card-body">
        <div className="stars" aria-label={`${hotel.starRating} star hotel`}>
          {'★'.repeat(hotel.starRating)}
        </div>
        <h3>{hotel.name}</h3>
        <p className="muted">{hotel.city} · {hotel.description}</p>
        <strong>{from ? `₹${from.toLocaleString('en-IN')} / night` : 'Rooms not added yet'}</strong>
        <Link
          className="btn secondary"
          to={`/hotels/${hotel.id}${qs.toString() ? `?${qs}` : ''}`}
        >
          View Rooms
        </Link>
      </div>
    </article>
  );
}
