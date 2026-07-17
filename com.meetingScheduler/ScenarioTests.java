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

// Throwaway scenario harness: exercises happy paths + edge/error cases and prints PASS/FAIL.
public class ScenarioTests {

    static int pass = 0, fail = 0;

    // A fresh, fully-wired context per scenario so state doesn't leak between tests.
    static class Ctx {
        RoomService roomService = new RoomService();
        UserService userService = new UserService();
        BookingService bookingService = new BookingService(roomService);
        String[] u = new String[6];

        Ctx() {
            roomService.addRoom(Room.builder("R1", 5).name("Orion").withWhiteboard().build());
            roomService.addRoom(Room.builder("R2", 10).name("Nebula").withProjector().withVideoConf().withAC().build());
            roomService.addRoom(Room.builder("R3", 3).name("Pulsar").withProjector().build());
            for (int i = 1; i <= 5; i++) {
                u[i] = userService.register("User" + i, "user" + i + "@corp.com", "9000" + i);
                bookingService.registerParticipant(userService.findById(u[i]));
            }
        }
    }

    static LocalDateTime t(int h, int m) { return LocalDateTime.of(2026, 7, 16, h, m); }

    static void expectOk(String name, Runnable r) {
        try { r.run(); pass++; System.out.println("PASS  " + name); }
        catch (Exception e) { fail++; System.out.println("FAIL  " + name + " -> unexpected " + e); }
    }

    static void expectError(String name, String code, Runnable r) {
        try { r.run(); fail++; System.out.println("FAIL  " + name + " -> expected " + code + " but succeeded"); }
        catch (AppException e) {
            if (code.equals(e.getErrorCode())) { pass++; System.out.println("PASS  " + name + " (" + code + ")"); }
            else { fail++; System.out.println("FAIL  " + name + " -> got " + e.getErrorCode() + " want " + code); }
        }
        catch (Exception e) { fail++; System.out.println("FAIL  " + name + " -> " + e); }
    }

    public static void main(String[] args) {
        // 1. specific room, happy
        expectOk("specific room booking", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1], c.u[2]), t(10, 0), t(11, 0)));
        });

        // 2. any room + amenity, best-fit picks smallest (R3 cap3 over R2 cap10)
        expectOk("any-room best-fit (expects R3)", () -> {
            Ctx c = new Ctx();
            Booking b = c.bookingService.createBooking(
                    new BookingRequest(null, List.of(c.u[1], c.u[2]), t(14, 0), t(15, 0), EnumSet.of(Amenity.PROJECTOR)));
            if (!b.getRoomId().equals("R3")) throw new RuntimeException("picked " + b.getRoomId());
        });

        // 3. any room + projector but capacity 5 -> only R2 fits (R3 too small)
        expectOk("any-room capacity forces R2", () -> {
            Ctx c = new Ctx();
            Booking b = c.bookingService.createBooking(
                    new BookingRequest(null, List.of(c.u[1], c.u[2], c.u[3], c.u[4], c.u[5]), t(14, 0), t(15, 0), EnumSet.of(Amenity.PROJECTOR)));
            if (!b.getRoomId().equals("R2")) throw new RuntimeException("picked " + b.getRoomId());
        });

        // 4. capacity exceeded on specific room (R3 cap3, 5 people)
        expectError("capacity exceeded", "ROOM_CAPACITY_EXCEEDED", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R3", List.of(c.u[1], c.u[2], c.u[3], c.u[4], c.u[5]), t(10, 0), t(11, 0)));
        });

        // 5. room not found
        expectError("room not found", "ROOM_NOT_FOUND", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R99", List.of(c.u[1]), t(10, 0), t(11, 0)));
        });

        // 6. double-booking overlap on same room
        expectError("overlap double-booking", "ROOM_NOT_AVAILABLE", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(10, 0), t(11, 0)));
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[2]), t(10, 30), t(11, 30)));
        });

        // 7. adjacent (non-overlapping) same room -> both OK
        expectOk("adjacent slots same room", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(10, 0), t(11, 0)));
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[2]), t(11, 0), t(12, 0)));
        });

        // 8. any-room but no room has the amenity (PHONE not on any room)
        expectError("no room with amenity", "NO_ROOM_AVAILABLE", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest(null, List.of(c.u[1]), t(14, 0), t(15, 0), EnumSet.of(Amenity.PHONE)));
        });

        // 9. any-room but all projector rooms already booked for that slot
        expectError("all matching rooms booked", "NO_ROOM_AVAILABLE", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R2", List.of(c.u[1]), t(14, 0), t(15, 0)));
            c.bookingService.createBooking(new BookingRequest("R3", List.of(c.u[2]), t(14, 0), t(15, 0)));
            c.bookingService.createBooking(new BookingRequest(null, List.of(c.u[3]), t(14, 0), t(15, 0), EnumSet.of(Amenity.PROJECTOR)));
        });

        // 10. before opening hours
        expectError("before open hours", "OUTSIDE_OPERATING_HOURS", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(9, 0), t(10, 0)));
        });

        // 11. after closing hours
        expectError("after close hours", "OUTSIDE_OPERATING_HOURS", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(21, 30), t(22, 30)));
        });

        // 12. start >= end
        expectError("start not before end", "INVALID_TIME_WINDOW", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(11, 0), t(11, 0)));
        });

        // 13. cross-day window
        expectError("cross-day window", "INVALID_TIME_WINDOW", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]),
                    LocalDateTime.of(2026, 7, 16, 21, 0), LocalDateTime.of(2026, 7, 17, 11, 0)));
        });

        // 14. cancel then rebook same slot
        expectOk("cancel frees the slot", () -> {
            Ctx c = new Ctx();
            Booking b = c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(10, 0), t(11, 0)));
            c.bookingService.cancelBooking(b.getId());
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[2]), t(10, 0), t(11, 0)));
        });

        // 15. get unknown booking
        expectError("get unknown booking", "BOOKING_NOT_FOUND", () -> {
            Ctx c = new Ctx();
            c.bookingService.getBooking("B999");
        });

        // 16. duplicate email
        expectError("duplicate email register", "USER_ALREADY_EXISTS", () -> {
            Ctx c = new Ctx();
            c.userService.register("Dup", "user1@corp.com", "111");
        });

        // 17. zero participants now rejected
        expectError("zero participants rejected", "NO_PARTICIPANTS", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(), t(10, 0), t(11, 0)));
        });

        // 18. same participant, overlapping time, different room -> rejected
        expectError("participant double-booked", "PARTICIPANT_UNAVAILABLE", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(10, 0), t(11, 0)));
            c.bookingService.createBooking(new BookingRequest("R2", List.of(c.u[1]), t(10, 30), t(11, 30)));
        });

        // 19. same participant, non-overlapping times -> allowed
        expectOk("participant back-to-back different rooms", () -> {
            Ctx c = new Ctx();
            c.bookingService.createBooking(new BookingRequest("R1", List.of(c.u[1]), t(10, 0), t(11, 0)));
            c.bookingService.createBooking(new BookingRequest("R2", List.of(c.u[1]), t(11, 0), t(12, 0)));
        });

        System.out.println("\n== " + pass + " passed, " + fail + " failed ==");
    }
}
