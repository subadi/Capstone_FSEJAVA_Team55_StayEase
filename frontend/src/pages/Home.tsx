import { FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../services/api';
import type { Hotel, Room } from '../types';
import { HotelCard } from '../components/HotelCard';

export function Home() {
  const [params, setParams] = useSearchParams();
  const [city, setCity] = useState(params.get('city') ?? 'Goa');
  const [checkIn, setCheckIn] = useState(params.get('checkIn') ?? '');
  const [checkOut, setCheckOut] = useState(params.get('checkOut') ?? '');
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [rooms, setRooms] = useState<Record<string, Room[]>>({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const latestRequest = useRef(0);

  const loadHotels = useCallback(async (searchCity: string, inDate: string, outDate: string) => {
    const requestId = ++latestRequest.current;
    setLoading(true);
    setError('');
    try {
      const foundHotels = await api.getHotels(searchCity.trim());
      if (requestId !== latestRequest.current) return;
      setHotels(foundHotels);
      if (inDate && outDate && inDate < outDate) {
        const results = await Promise.all(foundHotels.map(async (hotel) => {
          try {
            return [hotel.id, await api.getAvailableRooms(hotel.id, { checkIn: inDate, checkOut: outDate })] as const;
          } catch {
            // Keep the hotel visible if one hotel's availability request fails.
            return [hotel.id, []] as const;
          }
        }));
        if (requestId === latestRequest.current) setRooms(Object.fromEntries(results));
      } else {
        setRooms({});
      }
    } catch (err) {
      if (requestId !== latestRequest.current) return;
      setHotels([]);
      setRooms({});
      setError(err instanceof Error ? err.message : 'Could not load hotels. Check that the backend is running.');
    } finally {
      if (requestId === latestRequest.current) setLoading(false);
    }
  }, []);

  // Keep URL-driven searches (shared links/back-forward navigation) in sync with the form.
  useEffect(() => {
    setCity(params.get('city') ?? 'Goa');
    setCheckIn(params.get('checkIn') ?? '');
    setCheckOut(params.get('checkOut') ?? '');
  }, [params]);

  // Search as the destination is typed, with a short debounce to avoid a request per keystroke.
  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadHotels(city, checkIn, checkOut);
    }, 250);
    return () => window.clearTimeout(timer);
  }, [city, checkIn, checkOut, loadHotels]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (checkIn >= checkOut) {
      setError('Check-out date must be after check-in date.');
      return;
    }
    setParams({ city: city.trim(), checkIn, checkOut });
  };

  return (
    <section>
      <div className="hero">
        <p className="eyebrow">BOOK YOUR STAY</p>
        <h1>Find a room that feels right.</h1>
        <p>Search hotels, compare available rooms and confirm your stay in minutes.</p>
        <form className="search" onSubmit={submit}>
          <label>City<input value={city} onChange={(event) => setCity(event.target.value)} placeholder="e.g. Goa" required /></label>
          <label>Check-in<input type="date" value={checkIn} min={new Date().toISOString().slice(0, 10)} onChange={(event) => setCheckIn(event.target.value)} required /></label>
          <label>Check-out<input type="date" value={checkOut} min={checkIn || new Date().toISOString().slice(0, 10)} onChange={(event) => setCheckOut(event.target.value)} required /></label>
          <button className="btn" disabled={loading || !city.trim() || !checkIn || !checkOut || checkIn >= checkOut}>{loading ? 'Searching…' : 'Search Hotels'}</button>
        </form>
      </div>
      {error && <div className="alert error" role="alert">{error}</div>}
      <div className="section-head"><h2>Hotels in {city || 'your destination'}</h2><span>{loading ? 'Searching…' : `${hotels.length} result${hotels.length === 1 ? '' : 's'}`}</span></div>
      <div className="grid">
        {hotels.map((hotel) => <HotelCard key={hotel.id} hotel={hotel} rooms={rooms[hotel.id] ?? []} checkIn={checkIn} checkOut={checkOut} />)}
        {!loading && !hotels.length && <div className="empty">{error ? 'Hotels could not be loaded.' : 'No hotels found for this city. Try another city.'}</div>}
      </div>
    </section>
  );
}
