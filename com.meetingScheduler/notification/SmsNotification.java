package notification;

import booking.BookingEvent;
import user.User;

public class SmsNotification implements Notification{
        @Override
        public NotificationPayload build(BookingEvent e, User u) {
            return new NotificationPayload(u.getPhone(),"Suject", "Body");
        }

        @Override
        public void sendNotification(NotificationPayload p) {
            System.out.println("SMS sent successfully!");
        }
    }

