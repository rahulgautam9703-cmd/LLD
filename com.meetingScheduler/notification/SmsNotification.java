package notification;

import booking.BookingEvent;
import user.User;

public class SmsNotification implements Notification {

    // build + send combined into one method (see Notification interface for pros/cons + how to split later).
    @Override
    public void send(BookingEvent e, User u) {
        // --- build step
        NotificationPayload payload = new NotificationPayload(
                u.getPhone(),                                  // phone is the recipient for this channel
                null,
                "Booking " + e.getBookingId() + " in room " + e.getRoomId() + " at " + e.getTime());

        // --- send step
        System.out.println("SMS sent to " + payload.getRecipient() + " | " + payload.getBody());
    }
}
