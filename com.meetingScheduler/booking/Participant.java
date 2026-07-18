package booking;

import notification.CommunicationPreference;
import notification.Notification;
import notification.NotificationFactory;
import user.User;

import java.util.HashSet;
import java.util.Set;
/*
A Participant = a User who has OPTED IN to booking notifications (i.e. an observer of BookingEvents).
An attendee/User who doesn't need any notification is simply never a Participant, and
that's fine.
Import both SET and HASHSET
* Should it hold reference to User or UserId?
* User: Already immutable so no staleness
* Id: single source of truth. Every notification would force Participant to hold a UserService and resolve userId → User. Overkill when User never changes and is already immutable.
* */
public class Participant implements BookingObserver{
    private final User user;
    private final Set<CommunicationPreference> communicationPreference;
    /*We didnt use boolean requireEmailNotification, boolean requireSMSNotification etc to prevent explosion
Set is required for feature if someone wants to enquire about All communcation channel patticipant has subscribed to
Also Notifier  */

    // Package-private: only the booking package (i.e. BookingService) creates participants,
    // mirroring how only UserService creates users. main() can't do `new Participant(...)`.
    // The subscriber states WHICH channels they want at subscribe time (EMAIL/SMS/...). If none are
    // given we default to EMAIL. Preferences stay dynamic afterwards via subscribe()/unsubscribe().
    Participant(User user, Set<CommunicationPreference> preferences) {
        this.user = user;
        this.communicationPreference = (preferences == null || preferences.isEmpty())
                ? new HashSet<>(Set.of(CommunicationPreference.EMAIL)) // sensible default
                : new HashSet<>(preferences);                          // defensive copy
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

    @Override
    public void notifyBooking(BookingEvent event) {
        for(CommunicationPreference c : communicationPreference)
        {
            Notification n = NotificationFactory.create(c);
            if (n == null) {
                continue; // channel not yet supported by the factory (e.g. APP_NOTIFICATION)
            }
            n.send(event, user); // combined build + send in one call
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

