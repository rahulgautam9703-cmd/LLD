package room;

import shared.AppException;

public class RoomNotFoundException extends AppException {
    public RoomNotFoundException(String roomId) {
        super("Room not found: " + roomId, "ROOM_NOT_FOUND");
    }
}
