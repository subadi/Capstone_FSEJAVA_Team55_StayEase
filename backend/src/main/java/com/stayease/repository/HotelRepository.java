package com.stayease.repository;

import com.stayease.entity.Hotel;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    @Query("select h from Hotel h join fetch h.manager m where h.deleted = false and (:city='' or lower(h.city)=lower(:city)) order by h.name")
    List<Hotel> search(@Param("city") String city);

    List<Hotel> findByManagerId(UUID managerId);
}