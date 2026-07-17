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

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}

/*
* final on the fields - immutable
*
public final class Notification { - final on class
It stops anyone from extending Notification. No one could break immutability and because no one could add mutable fields or override getters to return changing values
* * */
