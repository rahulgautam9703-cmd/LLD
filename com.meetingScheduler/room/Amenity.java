package room;

// Each amenity carries its own data (cost + label). Room sums these instead of a Decorator chain
// (data-you-query -> model as data; behavior-you-compose -> Decorator).
public enum Amenity {
    PROJECTOR(500, "Projector"),
    WHITEBOARD(100, "Whiteboard"),
    VIDEO_CONF(800, "Video conferencing"),
    PHONE(50, "Conference phone"),
    AC(1000, "Air conditioning");

    private final int hourlyCost; // in rupees; int is fine for a demo (use BigDecimal for real money)
    private final String label;

    Amenity(int hourlyCost, String label) {
        this.hourlyCost = hourlyCost;
        this.label = label;
    }

    public int getHourlyCost() {
        return hourlyCost;
    }

    public String getLabel() {
        return label;
    }
}
