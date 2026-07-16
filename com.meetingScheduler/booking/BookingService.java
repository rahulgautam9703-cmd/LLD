package booking; //NOTICE FIRST LINE ALWAYS

import room.Room;
import room.RoomCapacityExceededException;
import room.RoomService;
import shared.AppException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingService {

    private final List<BookingObserver> systemObserver = new ArrayList<>(); //Optional Non-participant observers

    // Participant is a booking-specific role (a User wrapped as an observer for a meeting),
    // so its registry legitimately lives in this bounded context.
    private final Map<String, Participant> participants = new HashMap<>();

  /*booking service is becoming a god class here
    because instead of room service/Participant service it is having the store of these directly in it
    Note that User can have a user service which has a list but participant is a special user just for service booking so it can be kept here,
    room can have a separate room service which will have a list of room so right now we are creating room and adding it here we could have added it there and had a roomService injected in booking service
    Ideally Booking service should just have list of bookings*/

    private final List<Booking> bookings = new ArrayList<>();
    private final RoomService roomService; // private final List<Room> rooms = new ArrayList<>();

    public BookingService(RoomService roomService) {
        this.roomService = roomService;
    }

    /*2 cases 1 in which we just require any random room
     * Case 2 we just want a particular room and check if that is available*/
    public Booking createBooking(Booking booking) {
        Room room = roomService.findById(booking.getRoomId());

        int attendees = booking.getParticipantsId() == null ? 0 : booking.getParticipantsId().size();
        if (!room.canFit(attendees)) {
            throw new RoomCapacityExceededException(room.getRoomId(), attendees);
        }

        // Availability check lives HERE (not in RoomService): bookings are owned by this
        // service, so no BookingService <-> RoomService cycle is needed.
        if (!isRoomAvailable(booking.getRoomId(), booking.getStartTime(), booking.getEndTime())) {
            throw new AppException(
                    "Room " + booking.getRoomId() + " is already booked for the requested time",
                    "ROOM_NOT_AVAILABLE");
        }

        bookings.add(booking);
        notifyParticipants(booking, new BookingEvent(booking.getId(), booking.getStartTime(), EventType.CONFIRMED));
        return booking;
    }

    // Two intervals overlap when each starts before the other ends: startA < endB && startB < endA.
    private boolean isRoomAvailable(String roomId, LocalDateTime start, LocalDateTime end) {
        return bookings.stream()
                .filter(b -> b.getRoomId().equals(roomId))
                .noneMatch(b -> start.isBefore(b.getEndTime()) && b.getStartTime().isBefore(end));
    }

    // Register a participant once (wrapping a User); bookings then reference it by id.
    public void registerParticipant(String participantId, Participant participant) {
        participants.put(participantId, participant);
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
