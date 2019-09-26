package main.java.model;

import java.time.LocalDate;

public class VehicleHire {
    private String regNumber, driverId, hiree;
    private LocalDate startDate, endDate;

    public VehicleHire() {

    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getHiree() {
        return hiree;
    }

    public void setHiree(String hiree) {
        this.hiree = hiree;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
