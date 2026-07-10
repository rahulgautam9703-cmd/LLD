package notification;
/* If we don't mention first line as package, so it means it goes to default package/root which is com.meetingScheduler
and in such case no other package/folder will be able to import the default package
So this line is of utmost importance*/

public enum CommunicationPreference
{
    EMAIL,
    SMS,
    APP_NOTIFICATION
}

//If NOT public then other packages cannot access it.
