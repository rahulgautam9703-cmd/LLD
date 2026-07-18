package booking;

import java.time.LocalDateTime;
import java.util.List;
/*Should Booking class (not fields) be final?
Pros
It protects the immutability
class EvilBooking extends Booking {
    private String id;    // shadow mutable state
    @Override public String getId() { return id; } // now non-deterministic
}
Cons
1.
ORM proxying. If Booking later becomes a JPA/Hibernate entity, those frameworks subclass it at runtime for lazy-loading proxies — final breaks that. For your in-memory model this is irrelevant, but it's the #1 reason real entity classes are sometimes left non-final.
2.
Mocking. Some mock frameworks can't mock final classes. Barely applies here — you don't mock a data holder, you construct it; you mock services/interfaces.
3.
Future variants. If you ever wanted RecurringBooking / PrivateBooking, final blocks it — but the right tool for a closed set of variants is a sealed class (permit specific subtypes), not leaving it fully open. Not needed now.
* */

public final class Booking {

    private final String id;
    // attendeeIds = the USERS in this meeting
    // ("Participant"/observer) is an attendee who wants notifications of subject (Booking)
    private final List<String> attendeeIds;
    private final TimeSlot slot;   // start+end interval (encapsulates overlap logic)
    private final String roomId;

    public Booking(String id, String roomId, List<String> attendeeIds, TimeSlot slot) {
        this.id = id;
        this.roomId = roomId;
        this.attendeeIds = attendeeIds;
        this.slot = slot;
    }
//--------------------------------------------------------
    public String getId() {
        return id;
    }

    public List<String> getAttendeeIds() {
        return attendeeIds;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    // convenience delegates so callers/messages can still read start/end directly
    public LocalDateTime getStartTime() {
        return slot.getStart();
    }

    public LocalDateTime getEndTime() {
        return slot.getEnd();
    }

    public String getRoomId() {
        return roomId;
    }
}

/*Storing Room vs roomId
Room directly
Pros: fast fetch instead of lookup via RoomRepo
Cons: Stale data chance, more memory, tight coupling
*/
