package room; //NOT com.meetingScheduler.room

public class Room {

    private final String roomId;
    private final String roomName;
    private final int capacity;

    public Room(String roomId, String roomName, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be positive: " + capacity);
        }
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
    }

    public boolean canFit(int people) { return people <= capacity; }
//----------------------------------------------------------------
    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getCapacity() {
        return capacity;
    }
}
