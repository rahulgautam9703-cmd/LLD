package room;

import shared.AppException;

public class RoomCapacityExceededException extends AppException {   // extends YOUR base, not Exception
    public RoomCapacityExceededException(String roomId, int attendees) {
        super("Room " + roomId + " cannot fit " + attendees + " attendees", "ROOM_CAPACITY_EXCEEDED");
    }
}
