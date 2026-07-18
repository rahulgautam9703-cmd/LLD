package booking;

public interface BookingObserver {
    void notifyBooking(BookingEvent event); //Generally should have an event-based notification
}

/* By default all methods inside interface are PUBLIC and ABSTRACT unless specified otherwise

What should be input parameter for notifyBooking method?

Option 1
Not primitives (update(String title, LocalDateTime time, String location)) — every new field breaks every observer's signature.

Option 2
Not the whole Booking entity (update(Booking booking)) — this over-exposes.

Option 3
A dedicated BookingEvent is the sweet spot: immutable, carries exactly the facts of the event.
 */
