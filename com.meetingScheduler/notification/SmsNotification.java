package notification;

import booking.BookingEvent;
import user.User;

public class SmsNotification implements Notification {

    // build + send combined into one method (see Notification interface for pros/cons + how to split later).
    @Override
    public void send(BookingEvent e, User u) {
        // --- build step (render + resolve recipient) — would become a separate build() when decoupled ---
        NotificationPayload payload = new NotificationPayload(
                u.getPhone(),                                  // phone is the recipient for this channel
                null,                                          // SMS has no subject
                "Booking " + e.getBookingId() + " at " + e.getTime()); // body

        // --- send step (deliver) — reads only from the payload; would take it as its only input when decoupled ---
        System.out.println("SMS sent to " + payload.getRecipient() + " | " + payload.getBody());
    }
}
