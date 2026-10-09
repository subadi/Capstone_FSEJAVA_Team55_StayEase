package com.stayease.entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private int starRating;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false, length = 1000)
    private String coverImageUrl;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();
    @Column(length = 2000)
    private String amenities;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String v) {
        city = v;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int v) {
        starRating = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String v) {
        coverImageUrl = v;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User v) {
        manager = v;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String v) {
        amenities = v;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean v) {
        deleted = v;
    }
}
