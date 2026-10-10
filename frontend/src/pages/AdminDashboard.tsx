import { FormEvent, useEffect, useState } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { Hotel } from '../types';

const empty = {
  name: '', city: 'Goa', starRating: 3, description: '',
  coverImageUrl: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=85',
  managerId: '', amenities: [] as string[], imageUrls: [] as string[],
};
const lines = (value: string) => value.split(/\n|,/).map((item) => item.trim()).filter(Boolean);

export function AdminDashboard() {
  const { user } = useAuth();
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [form, setForm] = useState(empty);
  const [amenitiesText, setAmenitiesText] = useState('');
  const [imagesText, setImagesText] = useState('');
  const [editing, setEditing] = useState<Hotel | null>(null);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const load = async () => {
    try {
      const result = await api.getHotels('');
      setHotels(result);
      setForm((current) => ({ ...current, managerId: current.managerId || result[0]?.managerId || '' }));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not load hotels');
    }
  };
  useEffect(() => { void load(); }, []);

  const resetForm = () => {
    setEditing(null);
    setForm({ ...empty, managerId: hotels[0]?.managerId ?? '' });
    setAmenitiesText('');
    setImagesText('');
  };
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    try {
      setError('');
      const payload = {
        name: form.name.trim(), city: form.city.trim(), starRating: form.starRating,
        description: form.description.trim(), coverImageUrl: form.coverImageUrl.trim(),
        managerId: form.managerId.trim(), amenities: lines(amenitiesText), imageUrls: lines(imagesText),
      };
      if (editing) await api.adminUpdateHotel(editing.id, payload);
      else await api.adminCreateHotel(payload);
      resetForm();
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not save hotel');
    } finally { setSaving(false); }
  };

  const startEdit = (hotel: Hotel) => {
    setEditing(hotel);
    setForm({ ...empty, ...hotel, imageUrls: hotel.imageUrls ?? [] });
    setAmenitiesText(hotel.amenities.join(', '));
    setImagesText((hotel.imageUrls ?? []).join('\n'));
  };

  void user;
  return <section className="admin-page">
    <div className="section-head"><div><p className="eyebrow">STAYEASE · ADMIN CONSOLE</p><h1>Hotel Directory</h1><p className="muted">Manage properties, photos and guest-facing details.</p></div><span className="admin-count">{hotels.length} properties</span></div>
    {error && <div className="alert error" role="alert">{error}</div>}
    <div className="panel admin-form-panel"><h2>{editing ? 'Edit hotel' : 'Add a property'}</h2><p className="muted">Add a strong cover photo and extra gallery photos for a more complete listing.</p>
      <form className="form-grid" onSubmit={submit}>
        <label>Hotel name<input required value={form.name} onChange={(e) => setForm((current) => ({ ...current, name: e.target.value }))} placeholder="e.g. The Palm Retreat" /></label>
        <label>City<input required value={form.city} onChange={(e) => setForm((current) => ({ ...current, city: e.target.value }))} placeholder="e.g. Mumbai" /></label>
        <label>Star rating<select value={form.starRating} onChange={(e) => setForm((current) => ({ ...current, starRating: Number(e.target.value) }))}>{[1,2,3,4,5].map((n) => <option key={n} value={n}>{n} star{n > 1 ? 's' : ''}</option>)}</select></label>
        <label>Manager ID<input required value={form.managerId} onChange={(e) => setForm((current) => ({ ...current, managerId: e.target.value }))} placeholder="UUID of a registered manager" /><small>Use the UUID of an existing manager account.</small></label>
        <label className="wide">Cover image URL<input required type="url" value={form.coverImageUrl} onChange={(e) => setForm((current) => ({ ...current, coverImageUrl: e.target.value }))} /></label>
        <label className="wide">Gallery image URLs <span className="muted">(one URL per line, or comma separated)</span><textarea rows={4} value={imagesText} onChange={(e) => setImagesText(e.target.value)} placeholder={'https://.../lobby.jpg\nhttps://.../pool.jpg\nhttps://.../room.jpg'} /></label>
        <label className="wide">Amenities <span className="muted">(comma separated)</span><input value={amenitiesText} onChange={(e) => setAmenitiesText(e.target.value)} placeholder="Wi-Fi, Parking, Pool, Restaurant" /></label>
        <label className="wide">Description<textarea value={form.description} onChange={(e) => setForm((current) => ({ ...current, description: e.target.value }))} placeholder="Describe the property and its location" /></label>
        <div className="image-preview wide"><img src={form.coverImageUrl || '/hotel-placeholder.svg'} alt="Hotel cover preview" onError={(e) => { e.currentTarget.src = '/hotel-placeholder.svg'; }} /><div className="preview-caption"><strong>Cover preview</strong><span>{lines(imagesText).length} gallery photo(s) added</span></div></div>
        {lines(imagesText).length > 0 && <div className="admin-gallery-preview wide">{lines(imagesText).map((url, i) => <img key={`${url}-${i}`} src={url} alt={`Gallery preview ${i + 1}`} onError={(e) => { e.currentTarget.src = '/hotel-placeholder.svg'; }} />)}</div>}
        <div className="actions wide"><button className="btn" disabled={saving}>{saving ? 'Saving…' : editing ? 'Update hotel' : 'Add hotel'}</button>{editing && <button type="button" className="btn secondary" onClick={resetForm}>Cancel edit</button>}</div>
      </form>
    </div>
    <div className="section-head"><h2>All properties</h2></div>
    <div className="table-wrap"><table><thead><tr><th>Hotel</th><th>City</th><th>Stars</th><th>Manager</th><th>Starting price</th><th>Actions</th></tr></thead><tbody>{hotels.map((hotel) => <tr key={hotel.id}><td><div className="table-hotel"><img src={hotel.coverImageUrl || '/hotel-placeholder.svg'} alt="" onError={(e) => { e.currentTarget.src = '/hotel-placeholder.svg'; }} /><span>{hotel.name}</span></div></td><td>{hotel.city}</td><td>{hotel.starRating} ★</td><td>{hotel.managerId}</td><td>{hotel.startingPrice ? `₹${hotel.startingPrice.toLocaleString('en-IN')} / night` : 'Rooms not added yet'}</td><td><button className="btn small secondary" onClick={() => startEdit(hotel)}>Edit</button> <button className="btn small danger" onClick={async () => { if (confirm('Delete hotel?')) { try { setError(''); await api.adminDeleteHotel(hotel.id); await load(); } catch (e) { setError(e instanceof Error ? e.message : 'Could not delete hotel'); } } }}>Delete</button></td></tr>)}</tbody></table></div>
  </section>;
}
