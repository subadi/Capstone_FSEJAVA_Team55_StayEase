package com.stayease.service;

import com.stayease.dto.HotelDtos.HotelRequest;
import com.stayease.entity.Hotel;
import com.stayease.entity.Booking;
import com.stayease.repository.HotelRepository;
import com.stayease.repository.RoomRepository;
import com.stayease.repository.UserRepository;
import com.stayease.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.stayease.dto.HotelDtos.HotelResponse;
import com.stayease.dto.RoomDtos.RoomResponse;
import com.stayease.entity.Room;
import com.stayease.entity.User;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {
    @Mock
    HotelRepository hotels;
    @Mock
    UserRepository users;
    @Mock
    RoomRepository rooms;
    private HotelService service;
    private final UUID hotelId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new HotelService(hotels, users, rooms);
    }

    @Test
    void availableRoomsRejectsMissingOrInvalidDateRangeBeforeQuerying() {
        ApiException missing = assertThrows(ApiException.class, () -> service.availableRooms(hotelId, null, LocalDate.now().plusDays(2)));
        assertEquals("INVALID_DATES", missing.getError());
        ApiException sameDay = assertThrows(ApiException.class, () -> service.availableRooms(hotelId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(2)));
        assertEquals(HttpStatus.BAD_REQUEST, sameDay.getStatus());
        verifyNoInteractions(hotels, rooms);
    }

    @Test
    void availableRoomsRejectsUnknownHotel() {
        when(hotels.findById(hotelId)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.availableRooms(hotelId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("HOTEL_NOT_FOUND", ex.getError());
    }

    @Test
    void createRejectsUserWhoIsNotManager() {
        UUID managerId = UUID.randomUUID();
        var user = new com.stayease.entity.User();
        user.setRole("GUEST");
        when(users.findById(managerId)).thenReturn(Optional.of(user));
        HotelRequest request = new HotelRequest("Hotel", "Goa", 4, "Description", "https://example.com/hotel.jpg", managerId, List.of("Pool"));
        ApiException ex = assertThrows(ApiException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_MANAGER", ex.getError());
        verify(hotels, never()).save(any(Hotel.class));
    }

    @Test
    void updateRejectsUnknownHotel() {
        when(hotels.findById(hotelId)).thenReturn(Optional.empty());

        HotelRequest request = new HotelRequest(
                "Updated Hotel",
                "Goa",
                4,
                "Updated description",
                "https://example.com/hotel.jpg",
                UUID.randomUUID(),
                List.of("Pool")
        );

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.update(hotelId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("HOTEL_NOT_FOUND", ex.getError());
        verify(hotels, never()).save(any(Hotel.class));
    }

    @Test
    void deleteRejectsUnknownHotel() {
        when(hotels.findById(hotelId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.delete(hotelId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("HOTEL_NOT_FOUND", ex.getError());
        verify(hotels, never()).delete(any(Hotel.class));
    }

    @Test
    void listReturnsEmptyListWhenNoHotelsMatch() {
        when(hotels.search("Goa")).thenReturn(List.of());

        var result = service.list("Goa");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(hotels).search("Goa");
    }

    @Test
    void createSuccessfullyCreatesHotel() {
        UUID managerId = UUID.randomUUID();

        User manager = new User();
        manager.setRole("MANAGER");

        when(users.findById(managerId))
                .thenReturn(Optional.of(manager));

        when(hotels.save(any(Hotel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HotelRequest request = new HotelRequest(
                "  Test Hotel  ",
                "  Goa  ",
                4,
                "A hotel near the beach",
                "https://example.com/hotel.jpg",
                managerId,
                List.of("Pool", "WiFi")
        );

        HotelResponse result = service.create(request);

        assertNotNull(result);
        assertEquals("Test Hotel", result.name());
        assertEquals("Goa", result.city());
        assertEquals(4, result.starRating());
        assertEquals(List.of("Pool", "WiFi"), result.amenities());

        verify(hotels).save(any(Hotel.class));
    }

    @Test
    void updateSuccessfullyUpdatesExistingHotel() {
        UUID managerId = UUID.randomUUID();

        User manager = new User();
        manager.setRole("MANAGER");

        Hotel existing = new Hotel();

        when(hotels.findById(hotelId))
                .thenReturn(Optional.of(existing));

        when(users.findById(managerId))
                .thenReturn(Optional.of(manager));

        when(hotels.save(any(Hotel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HotelRequest request = new HotelRequest(
                "Updated Hotel",
                "Bengaluru",
                5,
                "Updated description",
                "https://example.com/updated.jpg",
                managerId,
                List.of("Pool")
        );

        HotelResponse result = service.update(hotelId, request);

        assertNotNull(result);
        assertEquals("Updated Hotel", result.name());
        assertEquals("Bengaluru", result.city());
        assertEquals(5, result.starRating());

        verify(hotels).findById(hotelId);
        verify(hotels).save(existing);
    }

    @Test
    void deleteCancelsBookingsAndSoftDeletesHotelWithoutRemovingBookingHistory() {
        Hotel existing = new Hotel();
        Room room = new Room();
        Booking booking = new Booking();
        booking.setStatus("CONFIRMED");
        room.getBookings().add(booking);

        when(hotels.findById(hotelId)).thenReturn(Optional.of(existing));
        when(rooms.findByHotelId(hotelId)).thenReturn(List.of(room));
        when(hotels.save(existing)).thenReturn(existing);

        service.delete(hotelId);

        assertTrue(existing.isDeleted());
        assertEquals("CANCELLED", booking.getStatus());
        verify(hotels).findById(hotelId);
        verify(rooms).findByHotelId(hotelId);
        verify(hotels).save(existing);
        verify(hotels, never()).delete(any(Hotel.class));
    }

    @Test
    void listReturnsHotelWithLowestActiveRoomPrice() {
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);
        UUID managerId = UUID.randomUUID();

        when(manager.getId()).thenReturn(managerId);

        when(hotel.getId()).thenReturn(hotelId);
        when(hotel.getName()).thenReturn("Test Hotel");
        when(hotel.getCity()).thenReturn("Goa");
        when(hotel.getStarRating()).thenReturn(4);
        when(hotel.getDescription()).thenReturn("Beach hotel");
        when(hotel.getCoverImageUrl())
                .thenReturn("https://example.com/hotel.jpg");
        when(hotel.getManager()).thenReturn(manager);
        when(hotel.getAmenities()).thenReturn("Pool||WiFi");

        Room expensive = mock(Room.class);
        when(expensive.isActive()).thenReturn(true);
        when(expensive.getPricePerNight())
                .thenReturn(new BigDecimal("2500"));

        Room cheap = mock(Room.class);
        when(cheap.isActive()).thenReturn(true);
        when(cheap.getPricePerNight())
                .thenReturn(new BigDecimal("1500"));

        Room inactive = mock(Room.class);
        when(inactive.isActive()).thenReturn(false);

        when(hotels.search("Goa")).thenReturn(List.of(hotel));
        when(rooms.findByHotelId(hotelId))
                .thenReturn(List.of(expensive, cheap, inactive));

        List<HotelResponse> result = service.list("Goa");

        assertEquals(1, result.size());
        assertEquals("Test Hotel", result.get(0).name());
        assertEquals(List.of("Pool", "WiFi"), result.get(0).amenities());
        assertEquals(
                0,
                new BigDecimal("1500").compareTo(result.get(0).startingPrice())
        );
    }


    @Test
    void availableRoomsReturnsMappedRooms() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(2);

        // Mock both objects because their methods are stubbed below
        Hotel hotel = mock(Hotel.class);
        Room room = mock(Room.class);

        UUID roomId = UUID.randomUUID();

        when(hotels.findById(hotelId))
                .thenReturn(Optional.of(hotel));

        when(rooms.findAvailable(hotelId, checkIn, checkOut))
                .thenReturn(List.of(room));

        when(hotel.getId()).thenReturn(hotelId);

        when(room.getId()).thenReturn(roomId);
        when(room.getHotel()).thenReturn(hotel);
        when(room.getRoomNumber()).thenReturn("101");
        when(room.getRoomType()).thenReturn("DELUXE");
        when(room.getPricePerNight())
                .thenReturn(new BigDecimal("2500"));
        when(room.getMaxOccupancy()).thenReturn(2);
        when(room.getDescription()).thenReturn("Deluxe room");
        when(room.getImageUrl())
                .thenReturn("https://example.com/room.jpg");
        when(room.isActive()).thenReturn(true);

        List<RoomResponse> result =
                service.availableRooms(hotelId, checkIn, checkOut);

        assertEquals(1, result.size());
        assertEquals(roomId, result.get(0).id());
        assertEquals(hotelId, result.get(0).hotelId());
        assertEquals("101", result.get(0).roomNumber());
        assertEquals("DELUXE", result.get(0).roomType());
        assertEquals(0, new BigDecimal("2500")
                .compareTo(result.get(0).pricePerNight()));
        assertEquals(2, result.get(0).maxOccupancy());

        verify(hotels).findById(hotelId);
        verify(rooms).findAvailable(hotelId, checkIn, checkOut);
    }

}
