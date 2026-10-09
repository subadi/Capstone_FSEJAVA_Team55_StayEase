package com.stayease.service;

import com.stayease.dto.BookingDtos.*;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final UserRepository users;

    public BookingService(BookingRepository b, RoomRepository r, UserRepository u) {
        bookings = b;
        rooms = r;
        users = u;
    }

    @Transactional
    public BookingResponse create(UUID userId, CreateBookingRequest r) {
        System.out.println("[BookingService.create] userId=" + userId + ", roomId=" + r.roomId() + ", checkIn=" + r.checkInDate() + ", checkOut=" + r.checkOutDate());
        if (!r.checkInDate().isBefore(r.checkOutDate()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATES", "Check-out must be after check-in");
        if (r.checkInDate().isBefore(LocalDate.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATES", "Check-in cannot be in the past");
        Room room = rooms.findByIdForUpdate(r.roomId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room not found"));
        if (!room.isActive()) throw new ApiException(HttpStatus.CONFLICT, "ROOM_INACTIVE", "Room is inactive");
        if (!bookings.findByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThan(room.getId(), "CANCELLED", r.checkOutDate(), r.checkInDate()).isEmpty())
            throw new ApiException(HttpStatus.CONFLICT, "ROOM_UNAVAILABLE", "Room is not available for selected dates");
        User guest = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));
        long nights = ChronoUnit.DAYS.between(r.checkInDate(), r.checkOutDate());
        Booking b = new Booking();
        b.setBookingRef("STY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setGuest(guest);
        b.setRoom(room);
        b.setCheckInDate(r.checkInDate());
        b.setCheckOutDate(r.checkOutDate());
        b.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        b.setStatus("CONFIRMED");
        b.setCreatedAt(LocalDateTime.now());
        return to(bookings.save(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine(UUID userId) {
        System.out.println("[BookingService.mine] Loading bookings for guest=" + userId);
        var result = bookings.findByGuestIdOrderByCheckInDateDesc(userId).stream().map(this::to).toList();
        System.out.println("[BookingService.mine] Returned booking count=" + result.size());
        return result;
    }

    @Transactional
    public BookingResponse cancel(UUID userId, UUID id) {
        Booking b = bookings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Booking not found"));
        if (!b.getGuest().getId().equals(userId))
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You can only cancel your own booking");
        if (!"CONFIRMED".equals(b.getStatus()))
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_CANCELLED", "Booking is already cancelled");
        if (!b.getCheckInDate().isAfter(LocalDate.now()))
            throw new ApiException(HttpStatus.CONFLICT, "CANCELLATION_NOT_ALLOWED", "Past or current stays cannot be cancelled");
        b.setStatus("CANCELLED");
        return to(bookings.save(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> managerUpcoming(UUID managerId) {
        System.out.println("[BookingService.managerUpcoming] Loading bookings for manager=" + managerId);
        var result = bookings.findByRoomHotelManagerIdAndStatusNotAndCheckInDateGreaterThanEqualOrderByCheckInDateAsc(managerId, "CANCELLED", LocalDate.now()).stream().map(this::to).toList();
        System.out.println("[BookingService.managerUpcoming] Returned booking count=" + result.size());
        return result;
    }

    private BookingResponse to(Booking b) {
        Room r = b.getRoom();
        return new BookingResponse(b.getId(), b.getBookingRef(), r.getId(), r.getHotel().getId(), b.getGuest().getId(), b.getCheckInDate(), b.getCheckOutDate(), b.getTotalPrice(), b.getStatus(), b.getCreatedAt(), r.getHotel().getName(), r.getRoomType(), r.getRoomNumber());
    }
}
