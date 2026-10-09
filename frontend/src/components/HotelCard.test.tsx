import {render,screen} from '@testing-library/react';import {MemoryRouter} from 'react-router-dom';import {describe,it,expect} from 'vitest';import {HotelCard} from './HotelCard';
const hotel={id:'h1',name:'Test Hotel',city:'Goa',starRating:4,description:'Nice',coverImageUrl:'/x.jpg',managerId:'m',amenities:[]};const rooms=[{id:'r1',hotelId:'h1',roomNumber:'1',roomType:'Single' as const,pricePerNight:1500,maxOccupancy:1,description:'',imageUrl:'',isActive:true}];
describe('HotelCard',()=>it('shows starting room price and preserves selected dates',()=>{render(<MemoryRouter><HotelCard hotel={hotel} rooms={rooms} checkIn="2026-11-01" checkOut="2026-11-03"/></MemoryRouter>);expect(screen.getByText('₹1,500 / night')).toBeInTheDocument();expect(screen.getByRole('link',{name:'View Rooms'})).toHaveAttribute('href','/hotels/h1?checkIn=2026-11-01&checkOut=2026-11-03')}));


describe('HotelCard starting price fallback',()=>it('shows hotel starting price when date-based rooms are not loaded',()=>{render(<MemoryRouter><HotelCard hotel={{...hotel,startingPrice:3500}} rooms={[]}/></MemoryRouter>);expect(screen.getByText('₹3,500 / night')).toBeInTheDocument()}));

describe('HotelCard price behavior',()=>it('shows rooms-not-added instead of fake zero when no price exists',()=>{render(<MemoryRouter><HotelCard hotel={hotel} rooms={[]}/></MemoryRouter>);expect(screen.getByText('Rooms not added yet')).toBeInTheDocument()}));
