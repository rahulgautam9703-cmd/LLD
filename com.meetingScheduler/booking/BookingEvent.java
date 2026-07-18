package booking;

import java.time.LocalDateTime;
/*
* Pros (why a dedicated Bookingevent class)
1. Only for i/p to BookingObservers, not passing the "Booking class as i/p - overexposing". Decouple Observer and Booking class
2. Stable i/p parameter inside observer notifyBooking(BookingEvent), instead of Passing primitives (notify(String title, LocalDateTime t, String loc)) →  new field breaks every observer's signature.
3. Testable. Trivial to construct a BookingEvent to unit-test an observer.

Cons
1. Extra class + mapping. Field duplication.
 */

// Immutable read-only snapshot
public final class BookingEvent {
    private final String bookingId;
    private final String roomId;
    private final LocalDateTime time; //NOTICE
    private final EventType eventType;
    //Good to have (later): Organiser, list of participants

    public BookingEvent(String bookingId, String roomId, LocalDateTime time, EventType eventType) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.time = time;
        this.eventType = eventType;
    }
//-- no setters because the fields are final ------------------
    public String getBookingId() {
        return bookingId;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public EventType getEventType() {
        return eventType;
    }
}
