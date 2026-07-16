package notification;

public final class NotificationPayload {
    private final String recipient;   // resolved address: an email OR a phone number
    private final String subject;     // may be null (SMS has no subject)
    private final String body;

    public NotificationPayload(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }
}

/*
* final on the fields (the important one here)
*This means the field can be assigned exactly once — in the constructor — and never reassigned afterward. That's what makes the object immutable:

public final class Notification {
It stops anyone from subclassing Notification. Why that helps for a value object:
Preserves immutability. If someone could extend Notification, a subclass could add mutable fields or override getters to return changing values — quietly breaking the immutability guarantee you built. final on the class slams that door: the object you defined is exactly the object everyone uses.
Value objects aren't meant to be extended. A Notification is a plain data holder, not a base for a hierarchy. Marking it final says "this is a complete, standalone value — don't build on it."

* * */
