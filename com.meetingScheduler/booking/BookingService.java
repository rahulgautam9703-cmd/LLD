package booking; //NOTICE FIRST LINE ALWAYS

import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private final List<BookingObserver> observers = new ArrayList<>();

    public void addParticipant(BookingObserver observer) {
        observers.add(observer);
    }

    public void notifyParticipants(BookingEvent event) {
        observers.forEach(observer -> observer.notifyBooking(event));
    }
}
