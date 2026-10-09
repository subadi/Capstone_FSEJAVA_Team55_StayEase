package com.stayease.controller;

import com.stayease.dto.BookingDtos.BookingResponse;
import com.stayease.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    private final BookingService bookings;

    public ManagerController(BookingService b) {
        bookings = b;
    }

    @GetMapping("/bookings")
    public List<BookingResponse> upcoming(Authentication a) {
        return bookings.managerUpcoming(UUID.fromString(a.getName()));
    }
}
