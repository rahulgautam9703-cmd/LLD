package booking; //NOTICE FIRST LINE ALWAYS

import notification.CommunicationPreference;
import room.NoRoomAvailableException;
import room.Room;
import room.RoomCapacityExceededException;
import room.RoomService;
import shared.AppException;
import user.User;
import user.UserService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//Booking service class is (orchestration + the observer subject)
public class BookingService {

    // Global operating hours: bookings allowed only within this window (same day).
    private static final LocalTime OPEN = LocalTime.of(10, 0);
    private static final LocalTime CLOSE = LocalTime.of(22, 0);

    private final Map<String, Booking> bookings = new HashMap<>(); // Main repo for successful bookings

/*Any attendee/User who wants to receive notification has to register here as Participant (Booking Observer)*/
    // ConcurrentHashMap: notifyObservers now iterates this OUTSIDE the booking lock (see createBooking),
    // so it can race with subscribe/unsubscribe -> a thread-safe map avoids ConcurrentModification.
    private final Map<String, Participant> notificationSubscribers = new ConcurrentHashMap<>();

    private final RoomService roomService;
    private final UserService userService;                  // to verify an attendeeId is a REAL userId (referential integrity)
    // non-final + volatile so the policy can be SWAPPED AT RUNTIME (see setter). volatile = safe
    // publication: createBooking may read it on one thread while another is changing it.
    private volatile RoomSelectionStrategy roomSelectionStrategy; // pluggable
    private final AtomicInteger bookingSeq = new AtomicInteger(0); // BookingService owns booking id generation

  /* DEPENDENCY INJECTION vs SINGLETON — why this class takes its collaborators in the constructor
     instead of being a global `BookingService.getInstance()`:
     PROS of DI (what we do)
       - Testable
       - Explicit dependencies: the constructor documents exactly what this class needs; a Singleton
         hides them behind static getInstance() calls scattered in the code.
       - Swappable behaviour: inject BestFitSelection or FirstComeFirstServeSelection without touching
         this class (Open/Closed). A Singleton strategy would be a global you can't vary per use.
       - No global mutable state / lifecycle controlled by the caller.
     CONS of DI
       - Caller (composition root / main) must wire the graph and pass it around — more boilerplate.
       - No single global access point; you must hold a reference.
     A Singleton's only real win here (one shared instance, easy global access) is better achieved by
     just creating one instance in main and injecting it — so DI wins for this design.
     Dependencies stay one-directional (BookingService -> RoomService/UserService); no cycle. */

    public BookingService(RoomService roomService, UserService userService,
                          RoomSelectionStrategy roomSelectionStrategy) {
        this.roomService = roomService;
        this.userService = userService;
        this.roomSelectionStrategy = roomSelectionStrategy;
    }

    // WHY a setter: runtime-swap possible. Cost of this option: the field must be mutable (non-final + volatile).
    public void setRoomSelectionStrategy(RoomSelectionStrategy roomSelectionStrategy) {
        this.roomSelectionStrategy = roomSelectionStrategy;
    }

    public Booking createBooking(BookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("BookingRequest must not be null");
        }
        Booking booking = reserve(request); // Only this block sync instead of createBooking as notify doesnt require sync
        notifyObservers(booking, new BookingEvent(booking.getId(), booking.getRoomId(), booking.getStartTime(), EventType.CONFIRMED));
        return booking;
    }

    // synchronized - two concurrent requests could both pass isRoomAvailable and book the same slot (TOCTOU).
    private synchronized Booking reserve(BookingRequest request) {
        validateTimeWindow(request);   // outside global operating hours?
        validateAttendees(request);    // >=1 attendee, all real users, none already double-booked

        Room room = request.hasSpecificRoom()
                ? validateSpecificRoom(request)
                : selectRoom(request); // Room Selection strategy

        // Service generates the id now (only after all checks passed, so ids aren't wasted on failed requests)
        String bookingId = "B" + bookingSeq.incrementAndGet();
        Booking booking = new Booking(bookingId, room.getRoomId(), request.getAttendeeIds(), request.getSlot());
        bookings.put(booking.getId(), booking);
        return booking;
    }

    // Start<end and non-null are already guaranteed by TimeSlot's constructor.
    private void validateTimeWindow(BookingRequest request) {
        TimeSlot slot = request.getSlot();
        if (slot.getStart().toLocalTime().isBefore(OPEN) || slot.getEnd().toLocalTime().isAfter(CLOSE)) {
            throw new AppException(
                    "Bookings are allowed only between " + OPEN + " and " + CLOSE,
                    "OUTSIDE_OPERATING_HOURS");
        }
    }

    // Existence/state checks only. "non-null, non-empty, no-null-ids" are already guaranteed by BookingRequest.
    private void validateAttendees(BookingRequest request) {
        List<String> attendeeIds = request.getAttendeeIds();
        for (String attendeeId : attendeeIds) {
            // Referential integrity: throws UserNotFoundException (USER_NOT_FOUND) if the id is a ghost.
            userService.requireExists(attendeeId);
            boolean alreadyBusy = bookings.values().stream()
                    .filter(b -> b.getAttendeeIds() != null && b.getAttendeeIds().contains(attendeeId))
                    .anyMatch(b -> b.getSlot().overlaps(request.getSlot()));
            if (alreadyBusy) {
                throw new AppException(
                        "Attendee " + attendeeId + " already has an overlapping booking",
                        "ATTENDEE_UNAVAILABLE");
            }
        }
    }

    private Room validateSpecificRoom(BookingRequest request) {
        //Does room exists
        Room room = roomService.findById(request.getRoomId());
        //Is capacity okay?
        int attendees = request.getRequiredCapacity();
        if (!room.canFit(attendees)) {
            throw new RoomCapacityExceededException(room.getRoomId(), attendees);
        }
        // Slot conflict
        if (!isRoomAvailable(room.getRoomId(), request.getSlot())) {
            throw new AppException(
                    "Room " + room.getRoomId() + " is already booked for the requested time",
                    "ROOM_NOT_AVAILABLE");
        }
        return room;
    }

    // Case 2/3: RoomService filters by attributes (capacity + must have ALL amenities); we filter those
    // by availability, then delegate the "which one?" choice to the injected RoomSelectionStrategy.
    private Room selectRoom(BookingRequest request) {
        List<Room> available = roomService
                .findByRequirements(request.getRequiredCapacity(), request.getRequiredAmenities())
                .stream()
                .filter(r -> isRoomAvailable(r.getRoomId(), request.getSlot()))
                .collect(Collectors.toList());

        return roomSelectionStrategy.select(available)
                .orElseThrow(() -> new NoRoomAvailableException(
                        "need capacity " + request.getRequiredCapacity()
                                + " + amenities " + request.getRequiredAmenities()
                                + " for " + request.getStartTime() + "–" + request.getEndTime()));
    }

    // synchronized: reads the shared bookings map, which createBooking/cancel mutate under the same lock.
    public synchronized Booking getBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException(bookingId);
        }
        return booking;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = removeBooking(bookingId); //Sync
        notifyObservers(booking, new BookingEvent(booking.getId(), booking.getRoomId(), booking.getStartTime(), EventType.CANCELLED));
    }

    private synchronized Booking removeBooking(String bookingId) {
        Booking booking = getBooking(bookingId);   // Find-then-remove on the shared bookings map must be Sync
        bookings.remove(bookingId);
        return booking;
    }

    private boolean isRoomAvailable(String roomId, TimeSlot slot) {
        return bookings.values().stream()
                .filter(b -> b.getRoomId().equals(roomId))
                .noneMatch(b -> b.getSlot().overlaps(slot));
    }

/*Any Attendee (User) who wish to receive notification of Subject - Booking must register themselves at booking service along with Comm preference*
BookingService = subject = owns the observer collections + notifies
 So participant is observer to Booking service subject, so at the time of registeration to subject ,they should disclose their communication preference  wrt Booking Event NOT ANY OTHER EVENT/SUBJECT
 They can have difference preference for different subjects like Booking (Email), JIRA (SMS), Slack (SMS) etc.*/
    public Participant subscribeToNotifications(User user, Set<CommunicationPreference> preferences) {
        Participant participant = new Participant(user, preferences);
        notificationSubscribers.put(user.getId(), participant);
        return participant;
    }

    public void unsubscribeFromNotifications(User user) {
        notificationSubscribers.remove(user.getId());
    }

    // PRIVATE
    // An attendee who never subscribed is simply skipped (intended: they chose not to be notified) —
    // that's why the null check below is correct behavior, not a dropped bug.
    private void notifyObservers(Booking booking, BookingEvent event) {
        if (booking.getAttendeeIds() == null) {
            return;
        }
        for (String attendeeId : booking.getAttendeeIds()) {
            Participant subscriber = notificationSubscribers.get(attendeeId);
            if (subscriber != null) {      // attendee opted in -> notify; else skip (didn't subscribe)
                subscriber.notifyBooking(event);
            }
        }
    }
}




