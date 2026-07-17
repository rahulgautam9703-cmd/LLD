package booking;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {

    // id is NOT final: it's assigned by BookingService after validation, not by the caller.
    private String id;

    // Caller-supplied request data: final, set once via the constructor.
    private final List<String> participantsId; //VVIP partiipants not stored here
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String roomId;

    public Booking(String roomId, List<String> participantsId, LocalDateTime startTime, LocalDateTime endTime) {
        this.roomId = roomId;
        this.participantsId = participantsId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
//--------------------------------------------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipantsId() {
        return participantsId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getRoomId() {
        return roomId;
    }
}

/*Storing Room vs roomId
Room directly
Pros: direct fetching room contents(fast) instead of lookup via RoomRepo,
Cons: Stale data chance, more memory, tight coupling
*/
