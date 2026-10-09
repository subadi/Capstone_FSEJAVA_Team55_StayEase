package com.stayease.repository;

import com.stayease.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByHotelId(UUID hotelId);

    List<Room> findByHotelManagerId(UUID managerId);

    boolean existsByHotelIdAndRoomNumber(UUID hotelId, String roomNumber);

    @Query("select r from Room r where r.hotel.id=:hotelId and r.active=true and not exists (select b from Booking b where b.room.id=r.id and b.status <> 'CANCELLED' and b.checkInDate < :checkOut and b.checkOutDate > :checkIn)")
    List<Room> findAvailable(@Param("hotelId") UUID hotelId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id=:id")
    Optional<Room> findByIdForUpdate(@Param("id") UUID id);
}