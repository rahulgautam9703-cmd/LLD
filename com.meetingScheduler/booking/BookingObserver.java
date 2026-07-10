package booking;

public interface BookingObserver {
    void notifyBooking(BookingEvent event);
}

/* By default all methods are PUBLIC and ABSTRACT unless specified otherwise
What should be input parameter for notifyBooking method?

Option 1
Not primitives (update(String title, LocalDateTime time, String location)) — every new field breaks every observer's signature.

Option 2
Not the whole Booking entity (update(Booking booking)) — this over-exposes. You'd be leaking the entity across a boundary and inviting observers to mutate it.
An event is a read-only snapshot of just what's relevant about the change.

Option 3
A dedicated BookingEvent is the sweet spot: immutable, carries exactly the facts of the event (what booking, what changed, when), and decouples the observer from the entity.
It is more production grade


It reactes to a booking event (sort of event based notification)


 */
