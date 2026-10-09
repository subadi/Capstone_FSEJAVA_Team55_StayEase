package com.stayease.service;

import com.stayease.dto.RoomDtos.RoomRequest;
import com.stayease.entity.Hotel;
import com.stayease.entity.Room;
import com.stayease.entity.User;
import com.stayease.exception.ApiException;
import com.stayease.repository.HotelRepository;
import com.stayease.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock
    RoomRepository rooms;
    @Mock
    HotelRepository hotels;
    private RoomService service;
    private final UUID managerId = UUID.randomUUID();
    private final UUID hotelId = UUID.randomUUID();

    private RoomRequest request() {
        return new RoomRequest(" 101 ", "Deluxe", new BigDecimal("120.00"), 2, "Sea view", "https://example.com/room.jpg", true);
    }

    @BeforeEach
    void setUp() {
        service = new RoomService(rooms, hotels);
    }

    @Test
    void createTrimsRoomNumberAndMapsRoomFields() {
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);
        when(hotels.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);
        when(rooms.existsByHotelIdAndRoomNumber(hotelId, " 101 ")).thenReturn(false);
        when(rooms.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setRoomNumber(r.getRoomNumber().trim());
            return r;
        });
        var response = service.create(managerId, hotelId, request());
        assertEquals("101", response.roomNumber());
        assertEquals("Deluxe", response.roomType());
        assertEquals(new BigDecimal("120.00"), response.pricePerNight());
        assertEquals(2, response.maxOccupancy());
        assertTrue(response.isActive());
        verify(rooms).save(any(Room.class));
    }

    @Test
    void createRejectsUnknownHotel() {
        when(hotels.findById(hotelId)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.create(managerId, hotelId, request()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("HOTEL_NOT_FOUND", ex.getError());
        verify(rooms, never()).save(any());
    }

    @Test
    void createRejectsHotelOwnedByAnotherManager() {
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);
        when(hotels.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(UUID.randomUUID());
        ApiException ex = assertThrows(ApiException.class, () -> service.create(managerId, hotelId, request()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(rooms, never()).save(any());
    }

    @Test
    void createRejectsDuplicateRoomNumber() {
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);
        when(hotels.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);
        when(rooms.existsByHotelIdAndRoomNumber(hotelId, " 101 ")).thenReturn(true);
        ApiException ex = assertThrows(ApiException.class, () -> service.create(managerId, hotelId, request()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("ROOM_EXISTS", ex.getError());
        verify(rooms, never()).save(any());
    }

    @Test
    void managerRoomsReturnsRoomsFromRepositoryMapping() {
        Hotel hotel = mock(Hotel.class);
        Room room = mock(Room.class);
        UUID roomId = UUID.randomUUID();
        when(rooms.findByHotelManagerId(managerId)).thenReturn(List.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getHotel()).thenReturn(hotel);
        when(hotel.getId()).thenReturn(hotelId);
        when(room.getRoomNumber()).thenReturn("101");
        when(room.getRoomType()).thenReturn("Deluxe");
        when(room.getPricePerNight()).thenReturn(new BigDecimal("120.00"));
        when(room.getMaxOccupancy()).thenReturn(2);
        when(room.getDescription()).thenReturn("Sea view");
        when(room.getImageUrl()).thenReturn("img");
        when(room.isActive()).thenReturn(true);
        var result = service.managerRooms(managerId);
        assertEquals(1, result.size());
        assertEquals(roomId, result.get(0).id());
        assertEquals(hotelId, result.get(0).hotelId());
    }

    @Test
    void updateRejectsUnknownRoom() {
        UUID roomId = UUID.randomUUID();
        when(rooms.findById(roomId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.update(managerId, roomId, request())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("ROOM_NOT_FOUND", ex.getError());
        verify(rooms, never()).save(any(Room.class));
    }

    @Test
    void updateRejectsRoomOwnedByAnotherManager() {
        UUID roomId = UUID.randomUUID();
        UUID anotherManagerId = UUID.randomUUID();

        Room room = mock(Room.class);
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        when(rooms.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getHotel()).thenReturn(hotel);
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(anotherManagerId);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.update(managerId, roomId, request())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getError());
        verify(rooms, never()).save(any(Room.class));
    }

    @Test
    void deleteRejectsUnknownRoom() {
        UUID roomId = UUID.randomUUID();
        when(rooms.findById(roomId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.delete(managerId, roomId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("ROOM_NOT_FOUND", ex.getError());
        verify(rooms, never()).delete(any(Room.class));
    }

    @Test
    void toggleRejectsRoomOwnedByAnotherManager() {
        UUID roomId = UUID.randomUUID();
        UUID anotherManagerId = UUID.randomUUID();

        Room room = mock(Room.class);
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        when(rooms.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getHotel()).thenReturn(hotel);
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(anotherManagerId);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.toggle(
                        managerId,
                        roomId,
                        new com.stayease.dto.RoomDtos.RoomStatusRequest(false)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(rooms, never()).save(any(Room.class));
    }


    @Test
    void updateSuccessfullyUpdatesRoom() {
        UUID roomId = UUID.randomUUID();

        Room existingRoom = new Room();
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        existingRoom.setHotel(hotel);

        when(rooms.findById(roomId))
                .thenReturn(Optional.of(existingRoom));
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);
        when(rooms.save(any(Room.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.update(managerId, roomId, request());

        assertEquals("101", response.roomNumber());
        assertEquals("Deluxe", response.roomType());
        assertEquals(0, new BigDecimal("120.00")
                .compareTo(response.pricePerNight()));
        assertEquals(2, response.maxOccupancy());
        assertTrue(response.isActive());

        verify(rooms).save(existingRoom);
    }


    @Test
    void deleteSuccessfullyDeletesRoom() {
        UUID roomId = UUID.randomUUID();
        Room existingRoom = mock(Room.class);
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        when(rooms.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(existingRoom.getHotel()).thenReturn(hotel);
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);

        service.delete(managerId, roomId);

        verify(rooms).findById(roomId);
        verify(rooms).delete(existingRoom);
    }


    @Test
    void toggleSuccessfullyActivatesRoom() {
        UUID roomId = UUID.randomUUID();

        Room existingRoom = new Room();
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        existingRoom.setHotel(hotel);

        when(rooms.findById(roomId))
                .thenReturn(Optional.of(existingRoom));
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);
        when(rooms.save(any(Room.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = service.toggle(
                managerId,
                roomId,
                new com.stayease.dto.RoomDtos.RoomStatusRequest(true)
        );

        assertTrue(response.isActive());
        verify(rooms).save(existingRoom);
    }


    @Test
    void toggleSuccessfullyDeactivatesRoom() {
        UUID roomId = UUID.randomUUID();
        Room existingRoom = mock(Room.class);
        Hotel hotel = mock(Hotel.class);
        User manager = mock(User.class);

        when(rooms.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(existingRoom.getHotel()).thenReturn(hotel);
        when(hotel.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(managerId);
        when(rooms.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.toggle(
                managerId,
                roomId,
                new com.stayease.dto.RoomDtos.RoomStatusRequest(false)
        );

        verify(existingRoom).setActive(false);
        assertFalse(response.isActive());
        verify(rooms).save(existingRoom);
    }
}
