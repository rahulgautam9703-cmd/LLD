package notification;

public interface Notification {
    //void notify(); //Each Oject has notify() method for inter thread communication
    void sendNotification();
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


Each notifier

* */
