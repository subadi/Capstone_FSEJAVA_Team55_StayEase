export type Role = 'GUEST' | 'MANAGER' | 'ADMIN';
export type RoomType = 'Single' | 'Double' | 'Suite';
export type BookingStatus = 'CONFIRMED' | 'CANCELLED';
export interface User { id:string; email:string; password?:string; name:string; role:Role; }
export interface Hotel { id:string; name:string; city:string; starRating:number; description:string; coverImageUrl:string; managerId:string; amenities:string[]; startingPrice?:number; }
export interface Room { id:string; hotelId:string; roomNumber:string; roomType:RoomType; pricePerNight:number; maxOccupancy:number; description:string; imageUrl:string; isActive:boolean; }
export interface Booking { id:string; bookingRef:string; roomId:string; hotelId:string; userId:string; checkInDate:string; checkOutDate:string; totalPrice:number; status:BookingStatus; createdAt:string; hotelName?:string; roomType?:RoomType; roomNumber?:string; }
export interface AuthResponse { token:string; user:Omit<User,'password'>; }
export interface ApiErrorShape { timestamp:string; path:string; error:string; message:string; status?:number; }
export interface SearchParams { city:string; checkIn:string; checkOut:string; }
