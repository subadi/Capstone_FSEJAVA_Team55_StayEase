package com.stayease.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(name = "uk_hotel_room_number", columnNames = {"hotel_id", "room_number"}))
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    @Column(name = "room_number", nullable = false)
    private String roomNumber;
    @Column(name = "room_type", nullable = false)
    private String roomType;
    @Column(name = "price_per_night", nullable = false)
    private java.math.BigDecimal pricePerNight;
    @Column(name = "max_occupancy", nullable = false)
    private int maxOccupancy;
    @Column(length = 2000)
    private String description;
    @Column(name = "image_url", length = 1000)
    private String imageUrl;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Booking> bookings = new java.util.ArrayList<>();

    public UUID getId() {
        return id;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel v) {
        hotel = v;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String v) {
        roomNumber = v;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String v) {
        roomType = v;
    }

    public java.math.BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(java.math.BigDecimal v) {
        pricePerNight = v;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int v) {
        maxOccupancy = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public boolean isActive() {
        return active;
    }

    public java.util.List<Booking> getBookings() {
        return bookings;
    }

    public void setActive(boolean v) {
        active = v;
    }
}
