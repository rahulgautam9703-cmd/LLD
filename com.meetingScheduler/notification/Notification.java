package notification;

import booking.BookingEvent;
import user.User;

public interface Notification {
    //void notify(); //Cant used this method -Reserved - Each Object class object has notify() method for inter thread communication.

    // COMBINED build + send: one method both renders the message AND delivers it. -- For now choosing this.
    // PROS: simplest
    // CONS: build and send become ONE responsibility ->
    //        - can't unit-test rendering without actually delivering,
    //        - can't retry send without re-building,
    //        - transport is coupled to the domain (send knows BookingEvent/User).
    //
    // FUTURE DECOUPLE (production-grade) — split back into two roles:
    //     NotificationPayload build(BookingEvent e, User u); // channel-aware rendering, testable
    //     void send(NotificationPayload p);                  // dumb transport, domain-agnostic
    // Then send() takes a self-contained payload (resolved recipient + subject + body), not the domain.
    void send(BookingEvent e, User u);
}

/*
What shall be passed inside sendNotification() ?

Option 1
(BookingEvent event, String contact) // EMAIL/Mobile number in String contact
But in this case Observer (Participant) class becomes responsible for providing different input (Email/Phone/App) etc inside itself to call different notifier

Option 2
send(BookingEvent event, ContactInfo contact);
Here each notifier both formats the message and pulls the right field from ContactInfo (email vs phone).

*/
