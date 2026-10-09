import { FormEvent, useCallback, useEffect, useState } from 'react';
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

  const loadHotels = useCallback(async (searchCity: string, inDate: string, outDate: string) => {
    setLoading(true);
    setError('');
    try {
      const foundHotels = await api.getHotels(searchCity.trim());
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
        setRooms(Object.fromEntries(results));
      } else {
        setRooms({});
      }
    } catch (err) {
      setHotels([]);
      setRooms({});
      setError(err instanceof Error ? err.message : 'Could not load hotels. Check that the backend is running.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const searchCity = params.get('city') ?? 'Goa';
    const inDate = params.get('checkIn') ?? '';
    const outDate = params.get('checkOut') ?? '';
    setCity(searchCity);
    setCheckIn(inDate);
    setCheckOut(outDate);
    void loadHotels(searchCity, inDate, outDate);
  }, [params, loadHotels]);

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
