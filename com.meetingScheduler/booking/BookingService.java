package booking; //NOTICE FIRST LINE ALWAYS

import room.NoRoomAvailableException;
import room.Room;
import room.RoomCapacityExceededException;
import room.RoomService;
import shared.AppException;
import user.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingService {

    // Global operating hours: bookings allowed only within this window (same day).
    private static final LocalTime OPEN = LocalTime.of(10, 0);
    private static final LocalTime CLOSE = LocalTime.of(22, 0);

    private final Map<String, Booking> bookings = new HashMap<>(); // bookingId -> Booking, for O(1) get/cancel
    private final List<BookingObserver> systemObserver = new ArrayList<>(); //Optional Non-participant observers
    private final Map<String, Participant> participants = new HashMap<>(); // Participants are stored in BookingService
    private final RoomService roomService; // private final List<Room> rooms = new ArrayList<>();
    private final AtomicInteger bookingSeq = new AtomicInteger(0); // BookingService owns booking id generation

  /*To avoid booking service is becoming a god class
    Note that User can have a user service which has a list but participant is a special user just for service booking so it can be kept here,
    room can have a separate room service which will have a list of room so right now we are creating room and adding it here we could have added it there and had a roomService injected in booking service
    Ideally Booking service should just have list of bookings
    But since participant is a booking-specific role (a User wrapped as an observer for a meeting),  so it is stored here
    But User is stored in userService*/

    public BookingService(RoomService roomService) {
        this.roomService = roomService;
    }

    // Caller passes constraints (a BookingRequest). The service picks the room:
    //  - specific room named  -> validate that one
    //  - no room named        -> select any room matching capacity + amenities + availability
    // synchronized: the availability checks + the put must be one atomic step, otherwise two
    // concurrent requests could both pass isRoomAvailable and then both book the same slot (TOCTOU).
    public synchronized Booking createBooking(BookingRequest request) {
        validateTimeWindow(request);      // reject bad/out-of-hours windows before touching rooms
        validateParticipants(request);    // must have >=1 participant, none already double-booked

        Room room = request.hasSpecificRoom()
                ? validateSpecificRoom(request)
                : selectRoom(request);

        Booking booking = new Booking(
                room.getRoomId(),
                request.getParticipantIds(),
                request.getStartTime(),
                request.getEndTime());

        // Service owns identity: assign the booking id here, don't trust one off the caller.
        booking.setId("B" + bookingSeq.incrementAndGet());
        bookings.put(booking.getId(), booking);
        notifyParticipants(booking, new BookingEvent(booking.getId(), booking.getStartTime(), EventType.CONFIRMED));
        return booking;
    }

    // Global rule: start must be before end, same day, and within OPEN..CLOSE operating hours.
    private void validateTimeWindow(BookingRequest request) {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        if (start == null || end == null || !start.isBefore(end)) {
            throw new AppException("Booking start must be before end", "INVALID_TIME_WINDOW");
        }
        // Operating-hours check assumes a single-day booking; reject cross-day windows.
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new AppException("Booking must start and end on the same day", "INVALID_TIME_WINDOW");
        }
        if (start.toLocalTime().isBefore(OPEN) || end.toLocalTime().isAfter(CLOSE)) {
            throw new AppException(
                    "Bookings are allowed only between " + OPEN + " and " + CLOSE,
                    "OUTSIDE_OPERATING_HOURS");
        }
    }

    // A booking needs at least one participant, and none of them may already be in an
    // overlapping meeting (a person can't be in two rooms at once).
    private void validateParticipants(BookingRequest request) {
        List<String> participantIds = request.getParticipantIds();
        if (participantIds == null || participantIds.isEmpty()) {
            throw new AppException("A booking needs at least one participant", "NO_PARTICIPANTS");
        }
        for (String participantId : participantIds) {
            boolean alreadyBusy = bookings.values().stream()
                    .filter(b -> b.getParticipantsId() != null && b.getParticipantsId().contains(participantId))
                    .anyMatch(b -> request.getStartTime().isBefore(b.getEndTime())
                            && b.getStartTime().isBefore(request.getEndTime()));
            if (alreadyBusy) {
                throw new AppException(
                        "Participant " + participantId + " already has an overlapping booking",
                        "PARTICIPANT_UNAVAILABLE");
            }
        }
    }

    // Case 1: caller named a room -> it must exist, fit the head count, and be free for the window.
    private Room validateSpecificRoom(BookingRequest request) {
        Room room = roomService.findById(request.getRoomId());
        int attendees = request.getRequiredCapacity();
        if (!room.canFit(attendees)) {
            throw new RoomCapacityExceededException(room.getRoomId(), attendees);
        }
        // Availability check lives HERE (not in RoomService): bookings are owned by this
        // service, so no BookingService <-> RoomService cycle is needed.
        if (!isRoomAvailable(room.getRoomId(), request.getStartTime(), request.getEndTime())) {
            throw new AppException(
                    "Room " + room.getRoomId() + " is already booked for the requested time",
                    "ROOM_NOT_AVAILABLE");
        }
        return room;
    }

    // Case 2/3: RoomService filters by attributes (capacity + amenities); we filter those by
    // availability and pick best-fit (smallest room that still works) to avoid wasting big rooms.
    private Room selectRoom(BookingRequest request) {
        return roomService.findByRequirements(request.getRequiredCapacity(), request.getRequiredAmenities())
                .stream()
                .filter(r -> isRoomAvailable(r.getRoomId(), request.getStartTime(), request.getEndTime()))
                // best-fit by capacity; tie-break by roomId so selection is deterministic (HashMap order isn't)
                .min(Comparator.comparingInt(Room::getCapacity).thenComparing(Room::getRoomId))
                .orElseThrow(() -> new NoRoomAvailableException(
                        "need capacity " + request.getRequiredCapacity()
                                + " + amenities " + request.getRequiredAmenities()
                                + " for " + request.getStartTime() + "–" + request.getEndTime()));
    }

    public Booking getBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new BookingNotFoundException(bookingId);
        }
        return booking;
    }
    /*
    Checked — extend Exception (but not RuntimeException).
    The compiler forces you to throws or try/catch them.
    Unchecked — extend RuntimeException.
    The compiler imposes no such requirement; they propagate silently up the stack until something catches them (or the thread dies).*/
    public void cancelBooking(String bookingId) {
        Booking booking = getBooking(bookingId);   // throws BookingNotFoundException if unknown
        bookings.remove(bookingId);
        notifyParticipants(booking, new BookingEvent(booking.getId(), booking.getStartTime(), EventType.CANCELLED));
    }

    // Two intervals overlap when each starts before the other ends: startA < endB && startB < endA.
    // Map key (bookingId) doesn't help this query, so we still scan values by room + time.
    private boolean isRoomAvailable(String roomId, LocalDateTime start, LocalDateTime end) {
        return bookings.values().stream()
                .filter(b -> b.getRoomId().equals(roomId))
                .noneMatch(b -> start.isBefore(b.getEndTime()) && b.getStartTime().isBefore(end));
    }

    // BookingService owns Participant creation (a booking-specific role) not ParticipantService
    public Participant registerParticipant(User user) {
        Participant participant = new Participant(user);
        participants.put(user.getId(), participant);
        return participant;
    }

    public void addSystemObserver(BookingObserver observer) {
        systemObserver.add(observer);
    }

    // Notify every participant of this booking (resolved by id) plus all system observers.
    public void notifyParticipants(Booking booking, BookingEvent event) {
        if (booking.getParticipantsId() != null) {
            for (String participantId : booking.getParticipantsId()) {
                Participant p = participants.get(participantId);
                if (p != null) {
                    p.notifyBooking(event);
                }
            }
        }
        systemObserver.forEach(o -> o.notifyBooking(event));
    }
}

//Booking service class is (orchestration + the observer subject)

//Where will participant be stored? Separate user repo, inside booking service, inside booking?
/*
* Pros and cons of having room service
*Pros
*Instead of having rooms here, they can be in room service
* Separation of concerns, room features reusable at other places also, testability
* Cons
* Overkill for small system, no real need because here we just require isAvailable etc
* It will also require booking service to check for availability across rooms
* */
