package com.stayease.controller;

import com.stayease.dto.HotelDtos.*;
import com.stayease.dto.RoomDtos.RoomResponse;
import com.stayease.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService service;

    public HotelController(HotelService s) {
        service = s;
    }

    @GetMapping
    public List<HotelResponse> list(@RequestParam(defaultValue = "") String city) {
        System.out.println("[HotelController.list] city=" + city);
        return service.list(city);
    }

    @GetMapping("/{id}/rooms")
    public List<RoomResponse> rooms(@PathVariable UUID id, @RequestParam LocalDate checkIn, @RequestParam LocalDate checkOut) {
        System.out.println("[HotelController.rooms] hotelId=" + id + ", checkIn=" + checkIn + ", checkOut=" + checkOut);
        return service.availableRooms(id, checkIn, checkOut);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse create(@Valid @RequestBody HotelRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public HotelResponse update(@PathVariable UUID id, @Valid @RequestBody HotelRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
