package notification;

import booking.BookingEvent;
import user.User;

public class EmailNotification implements Notification{
    @Override
    public NotificationPayload build(BookingEvent e, User u) {
        return new NotificationPayload(u.getEmail(),"Suject", "Body");
    }

    @Override
    public void sendNotification(NotificationPayload p) {
        System.out.println("Email sent successfully!");
    }
}
