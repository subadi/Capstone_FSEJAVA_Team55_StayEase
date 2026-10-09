package com.stayease.service;

import com.stayease.dto.RoomDtos.*;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoomService {
    private final RoomRepository rooms;
    private final HotelRepository hotels;

    public RoomService(RoomRepository r, HotelRepository h) {
        rooms = r;
        hotels = h;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> managerRooms(UUID managerId) {
        System.out.println("[RoomService.managerRooms] managerId=" + managerId);
        var result = rooms.findByHotelManagerId(managerId).stream().map(this::to).toList();
        System.out.println("[RoomService.managerRooms] room count=" + result.size());
        return result;
    }

    @Transactional
    public RoomResponse create(UUID managerId, UUID hotelId, RoomRequest r) {
        System.out.println("[RoomService.create] managerId=" + managerId + ", hotelId=" + hotelId + ", roomNumber=" + r.roomNumber());
        Hotel h = hotels.findById(hotelId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HOTEL_NOT_FOUND", "Hotel not found"));
        ensureOwner(managerId, h);
        if (rooms.existsByHotelIdAndRoomNumber(hotelId, r.roomNumber()))
            throw new ApiException(HttpStatus.CONFLICT, "ROOM_EXISTS", "Room number already exists");
        Room x = new Room();
        x.setHotel(h);
        apply(x, r);
        return to(rooms.save(x));
    }

    @Transactional
    public RoomResponse update(UUID managerId, UUID id, RoomRequest r) {
        System.out.println("[RoomService.update] managerId=" + managerId + ", roomId=" + id);
        Room x = get(id);
        ensureOwner(managerId, x.getHotel());
        apply(x, r);
        return to(rooms.save(x));
    }

    @Transactional
    public void delete(UUID managerId, UUID id) {
        System.out.println("[RoomService.delete] managerId=" + managerId + ", roomId=" + id);
        Room x = get(id);
        ensureOwner(managerId, x.getHotel());
        rooms.delete(x);
    }

    @Transactional
    public RoomResponse toggle(UUID managerId, UUID id, RoomStatusRequest r) {
        Room x = get(id);
        ensureOwner(managerId, x.getHotel());
        x.setActive(r.isActive());
        return to(rooms.save(x));
    }

    private Room get(UUID id) {
        return rooms.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room not found"));
    }

    private void ensureOwner(UUID managerId, Hotel h) {
        if (!h.getManager().getId().equals(managerId))
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You can only manage rooms for your hotel");
    }

    private void apply(Room x, RoomRequest r) {
        x.setRoomNumber(r.roomNumber().trim());
        x.setRoomType(r.roomType());
        x.setPricePerNight(r.pricePerNight());
        x.setMaxOccupancy(r.maxOccupancy());
        x.setDescription(r.description() == null ? "" : r.description());
        x.setImageUrl(r.imageUrl() == null ? "" : r.imageUrl());
        x.setActive(r.isActive());
    }

    private RoomResponse to(Room r) {
        return new RoomResponse(r.getId(), r.getHotel().getId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(), r.getMaxOccupancy(), r.getDescription(), r.getImageUrl(), r.isActive());
    }
}
