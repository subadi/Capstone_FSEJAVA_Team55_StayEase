package com.stayease.controller;

import com.stayease.dto.BookingDtos.*;
import com.stayease.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService s) {
        service = s;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest r, Authentication a) {
        System.out.println("[BookingController.create] principal=" + (a == null ? "null" : a.getName()));
        return service.create(UUID.fromString(a.getName()), r);
    }

    @GetMapping("/mine")
    public List<BookingResponse> mine(Authentication a) {
        System.out.println("[BookingController.mine] principal=" + (a == null ? "null" : a.getName()));
        return service.mine(UUID.fromString(a.getName()));
    }

    @PutMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable UUID id, Authentication a) {
        System.out.println("[BookingController.cancel] principal=" + (a == null ? "null" : a.getName()) + ", bookingId=" + id);
        return service.cancel(UUID.fromString(a.getName()), id);
    }
}
