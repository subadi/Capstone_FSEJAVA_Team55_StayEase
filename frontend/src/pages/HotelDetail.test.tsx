import {render,screen} from '@testing-library/react';
import {MemoryRouter,Route,Routes} from 'react-router-dom';
import {describe,it,expect,vi,beforeEach} from 'vitest';
import userEvent from '@testing-library/user-event';
import {HotelDetail} from './HotelDetail';
const getHotels=vi.fn(),getAvailableRooms=vi.fn(),createBooking=vi.fn();
vi.mock('../services/api',()=>({api:{getHotels,getAvailableRooms,createBooking}}));
vi.mock('../context/AuthContext',()=>({useAuth:()=>({user:{id:'u',email:'e',name:'Guest',role:'GUEST'},login:vi.fn(),register:vi.fn(),logout:vi.fn()})}));
describe('HotelDetail',()=>{beforeEach(()=>{getHotels.mockResolvedValue([{id:'h1',name:'Sunset Palms',city:'Goa',starRating:4,description:'Beach',coverImageUrl:'',managerId:'m',amenities:['Pool'],startingPrice:3500}]);getAvailableRooms.mockResolvedValue([{id:'r1',hotelId:'h1',roomNumber:'101',roomType:'Double',pricePerNight:3500,maxOccupancy:2,description:'Nice',imageUrl:'',isActive:true}]);createBooking.mockResolvedValue({bookingRef:'SE-123',totalPrice:7000});});it('loads room and confirms booking',async()=>{const u=userEvent.setup();render(<MemoryRouter initialEntries={['/hotels/h1?checkIn=2026-11-01&checkOut=2026-11-03']}><Routes><Route path='/hotels/:id' element={<HotelDetail/>}/><Route path='/stays' element={<div>Stays</div>}/></Routes></MemoryRouter>);expect(await screen.findByText('Sunset Palms')).toBeInTheDocument();await u.click(screen.getByRole('button',{name:'Book'}));expect(screen.getByText(/Total: ₹7,000/)).toBeInTheDocument();await u.click(screen.getByRole('button',{name:'Confirm Booking'}));expect(createBooking).toHaveBeenCalledWith({roomId:'r1',checkInDate:'2026-11-01',checkOutDate:'2026-11-03'});});});
