package booking;

import notification.CommunicationPreference;
import notification.Notification;
import notification.NotificationFactory;
import user.User;

import java.util.HashSet;
import java.util.Set;

public class Participant implements BookingObserver{
    private final User user;
    private final Set<CommunicationPreference> communicationPreference;
    /*We didnt use boolean requireEmailNotification, boolean requireSMSNotification etc to prevent explosion*/
/*Set is required for feature if someone wants to enquire about All communcation channel patticipant has subscribed to
Also Notifier  */


    public Participant(User user) {
        //SAMPLE INPUT EMAIL VALIDATION CODE
        /*if (email == null || email.isBlank() || !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }*/
        this.user = user;
        //this.communicationPreference = new HashSet<>(CommunicationPreference.EMAIL); //No such constructor present
        this.communicationPreference = new HashSet<>();
        this.communicationPreference.add(CommunicationPreference.EMAIL);

    }
   /* private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }*/

    /*Channel Subscription should be dynamic and should NOT be tied to the creation of participants, at any time can this be subscribed/unsubscribed*/

    public void subscribe (CommunicationPreference c)
    {
        this.communicationPreference.add(c);
    }

    public void unsubscribe (CommunicationPreference c)
    {
        this.communicationPreference.remove(c);
    }

    /*Another thing worth thinking is what should be input to notifyBooking method of BookingObserver interface
    * As of now we are */

    @Override
    public void notifyBooking(BookingEvent event) {
        for(CommunicationPreference c : communicationPreference)
        {
            Notification n = NotificationFactory.create(c);

        }
    }


/*
* Participant should NOT do this
public void update(BookingEvent e) {
    for (Channel c : channels) {
        Notification msg = new Notification(       // render + address (a builder would do this in prod)
                c == Channel.EMAIL ? user.getEmail() : user.getPhone(),
                e.title + " " + e.type,
                "On " + e.dateTime + " at " + e.location);
        Notifier notifier = NotifierFactory.create(c);   // pick transport
        notifier.send(msg);                              // dumb delivery
    }
}
Because
* 1. as notifications increases or channels increases Participant class will have to change itself.
* 2. Participant class should not decide what to send as message into the notification
*
* So in general notifyBooking() is doing three jobs — pick recipient, render content, deliver — when its only real job is orchestrate.
*/


//Participant is wrapper class around USER which is an observer



    //Boilerplate code
    public User getUser() {
        return user;
    }

    public String getName() {
        return user.getName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getPhone() {
        return user.getPhone();
    }

    public Set<CommunicationPreference> getCommunicationPreference() {
        return communicationPreference;
    }
}

/*Import both SET and HASHSET*/
/*
* The two options
A. Current — Participant has-a User object
•
✅ notifyBooking needs email/phone immediately to send notifications — it has them directly, no lookup.
•
✅ User is immutable (final fields), so holding the reference can't go stale.
•
✅ No dependency on a UserService buried in the notification path.
•
❌ No central place that owns User lifecycle / uniqueness; main is the de-facto registry.
B. UserService owns users, Participant stores userId
•
✅ Single source of truth for users; register once, reuse across roles (participant, organizer, etc.) — matches your own User.java comment ("User can be participant, author, scrum master").
•
✅ Users outlive any one booking.
•
❌ To send a notification, Participant (or the notifier) must now hold a UserService and resolve userId → User every time — pushes a service dependency deep into the observer, more indirection.
•
❌ Overkill if users never change and aren't shared.*/
