package main.java.model;

public enum VehicleType {
    MINI_BUSES("Mini Buses"),
    MINI_VANS("Mini Vans"),
    LORRIES("Lorries");

    private String type;
    VehicleType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
