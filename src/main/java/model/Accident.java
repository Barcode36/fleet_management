package main.java.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Accident {
    private int accidentId;
    private LocalDate date, dateReportedToInsurance;
    private LocalTime time;
    private String policeStationReported;
    private String place;
    private String description;

    public Accident() {

    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDateReportedToInsurance() {
        return dateReportedToInsurance;
    }

    public void setDateReportedToInsurance(LocalDate dateReportedToInsurance) {
        this.dateReportedToInsurance = dateReportedToInsurance;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getPoliceStationReported() {
        return policeStationReported;
    }

    public void setPoliceStationReported(String policeStationReported) {
        this.policeStationReported = policeStationReported;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getAccidentId() {
        return accidentId;
    }

    public void setAccidentId(int accidentId) {
        this.accidentId = accidentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
