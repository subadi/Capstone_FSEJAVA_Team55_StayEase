import type {AuthResponse,Booking,Hotel,Room,SearchParams,User,ApiErrorShape} from '../types';
import {request} from './http';

function decodeJwtUser(token:string): AuthResponse['user'] {
  const payload = token.split('.')[1];
  if (!payload) throw new Error('Invalid authentication token');
  const json = JSON.parse(atob(payload.replace(/-/g,'+').replace(/_/g,'/')));
  return { id: json.sub, email: json.email, name: json.name, role: json.role };
}

export const api = {
  async register(x:{email:string;password:string;name:string}):Promise<void> {
    await request<void>('/auth/register',{method:'POST',body:JSON.stringify(x)});
  },
  async login(x:{email:string;password:string}):Promise<AuthResponse> {
    const response=await request<{token:string}>('/auth/login',{method:'POST',body:JSON.stringify(x)});
    const user=decodeJwtUser(response.token);
    localStorage.setItem('stayease_token',response.token);
    return {token:response.token,user};
  },
  logout:()=>request<void>('/auth/logout',{method:'POST'}),
  getHotels:(city:string)=>request<Hotel[]>(`/hotels?city=${encodeURIComponent(city)}`),
  getAvailableRooms:(id:string,p:Pick<SearchParams,'checkIn'|'checkOut'>)=>request<Room[]>(`/hotels/${id}/rooms?checkIn=${encodeURIComponent(p.checkIn)}&checkOut=${encodeURIComponent(p.checkOut)}`),
  createBooking:(x:{roomId:string;checkInDate:string;checkOutDate:string})=>request<Booking>('/bookings',{method:'POST',body:JSON.stringify(x)}),
  getMyBookings:()=>request<Booking[]>('/bookings/mine'),
  cancelBooking:(id:string)=>request<Booking>(`/bookings/${id}/cancel`,{method:'PUT'}),
  getManagerRooms:()=>request<Room[]>('/manager/rooms'),
  createRoom:(hotelId:string,x:Omit<Room,'id'|'hotelId'>)=>request<Room>(`/hotels/${hotelId}/rooms`,{method:'POST',body:JSON.stringify(x)}),
  updateRoom:(id:string,x:Partial<Room>)=>request<Room>(`/rooms/${id}`,{method:'PUT',body:JSON.stringify(x)}),
  deleteRoom:(id:string)=>request<void>(`/rooms/${id}`,{method:'DELETE'}),
  toggleRoom:(id:string,isActive:boolean)=>request<Room>(`/rooms/${id}/status`,{method:'PATCH',body:JSON.stringify({isActive})}),
  getManagerBookings:()=>request<Booking[]>('/manager/bookings?upcoming=true'),
  adminCreateHotel:(x:Omit<Hotel,'id'|'startingPrice'>)=>request<Hotel>('/hotels',{method:'POST',body:JSON.stringify(x)}),
  adminUpdateHotel:(id:string,x:Partial<Hotel>)=>request<Hotel>(`/hotels/${id}`,{method:'PUT',body:JSON.stringify(x)}),
  adminDeleteHotel:(id:string)=>request<void>(`/hotels/${id}`,{method:'DELETE'})
};
export type Api=typeof api;
export type ApiError=ApiErrorShape;
