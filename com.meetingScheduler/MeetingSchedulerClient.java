import booking.Booking;
import booking.BookingRequest;
import booking.BookingService;
import room.Amenity;
import room.Room;
import room.RoomService;
import shared.AppException;
import user.UserService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

// Composition root: builds the object graph (wiring) and drives a demo booking.
public class MeetingSchedulerClient {

    public static void main(String[] args) {
        System.out.println("Welcome to the Meeting Scheduler App!");

        // 1. Build RoomService and register rooms in it (RoomService owns rooms).
        //    The client requesting a booking doesn't need to know these rooms exist.
        RoomService roomService = new RoomService();
        roomService.addRoom(Room.builder("R1", 5).name("Orion").withWhiteboard().build());
        roomService.addRoom(Room.builder("R2", 10).name("Nebula").withProjector().withVideoConf().withAC().build());
        roomService.addRoom(Room.builder("R3", 3).name("Pulsar").withProjector().build());

        // 2. Build UserService (source of truth for users + identity) and register users.
        //    UserService assigns the id; the client never invents one.
        UserService userService = new UserService();
        String aliceId = userService.register("Alice", "alice@corp.com", "111");
        String bobId = userService.register("Bob", "bob@corp.com", "222");

        // 3. Inject RoomService into BookingService (one-directional dependency, no cycle).
        BookingService bookingService = new BookingService(roomService);

        // 4. Register participants by handing the resolved User to BookingService, which owns
        //    Participant creation (client can't `new Participant(...)` — package-private ctor).
        bookingService.registerParticipant(userService.findById(aliceId));
        bookingService.registerParticipant(userService.findById(bobId));

        // 5a. Case 1 — caller wants a SPECIFIC room (R1).
        BookingRequest specificReq = new BookingRequest(
                "R1",
                List.of(aliceId, bobId),
                LocalDateTime.of(2026, 7, 16, 10, 0),
                LocalDateTime.of(2026, 7, 16, 11, 0));
        Booking b1 = bookingService.createBooking(specificReq);
        System.out.println("Booking " + b1.getId() + " confirmed in specific room " + b1.getRoomId());

        // 5b. Case 3 — caller wants ANY room (roomId = null) that fits 2 people AND has a PROJECTOR.
        //     Candidates: R2 (cap 10) and R3 (cap 3). Best-fit picks R3 (smallest that still works).
        BookingRequest anyReq = new BookingRequest(
                null,
                List.of(aliceId, bobId),
                LocalDateTime.of(2026, 7, 16, 14, 0),
                LocalDateTime.of(2026, 7, 16, 15, 0),
                EnumSet.of(Amenity.PROJECTOR));
        Booking b2 = bookingService.createBooking(anyReq);
        Room picked = roomService.findById(b2.getRoomId());
        System.out.println("Booking " + b2.getId() + " confirmed in auto-selected room " + b2.getRoomId()
                + " -> " + picked.description() + " @ " + picked.hourlyCost() + "/hr");

        // ---- A few inline test cases exercising the validation rules ----

        // TEST 1: same participant, overlapping time, different room -> PARTICIPANT_UNAVAILABLE.
        //         Alice is in b2 (14:00-15:00); try to also put her in R1 at 14:30.
        try {
            bookingService.createBooking(new BookingRequest(
                    "R1", List.of(aliceId),
                    LocalDateTime.of(2026, 7, 16, 14, 30),
                    LocalDateTime.of(2026, 7, 16, 15, 30)));
            System.out.println("TEST1 FAIL: expected participant clash to be rejected");
        } catch (AppException e) {
            System.out.println("TEST1 OK: rejected double-booked participant (" + e.getErrorCode() + ")");
        }

        // TEST 2: empty participant list -> NO_PARTICIPANTS.
        try {
            bookingService.createBooking(new BookingRequest(
                    "R1", List.of(),
                    LocalDateTime.of(2026, 7, 16, 16, 0),
                    LocalDateTime.of(2026, 7, 16, 17, 0)));
            System.out.println("TEST2 FAIL: expected empty participants to be rejected");
        } catch (AppException e) {
            System.out.println("TEST2 OK: rejected empty participants (" + e.getErrorCode() + ")");
        }

        // TEST 3: outside operating hours (before 10:00) -> OUTSIDE_OPERATING_HOURS.
        try {
            bookingService.createBooking(new BookingRequest(
                    "R1", List.of(aliceId),
                    LocalDateTime.of(2026, 7, 16, 8, 0),
                    LocalDateTime.of(2026, 7, 16, 9, 0)));
            System.out.println("TEST3 FAIL: expected out-of-hours booking to be rejected");
        } catch (AppException e) {
            System.out.println("TEST3 OK: rejected out-of-hours booking (" + e.getErrorCode() + ")");
        }
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
