package main.java.model;

public enum TimeUnit {
    DAYS(1), WEEKS(7), MONTHS(30);

    private int mFactor;
    TimeUnit(int mFactor) {
        this.mFactor = mFactor;
    }
    public int getmFactor() {
        return mFactor;
    }
}
