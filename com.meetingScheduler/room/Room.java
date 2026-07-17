package room; //NOT com.meetingScheduler.room

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Room {

    private static final int BASE_HOURLY_COST = 2000; // room rent before amenities

    private final String roomId;
    private final String roomName;
    private final int capacity;
    private final Set<Amenity> amenities;

    // Private: the only way to build a Room is through the Builder (always-valid + labeled args).
    private Room(Builder b) {
        this.roomId = b.roomId;
        this.roomName = b.roomName;
        this.capacity = b.capacity;
        this.amenities = b.amenities.isEmpty()
                ? EnumSet.noneOf(Amenity.class)
                : EnumSet.copyOf(b.amenities);
    }

    // Mandatory fields (id + capacity) are required to even start building.
    public static Builder builder(String roomId, int capacity) {
        return new Builder(roomId, capacity);
    }

    public boolean canFit(int people) { return people <= capacity; }

    // True only if this room has every amenity the caller requires (superset check).
    public boolean hasAll(Set<Amenity> required) {
        return required == null || amenities.containsAll(required);
    }

    // Cost = base + sum of amenity costs (the "fold over the set" — no Decorator needed).
    public int hourlyCost() {
        return BASE_HOURLY_COST + amenities.stream().mapToInt(Amenity::getHourlyCost).sum();
    }

    public String description() {
        String base = roomName + " (cap " + capacity + ")";
        if (amenities.isEmpty()) {
            return base;
        }
        String feats = amenities.stream().map(Amenity::getLabel).collect(Collectors.joining(", "));
        return base + " with " + feats;
    }
//----------------------------------------------------------------
    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    // Unmodifiable view: callers can read but not mutate the room's amenities (keeps Room immutable).
    public Set<Amenity> getAmenities() {
        return java.util.Collections.unmodifiableSet(amenities);
    }

    // ---- Builder: mandatory args in the constructor, optionals as withX() methods ----
    public static class Builder {
        private final String roomId;   // mandatory
        private final int capacity;    // mandatory
        private String roomName = "";  // optional
        private final Set<Amenity> amenities = EnumSet.noneOf(Amenity.class);

        private Builder(String roomId, int capacity) {
            this.roomId = roomId;
            this.capacity = capacity;
        }

        public Builder name(String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Builder withAmenity(Amenity amenity) {
            this.amenities.add(amenity);
            return this;
        }

        // convenience wrappers so calls read like `withAC().withProjector()`
        public Builder withAC() { return withAmenity(Amenity.AC); }
        public Builder withProjector() { return withAmenity(Amenity.PROJECTOR); }
        public Builder withWhiteboard() { return withAmenity(Amenity.WHITEBOARD); }
        public Builder withVideoConf() { return withAmenity(Amenity.VIDEO_CONF); }
        public Builder withPhone() { return withAmenity(Amenity.PHONE); }

        // Single validation point -> the Room can never exist in an invalid state.
        public Room build() {
            if (roomId == null || roomId.isBlank()) {
                throw new IllegalArgumentException("Room id is required");
            }
            if (capacity <= 0) {
                throw new IllegalArgumentException("Room capacity must be positive: " + capacity);
            }
            return new Room(this);
        }
    }
}
