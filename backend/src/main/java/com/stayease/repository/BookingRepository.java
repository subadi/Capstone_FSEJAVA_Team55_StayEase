package com.stayease.repository;

import com.stayease.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByGuestIdOrderByCheckInDateDesc(UUID guestId);

    List<Booking> findByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThan(UUID roomId, String status, LocalDate checkOut, LocalDate checkIn);

    List<Booking> findByRoomHotelManagerIdAndStatusNotAndCheckInDateGreaterThanEqualOrderByCheckInDateAsc(UUID managerId, String status, LocalDate from);

    boolean existsByRoomHotelId(UUID hotelId);
}