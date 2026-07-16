import booking.Booking;
import booking.BookingService;
import booking.Participant;
import room.Room;
import room.RoomService;
import user.User;

import user.UserService;

import java.time.LocalDateTime;
import java.util.List;

// Composition root: builds the object graph (wiring) and drives a demo booking.
public class MeetingSchedulerClient {

    public static void main(String[] args) {
        System.out.println("Welcome to the Meeting Scheduler App!");

        // 1. Build RoomService and register rooms in it (RoomService owns rooms).
        RoomService roomService = new RoomService();

        Room r1 = new Room("R1", "Orion", 5);   // always-valid, immutable
        roomService.addRoom(r1);

        // 2. Build UserService (source of truth for users + identity) and register users.
        //    UserService assigns the id; the client never invents one.
        UserService userService = new UserService();
        User aliceUser = userService.register("Alice", "alice@corp.com", "111");
        User bobUser = userService.register("Bob", "bob@corp.com", "222");

        // 3. Inject RoomService into BookingService (one-directional dependency, no cycle).
        BookingService bookingService = new BookingService(roomService);

        // 4. Wrap resolved Users as Participants (booking observers) and register them BY user id.
        //    Note: Participant holds the User OBJECT (in-memory composition), not just an id.
        Participant alice = new Participant(aliceUser);
        Participant bob = new Participant(bobUser);
        bookingService.registerParticipant(aliceUser.getId(), alice);
        bookingService.registerParticipant(bobUser.getId(), bob);

        // 5. Build a Booking that references room + participants BY ID (the real user ids now).
        Booking booking = new Booking();
        booking.setId("B1");
        booking.setRoomId("R1");
        booking.setParticipantsId(List.of(aliceUser.getId(), bobUser.getId()));
        booking.setStartTime(LocalDateTime.of(2026, 7, 16, 10, 0));
        booking.setEndTime(LocalDateTime.of(2026, 7, 16, 11, 0));

        // 6. Orchestrate: capacity + availability checks, store, notify observers.
        bookingService.createBooking(booking);
        System.out.println("Booking " + booking.getId() + " confirmed in room " + booking.getRoomId());
    }
    /*
    * Incase when cyclic dependency when booking service needs room service and vice versa — how Java handles it
Constructor injection cannot build a cycle with final fields (A needs B in its constructor, B needs A in its constructor — deadlock; you can't construct either first). The mechanical fix is setter injection, which breaks the cycle in two steps:
RoomService roomService = new RoomService();       // 1. construct both, no args
BookingService bookingService = new BookingService();
roomService.setBookingService(bookingService);     // 2. wire them after
bookingService.setRoomService(roomService);
This works but costs you: fields can't be final, objects exist half-initialized between steps, and it advertises the design smell.*/
}
