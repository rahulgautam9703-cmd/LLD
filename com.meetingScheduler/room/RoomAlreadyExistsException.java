package room;

import shared.AppException;

public class RoomAlreadyExistsException extends AppException {
    public RoomAlreadyExistsException(String roomId) {
        super("Room already exists: " + roomId, "ROOM_ALREADY_EXISTS");
    }
}
