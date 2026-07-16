package room;

import java.util.HashMap;
import java.util.Map;

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
}
