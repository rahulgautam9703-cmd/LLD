package notification;

import booking.BookingEvent;
import user.User;

public interface Notification {
    //void notify(); //Each Object has notify() method for inter thread communication so cant use this method name
    NotificationPayload build(BookingEvent e, User u);
    void sendNotification(NotificationPayload p);
}

/*
What shall be passed inside sendNotification() ?

Option 1
(BookingEvent event, String contact) // EMAIL in strong or mobile number in string
But in this case Observer class becomes responsible for providing different inputd number number, email etc inside itself to call different notifier

Option 2
send(BookingEvent event, ContactInfo contact);
Here each notifier both formats the message and pulls the right field from ContactInfo (email vs phone). Simple,
but it means formatting logic lives inside every notifier, and each notifier must know ContactInfo's shape.


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
