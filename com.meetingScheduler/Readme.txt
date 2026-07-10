Entity / Model class almost means same. Entity -> more towards DB. Better to use Model class - It is staeteful class used to store.

The word service id confusion because it can mean 
1. MS
2. @Service in SB
3. LLD - A class whose primary job is business logic or "behaviour" or coordination, rather than just holding data


Basic features
Notification - Invite, reminder, cancel
Room amenities - Builder + Decorator
Meeting - Book/cancel

More features
Time zone difference
Only working hours
Calendar view of each person
Meeting update - time/room,attendee
Waitlist for room
Analytics - Room, user level


Edge Cases to Handle
ScenarioHow to handleOrganizer cancels after attendees acceptedNotify all, free their slotsAttendee in different timezoneStore all times in UTC, display in local tzRoom capacity exceededReject booking, suggest larger roomMeeting with no room (virtual)Room is optional, not mandatoryAttendee added after meeting createdSend late invite, check their availabilityRecurring meeting room unavailable on one dateFlag that instance, suggest alternate roomTwo organizers book same room simultaneouslyOptimistic locking or mutex on room slot

Purely packaging by layers
model/       → data holders (any feature's data)
service/     → business logic (any feature's logic)
repository/  → database access (any feature's persistence)
controller/  → entry points (any feature's endpoints)

model/
    Booking.java
    Participant.java
    BookingEvent.java
    Channel.java          ← EVEN Enum is here
service/
    BookingService.java
    Notifier.java.       -> Interface
    NotifierFactory.java
    EmailNotifier.java.  -> Concrete

BUT WE CAN HAVE FEATURE WISE PACKAGING ALSO

booking/
    Booking.java
    BookingEvent.java
    Participant.java
    BookingService.java
room/                         ← new feature folder
    MeetingRoom.java          ← Room lives in its own feature
    Amenity.java
    RoomType.java
    RoomService.java
notification/
    Notifier.java
    Channel.java              ← here, it's a notification concept
    NotifierFactory.java
    EmailNotifier.java
    SmsNotifier.java
    WhatsAppNotifier.java


Room → its own room/ feature
A meeting room is a distinct domain concept with its own data (capacity, amenities, type),
its own logic (availability, pricing), and its own lifecycle independent of any booking.
That independence is the test for "does this deserve its own feature folder?" — and it clearly passes.
So MeetingRoom.java, the Amenity enum, RoomType, and RoomService all get a room/ package.


The underlying principle
Same test as always — "what concept is this fundamentally about, and can it exist on its own?"

User exists independently of bookings → its own feature (user/).
Participant exists only within a booking → lives in booking/.

In production grade its a mix of both where are there are layers inside features

ooking/
    model/
        Booking.java
        Participant.java
        BookingEvent.java         ← data payload
        BookingStatus.java        ← ENUM (it's data)
        BookingObserver.java      ← INTERFACE
    service/
        BookingService.java       ← logic
    repository/
        BookingRepository.java    ← INTERFACE (persistence contract → lives in repository layer)
    exception/
        BookingNotFoundException.java   ← EXCEPTION (own sub-folder, cross-cutting)

