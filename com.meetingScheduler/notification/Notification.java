package notification;

import booking.BookingEvent;
import user.User;

public interface Notification {
    //void notify(); //Reserved - Each Object has notify() method for inter thread communication.

    // COMBINED build + send: one method both renders the message AND delivers it.
    // Input = the domain objects (event + user), because building needs them to render
    // content and resolve the recipient (email vs phone).
    //
    // PROS: simplest interface; one call from the observer; can't send an unbuilt/mismatched payload.
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
But in this case Observer class becomes responsible for providing different input (Email/Phone/App) etc inside itself to call different notifier

Option 2
send(BookingEvent event, ContactInfo contact);
Here each notifier both formats the message and pulls the right field from ContactInfo (email vs phone).

Option 3
Production grade but over kill here




Option B
public void update(BookingEvent e) {
    for (Channel c : channels) {
        Notifier notifier = NotifierFactory.create(c);
        Notification msg = ...build...;
        notifier.send(msg);   // notifier only delivers
    }
}
public interface Notifier {
    Notification build(BookingEvent e, User user);  // render + address, per channel
    void send(Notification n);                       // deliver
}

Bifurcating building and sending further which is production grade because notification can be of many types not just bokking confirmed
Unit testing can be easy for building message and sending them separately.
In general any notification send should have have a common Message msg (payload) as input and send it and should not know how is it built

* */
