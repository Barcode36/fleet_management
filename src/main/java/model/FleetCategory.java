package main.java.model;

public enum FleetCategory {
    PARCEL_CARRIERS("Parcel Carriers"),
    PASSENGER_MINI_BUSES("Passenger Mini Buses");

    private String category;
    FleetCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}
