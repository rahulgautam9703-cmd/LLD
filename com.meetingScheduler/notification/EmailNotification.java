package notification;

import booking.BookingEvent;
import user.User;

public class EmailNotification implements Notification {

    // build + send combined into one method
    @Override
    public void send(BookingEvent e, User u) {
        // --- build
        NotificationPayload payload = new NotificationPayload(
                u.getEmail(),  // email
                "Booking " + e.getEventType(),
                "Booking " + e.getBookingId() + " in room " + e.getRoomId() + " at " + e.getTime());

        // --- send
        System.out.println("EMAIL sent to " + payload.getRecipient()
                + " | " + payload.getSubject() + " | " + payload.getBody());
    }
}
