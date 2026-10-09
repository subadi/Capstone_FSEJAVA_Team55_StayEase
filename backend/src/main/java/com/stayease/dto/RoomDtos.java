package com.stayease.dto;

import jakarta.validation.constraints.*;

public final class RoomDtos {
    private RoomDtos() {
    }

    public record RoomRequest(@NotBlank String roomNumber, @NotBlank String roomType,
                              @DecimalMin("0.01") java.math.BigDecimal pricePerNight, @Min(1) int maxOccupancy,
                              String description, String imageUrl, boolean isActive) {
    }

    public record RoomStatusRequest(boolean isActive) {
    }

    public record RoomResponse(java.util.UUID id, java.util.UUID hotelId, String roomNumber, String roomType,
                               java.math.BigDecimal pricePerNight, int maxOccupancy, String description,
                               String imageUrl, boolean isActive) {
    }
}