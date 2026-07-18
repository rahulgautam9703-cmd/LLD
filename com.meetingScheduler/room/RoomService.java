package room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomService {

    private final Map<String, Room> rooms = new HashMap<>(); // O(1)

    // Registry invariant: room ids are unique. Reject duplicates instead of silently overwriting.
    public void addRoom(Room room) {
        if (rooms.containsKey(room.getRoomId())) {
            throw new RoomAlreadyExistsException(room.getRoomId());
        }
        rooms.put(room.getRoomId(), room);
    }

    public Room findById(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        return room;
    }

    // Filter by ROOM ATTRIBUTES only (capacity + must have ALL required amenities). Availability is
    // NOT checked here because bookings live in BookingService -> keeps the dependency one-directional.
    public List<Room> findByRequirements(int minCapacity, Set<Amenity> requiredAmenities) {
        return rooms.values().stream()
                .filter(r -> r.canFit(minCapacity))
                .filter(r -> r.hasAll(requiredAmenities))
                .collect(Collectors.toList());
    }
}
