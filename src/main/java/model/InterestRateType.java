package main.java.model;

public enum InterestRateType {
    FIXED("Fixed"),
    REDUCING_BALANCE("Reducing balance");

    ;
    private String rate;
    InterestRateType(String rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return rate;
    }
}
