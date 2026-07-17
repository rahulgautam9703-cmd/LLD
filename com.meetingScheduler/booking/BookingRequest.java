package booking;

import room.Amenity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// What the caller WANTS (constraints) — distinct from Booking, which is the RESULT (has a concrete room + id).
public class BookingRequest {

    private final List<String> participantIds;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String roomId;                 // nullable: a specific room, else the service selects one
    private final Set<Amenity> requiredAmenities; // used only when roomId is null (may be null/empty)

    // Case 1: caller wants a SPECIFIC room.
    public BookingRequest(String roomId, List<String> participantIds,
                          LocalDateTime startTime, LocalDateTime endTime) {
        this(roomId, participantIds, startTime, endTime, null);
    }

    // Case 2/3: caller wants ANY room matching the required amenities (pass roomId = null).
    public BookingRequest(String roomId, List<String> participantIds,
                          LocalDateTime startTime, LocalDateTime endTime,
                          Set<Amenity> requiredAmenities) {
        this.roomId = roomId;
        this.participantIds = participantIds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredAmenities = requiredAmenities;
    }

    public boolean hasSpecificRoom() {
        return roomId != null;
    }

    // capacity needed is derived, never passed in — it's just the head count
    public int getRequiredCapacity() {
        return participantIds == null ? 0 : participantIds.size();
    }

    public List<String> getParticipantIds() {
        return participantIds;
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

    public Set<Amenity> getRequiredAmenities() {
        return requiredAmenities;
    }
}
