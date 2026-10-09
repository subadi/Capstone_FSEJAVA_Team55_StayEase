package com.stayease.config;

import com.stayease.entity.*;
import com.stayease.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner seed(UserRepository users, HotelRepository hotels, RoomRepository rooms, PasswordEncoder enc) {
        return args -> {
            if (users.count() > 0) return;
            User admin = user(users, "admin@stayease.com", "Admin", "ADMIN", enc.encode("admin123"));
            User mgr = user(users, "mgr1@stayease.com", "Hotel Mgr", "MANAGER", enc.encode("manager123"));
            User guest = user(users, "guest1@stayease.com", "Rohit Verma", "GUEST", enc.encode("guest123"));
            Hotel h1 = hotel(hotels, "Sunset Palms", "Goa", 4, "Beachfront resort with pool", "https://picsum.photos/seed/hotel1/800/500", mgr, "Beachfront||Pool||WiFi");
            Hotel h2 = hotel(hotels, "City Inn", "Goa", 3, "Budget-friendly city centre hotel", "https://picsum.photos/seed/hotel2/800/500", mgr, "WiFi||Restaurant||Parking");
            room(rooms, h1, "101", "Double", 3500, 2, "Comfortable double room", "https://picsum.photos/seed/room101/600/400");
            room(rooms, h1, "201", "Suite", 7000, 3, "Spacious suite", "https://picsum.photos/seed/room201/600/400");
            room(rooms, h2, "101", "Single", 1500, 1, "Compact single room", "https://picsum.photos/seed/room301/600/400");
        };
    }

    private User user(UserRepository r, String e, String n, String role, String p) {
        User u = new User();
        u.setEmail(e);
        u.setName(n);
        u.setRole(role);
        u.setPasswordHash(p);
        u.setCreatedAt(LocalDateTime.now());
        return r.save(u);
    }

    private Hotel hotel(HotelRepository r, String n, String c, int s, String d, String img, User m, String a) {
        Hotel h = new Hotel();
        h.setName(n);
        h.setCity(c);
        h.setStarRating(s);
        h.setDescription(d);
        h.setCoverImageUrl(img);
        h.setManager(m);
        h.setAmenities(a);
        return r.save(h);
    }

    private Room room(RoomRepository r, Hotel h, String num, String type, int price, int max, String d, String img) {
        Room x = new Room();
        x.setHotel(h);
        x.setRoomNumber(num);
        x.setRoomType(type);
        x.setPricePerNight(BigDecimal.valueOf(price));
        x.setMaxOccupancy(max);
        x.setDescription(d);
        x.setImageUrl(img);
        x.setActive(true);
        return r.save(x);
    }
}
