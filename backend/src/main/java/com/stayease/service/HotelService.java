package com.stayease.service;

import com.stayease.dto.HotelDtos.*;
import com.stayease.dto.RoomDtos.RoomResponse;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class HotelService {
    private final HotelRepository hotels;
    private final UserRepository users;
    private final RoomRepository rooms;

    public HotelService(HotelRepository h, UserRepository u, RoomRepository r) {
        hotels = h;
        users = u;
        rooms = r;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> list(String city) {
        System.out.println("[HotelService.list] city=" + city);
        var result = hotels.search(city == null ? "" : city.trim()).stream().map(this::toResponse).toList();
        System.out.println("[HotelService.list] hotel count=" + result.size());
        return result;
    }

    public Hotel get(UUID id) {
        Hotel hotel = hotels.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOTEL_NOT_FOUND", "Hotel not found"));
        if (hotel.isDeleted())
            throw new ApiException(HttpStatus.NOT_FOUND, "HOTEL_NOT_FOUND", "Hotel not found");
        return hotel;
    }

    @Transactional
    public HotelResponse create(HotelRequest r) {
        System.out.println("[HotelService.create] name=" + r.name() + ", city=" + r.city() + ", managerId=" + r.managerId());
        return save(new Hotel(), r);
    }

    @Transactional
    public HotelResponse update(UUID id, HotelRequest r) {
        System.out.println("[HotelService.update] hotelId=" + id);
        return save(get(id), r);
    }

    private HotelResponse save(Hotel h, HotelRequest r) {
        User m = users.findById(r.managerId()).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MANAGER", "Manager not found"));
        if (!"MANAGER".equals(m.getRole()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MANAGER", "Selected user is not a manager");
        h.setName(r.name().trim());
        h.setCity(r.city().trim());
        h.setStarRating(r.starRating());
        h.setDescription(r.description());
        h.setCoverImageUrl(r.coverImageUrl());
        h.setManager(m);
        h.setAmenities(String.join("||", r.amenities() == null ? List.of() : r.amenities()));
        return toResponse(hotels.save(h));
    }

    @Transactional
    public void delete(UUID id) {
        System.out.println("[HotelService.delete] hotelId=" + id);
        Hotel h = get(id);
        // Preserve booking history: cancel bookings, then hide the hotel instead of
        // physically deleting it (which would cascade-delete its rooms and bookings).
        for (Room room : rooms.findByHotelId(id)) {
            for (Booking booking : room.getBookings()) {
                if (!"CANCELLED".equals(booking.getStatus())) {
                    booking.setStatus("CANCELLED");
                }
            }
        }
        h.setDeleted(true);
        hotels.save(h);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> availableRooms(UUID id, java.time.LocalDate in, java.time.LocalDate out) {
        System.out.println("[HotelService.availableRooms] hotelId=" + id + ", checkIn=" + in + ", checkOut=" + out);
        if (in == null || out == null || !in.isBefore(out))
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATES", "Check-out must be after check-in");
        get(id);
        return rooms.findAvailable(id, in, out).stream().map(this::room).toList();
    }

    private HotelResponse toResponse(Hotel h) {
        var active = rooms.findByHotelId(h.getId()).stream().filter(Room::isActive).map(Room::getPricePerNight).min(BigDecimal::compareTo).orElse(null);
        return new HotelResponse(h.getId(), h.getName(), h.getCity(), h.getStarRating(), h.getDescription(), h.getCoverImageUrl(), h.getManager().getId(), h.getAmenities() == null || h.getAmenities().isBlank() ? List.of() : List.of(h.getAmenities().split("\\|\\|")), active);
    }

    private RoomResponse room(Room r) {
        return new RoomResponse(r.getId(), r.getHotel().getId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(), r.getMaxOccupancy(), r.getDescription(), r.getImageUrl(), r.isActive());
    }
}
