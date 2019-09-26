package main.java.model;

public class Setting {
    private ScheduledAction action;
    private int duration;
    private TimeUnit timeUnit;

    public Setting() {

    }
    public Setting(ScheduledAction action, int duration, TimeUnit timeUnit) {
        this.action = action;
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public ScheduledAction getAction() {
        return action;
    }

    public void setAction(ScheduledAction action) {
        this.action = action;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
