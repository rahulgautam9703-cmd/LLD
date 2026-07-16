package booking;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {

    private String id;
    private List<String> participantsId; //VVIP partiipants not stored here
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomId;
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

    public void setParticipantsId(List<String> participantsId) {
        this.participantsId = participantsId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

/*Storing Room vs roomId
Room directly
Pros: direct fetching room contents(fast) instead of lookup via RoomRepo,
Cons: Stale data chance, more memory, tight coupling
*/
