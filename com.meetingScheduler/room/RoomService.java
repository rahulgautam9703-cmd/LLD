package room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Owns the Room aggregate. Keyed by roomId -> O(1) lookup + id uniqueness for free.
public class RoomService {

    private final Map<String, Room> rooms = new HashMap<>();

    public void addRoom(Room room) {
        rooms.put(room.getRoomId(), room);
    }

    public Room findById(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        return room;
    }

    // Filter by ROOM ATTRIBUTES only (capacity + amenities). Availability is NOT checked here
    // because bookings live in BookingService -> keeps the dependency one-directional.
    public List<Room> findByRequirements(int minCapacity, Set<Amenity> requiredAmenities) {
        return rooms.values().stream()
                .filter(r -> r.canFit(minCapacity))
                .filter(r -> r.hasAll(requiredAmenities))
                .collect(Collectors.toList());
    }
}
