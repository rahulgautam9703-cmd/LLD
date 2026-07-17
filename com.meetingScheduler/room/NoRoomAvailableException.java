package room;

import shared.AppException;

public class NoRoomAvailableException extends AppException {
    public NoRoomAvailableException(String detail) {
        super("No room available: " + detail, "NO_ROOM_AVAILABLE");
    }
}
