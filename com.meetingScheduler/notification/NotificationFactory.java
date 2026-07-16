package notification; //Always first line is package

public class NotificationFactory {
    //Notice method
    public static  Notification create (CommunicationPreference c)
    {
        Notification type = null;
        switch(c)
        {
            case EMAIL -> type = new EmailNotification();
            case SMS -> type = new SmsNotification();
        }
        return type;
    }
}
/*This class is basically linking of Channel to particular notification.
Which should have been done in Participant class instead creation logic
which nothing but based on Communication channel ids moved in a separate class called factory*/
