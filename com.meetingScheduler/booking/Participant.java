package booking;

import notification.CommunicationPreference;
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
        System.out.println("Participant " + user.getName() + " notified: " + event);
    }









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
