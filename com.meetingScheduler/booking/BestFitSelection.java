package booking;

import room.Room;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BestFitSelection implements RoomSelectionStrategy {
    @Override
    public Optional<Room> select(List<Room> availableCandidates) {
        return availableCandidates.stream()
                .min(Comparator.comparingInt(Room::getCapacity).thenComparing(Room::getRoomId));
    }
}
