package com.stayease.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public final class HotelDtos {
    private HotelDtos() {
    }

    public record HotelRequest(@NotBlank String name, @NotBlank String city, @Min(1) @Max(5) int starRating,
                               @NotBlank String description, @NotBlank String coverImageUrl, @NotNull UUID managerId,
                               @NotNull List<String> amenities) {
    }

    public record HotelResponse(UUID id, String name, String city, int starRating, String description,
                                String coverImageUrl, UUID managerId, List<String> amenities,
                                java.math.BigDecimal startingPrice) {
    }
}