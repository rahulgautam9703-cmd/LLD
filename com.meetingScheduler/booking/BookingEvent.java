package booking;

import java.time.LocalDateTime;

public class BookingEvent {
    private String bookingId;
    private LocalDateTime time; //NOTICE
    private EventType eventType;

    /*Good to have: Organisaer, Location, List of all the participants
    * What should not go inside this What should NOT go in
The live Booking entity. Tempting, but it over-exposes — the observer gets the whole aggregate including its observer list, internal state, and mutation methods. Pass a snapshot of the needed fields instead*/


    public BookingEvent(String bookingId, LocalDateTime time, EventType eventType) {
        this.bookingId = bookingId;
        this.time = time;
        this.eventType = eventType;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
