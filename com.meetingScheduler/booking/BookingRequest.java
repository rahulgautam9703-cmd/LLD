package booking;

import room.Amenity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
/*
Initially we were passing Booking as i/p to BookingService.
Booking: Should be outcome of req
BookingReq: Should be i/p. Can be rejected. If accepted added to List<Booking> of BS.

/*
Earler - Public consttuctoe - Overloading

    // Case 1: SPECIFIC room.
    public BookingRequest(String roomId, List<String> participantIds,
                          LocalDateTime startTime, LocalDateTime endTime) {
        this(roomId, participantIds, startTime, endTime, null);
    }

    // Case 2: Aminites (Might not pass roomid as null).
    public BookingRequest(String roomId, List<String> participantIds,
                          LocalDateTime startTime, LocalDateTime endTime,
                          Set<Amenity> requiredAmenities) {
        this.roomId = roomId;
        this.participantIds = participantIds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredAmenities = requiredAmenities;
    }

WHY WE SHIFTED to named static factories "method"
  PROS
   - Can't build a contradictory request: with amenities + roomId
   - Clarity -  states the intention by name instead.
   - No overload ambiguity/telescoping as optional fields grow.


BookingRequestFactory is a separate class which makes a separate class called BookingRequest
* In our class creates itself so cant rename it as a Factory class
* Our  kind of classes are called Static Factory "Method" Not a Factory class
* Factory class = a separate object whose sole job is creating (usually a different type)
* Static factory "method" = the class creates itself via named methods. E.g. forRoom(...) / forAmenities(...) ARE factory methods
 * */

public class BookingRequest {

    private final List<String> attendeeIds;
    private final TimeSlot slot;
    private final String roomId;

    // Amenity is an ENUM, so the compiler already guarantees only real amenities are passed as input so no input validations needed
    private final Set<Amenity> requiredAmenities;

    // Private constructor: callers must create via factory "method" -> a contradictory (roomId + amenities) request
    private BookingRequest(String roomId, Set<Amenity> requiredAmenities,
                           List<String> attendeeIds, TimeSlot slot) {
        this.roomId = roomId;
        this.requiredAmenities = (requiredAmenities == null) ? null : Set.copyOf(requiredAmenities);
        this.attendeeIds = List.copyOf(attendeeIds);
        this.slot = slot;
    }

    // Case 1: caller wants a SPECIFIC room. No amenities.
    public static BookingRequest forRoom(String roomId, List<String> attendeeIds, TimeSlot slot) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is required for a specific-room booking");
        }
        validateAttendeeIds(attendeeIds);
        return new BookingRequest(roomId, null, attendeeIds, slot);
    }

    /*    Case 2/3: caller wants ANY room matching ALL required amenities (strict). No roomId.
        An empty set is allowed and means "any room that fits" (no amenity filter).*/
    public static BookingRequest forAmenities(Set<Amenity> requiredAmenities, List<String> attendeeIds, TimeSlot slot) {
        if (requiredAmenities == null) {
            throw new IllegalArgumentException("requiredAmenities must not be null (use an empty set for 'any room')");
        }
        validateAttendeeIds(attendeeIds);
        return new BookingRequest(null, requiredAmenities, attendeeIds, slot);
    }

    /*This class Booking only validates for NOT null inputs E.g. Non-null roomId but NOT if the room id entered is correct or not because
     * BookingRequest: Separation of concern -Only Acts as DTO. Should NOT have RoomService/UserService injected for validation. Easy to test without any DI.
     * BookingService class - What if room is validated at BookingReq and then by the time it reaches service its deleted, so validation must be at service*/
    private static void validateAttendeeIds(List<String> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            throw new IllegalArgumentException("attendeeIds must not be null or empty — a booking needs at least one attendee");
        }
        // stream, not contains(null): immutable List.of(...) throws NPE on contains(null)
        if (attendeeIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("attendeeIds must not contain null");
        }
    }

    public boolean hasSpecificRoom() {
        return roomId != null;
    }

    public int getRequiredCapacity() {
        return attendeeIds == null ? 0 : attendeeIds.size();
    }

    public List<String> getAttendeeIds() {
        return attendeeIds;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public LocalDateTime getStartTime() {
        return slot.getStart();
    }

    public LocalDateTime getEndTime() {
        return slot.getEnd();
    }

    public String getRoomId() {
        return roomId;
    }

    public Set<Amenity> getRequiredAmenities() {
        return requiredAmenities;
    }
}


