package com.stayease.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.*;

public final class BookingDtos {
    private BookingDtos() {
    }

    public record CreateBookingRequest(@NotNull UUID roomId, @NotNull LocalDate checkInDate,
                                       @NotNull LocalDate checkOutDate) {
    }

    public record BookingResponse(UUID id, String bookingRef, UUID roomId, UUID hotelId, UUID userId,
                                  LocalDate checkInDate, LocalDate checkOutDate, java.math.BigDecimal totalPrice,
                                  String status, java.time.LocalDateTime createdAt, String hotelName, String roomType,
                                  String roomNumber) {
    }
}