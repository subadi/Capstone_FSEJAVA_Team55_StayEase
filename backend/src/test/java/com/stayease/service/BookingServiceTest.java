package com.stayease.service;

import com.stayease.dto.BookingDtos.CreateBookingRequest;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookings;
    @Mock
    RoomRepository rooms;
    @Mock
    UserRepository users;
    private BookingService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID roomId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new BookingService(bookings, rooms, users);
    }

    @Test
    void createCalculatesTotalAndReturnsConfirmedBooking() {
        LocalDate in = LocalDate.now().plusDays(5), out = in.plusDays(3);
        Hotel hotel = mock(Hotel.class);
        when(hotel.getId()).thenReturn(UUID.randomUUID());
        when(hotel.getName()).thenReturn("Sunset Palms");
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(roomId);
        when(room.getHotel()).thenReturn(hotel);
        when(room.isActive()).thenReturn(true);
        when(room.getPricePerNight()).thenReturn(new BigDecimal("125.00"));
        when(room.getRoomType()).thenReturn("Deluxe");
        when(room.getRoomNumber()).thenReturn("101");
        User guest = mock(User.class);
        when(guest.getId()).thenReturn(userId);
        when(rooms.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
        when(bookings.findByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThan(roomId, "CANCELLED", out, in)).thenReturn(List.of());
        when(users.findById(userId)).thenReturn(Optional.of(guest));
        when(bookings.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(userId, new CreateBookingRequest(roomId, in, out));
        assertEquals(new BigDecimal("375.00"), response.totalPrice());
        assertEquals("CONFIRMED", response.status());
        assertTrue(response.bookingRef().matches("STY-[A-F0-9]{8}"));
        assertEquals(in, response.checkInDate());
        assertEquals(out, response.checkOutDate());
        verify(rooms).findByIdForUpdate(roomId);
        verify(bookings).save(any(Booking.class));
    }

    @Test
    void createRejectsCheckoutNotAfterCheckin() {
        LocalDate date = LocalDate.now().plusDays(3);
        ApiException ex = assertThrows(ApiException.class,
                () -> service.create(userId, new CreateBookingRequest(roomId, date, date)));
        assertEquals("INVALID_DATES", ex.getError());
        verifyNoInteractions(rooms, bookings, users);
    }

    @Test
    void createRejectsPastCheckin() {
        ApiException ex = assertThrows(ApiException.class, () -> service.create(userId,
                new CreateBookingRequest(roomId, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))));
        assertEquals("INVALID_DATES", ex.getError());
        verifyNoInteractions(rooms, bookings, users);
    }

    @Test
    void createRejectsMissingRoom() {
        when(rooms.findByIdForUpdate(roomId)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.create(userId,
                new CreateBookingRequest(roomId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3))));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("ROOM_NOT_FOUND", ex.getError());
    }

    @Test
    void createRejectsInactiveRoom() {
        Room room = mock(Room.class);
        when(room.isActive()).thenReturn(false);
        when(rooms.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
        ApiException ex = assertThrows(ApiException.class, () -> service.create(userId,
                new CreateBookingRequest(roomId, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3))));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("ROOM_INACTIVE", ex.getError());
    }

    @Test
    void createRejectsOverlappingBooking() {
        LocalDate in = LocalDate.now().plusDays(2), out = in.plusDays(2);
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(roomId);
        when(room.isActive()).thenReturn(true);
        when(rooms.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
        when(bookings.findByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThan(roomId, "CANCELLED", out, in)).thenReturn(List.of(mock(Booking.class)));
        ApiException ex = assertThrows(ApiException.class, () -> service.create(userId, new CreateBookingRequest(roomId, in, out)));
        assertEquals("ROOM_UNAVAILABLE", ex.getError());
        verify(users, never()).findById(any());
    }

    @Test
    void cancelRejectsBookingOwnedByAnotherGuest() {
        UUID bookingId = UUID.randomUUID(), otherUser = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        User guest = mock(User.class);
        when(bookings.findById(bookingId)).thenReturn(Optional.of(booking));
        when(booking.getGuest()).thenReturn(guest);
        when(guest.getId()).thenReturn(otherUser);
        ApiException ex = assertThrows(ApiException.class, () -> service.cancel(userId, bookingId));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getError());
        verify(bookings, never()).save(any());
    }


    @Test
    void cancelRejectsUnknownBooking() {
        UUID bookingId = UUID.randomUUID();
        when(bookings.findById(bookingId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.cancel(userId, bookingId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("BOOKING_NOT_FOUND", ex.getError());
        verify(bookings, never()).save(any(Booking.class));
    }

    @Test
    void cancelRejectsAlreadyCancelledBooking() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        User guest = mock(User.class);

        when(bookings.findById(bookingId)).thenReturn(Optional.of(booking));
        when(booking.getGuest()).thenReturn(guest);
        when(guest.getId()).thenReturn(userId);
        when(booking.getStatus()).thenReturn("CANCELLED");

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.cancel(userId, bookingId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("ALREADY_CANCELLED", ex.getError());
        verify(bookings, never()).save(any(Booking.class));
    }

    @Test
    void cancelRejectsStayStartingToday() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = mock(Booking.class);
        User guest = mock(User.class);

        when(bookings.findById(bookingId)).thenReturn(Optional.of(booking));
        when(booking.getGuest()).thenReturn(guest);
        when(guest.getId()).thenReturn(userId);
        when(booking.getStatus()).thenReturn("CONFIRMED");
        when(booking.getCheckInDate()).thenReturn(LocalDate.now());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.cancel(userId, bookingId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("CANCELLATION_NOT_ALLOWED", ex.getError());
        verify(bookings, never()).save(any(Booking.class));
    }

    @Test
    void mineReturnsEmptyListWhenGuestHasNoBookings() {
        when(bookings.findByGuestIdOrderByCheckInDateDesc(userId))
                .thenReturn(List.of());

        var result = service.mine(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookings).findByGuestIdOrderByCheckInDateDesc(userId);
    }

}
