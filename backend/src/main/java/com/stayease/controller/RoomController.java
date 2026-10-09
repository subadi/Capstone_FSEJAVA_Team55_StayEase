package com.stayease.controller;

import com.stayease.dto.RoomDtos.*;
import com.stayease.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RoomController {
    private final RoomService service;

    public RoomController(RoomService s) {
        service = s;
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@PathVariable UUID hotelId, @Valid @RequestBody RoomRequest r, Authentication a) {
        return service.create(UUID.fromString(a.getName()), hotelId, r);
    }

    @PutMapping("/rooms/{id}")
    public RoomResponse update(@PathVariable UUID id, @Valid @RequestBody RoomRequest r, Authentication a) {
        return service.update(UUID.fromString(a.getName()), id, r);
    }

    @DeleteMapping("/rooms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication a) {
        service.delete(UUID.fromString(a.getName()), id);
    }

    @PatchMapping("/rooms/{id}/status")
    public RoomResponse status(@PathVariable UUID id, @Valid @RequestBody RoomStatusRequest r, Authentication a) {
        return service.toggle(UUID.fromString(a.getName()), id, r);
    }

    @GetMapping("/manager/rooms")
    public List<RoomResponse> managerRooms(Authentication a) {
        System.out.println("[RoomController.managerRooms] principal=" + (a == null ? "null" : a.getName()));
        return service.managerRooms(UUID.fromString(a.getName()));
    }
}
