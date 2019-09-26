package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Vehicle {
    private StringProperty make, model, registrationNum, ownerId;
    private ObjectProperty<LocalDate> previousInspectionDate, previousSpeedGovernorRenewal, previousNTSALicenseRenewal, registrationDate, servicingDate;
    private FleetCategory category;
    private VehicleType type;
    private IntegerProperty numSeats;
    private DoubleProperty cost;
    private VehicleInsurance insurance;
    private boolean purchasedOnLoan;


    public Vehicle() {
        this.make = new SimpleStringProperty();
        this.ownerId = new SimpleStringProperty();
        this.model = new SimpleStringProperty();
        this.registrationNum = new SimpleStringProperty();
        this.previousInspectionDate = new SimpleObjectProperty<>();
        this.previousNTSALicenseRenewal = new SimpleObjectProperty<>();
        this.previousSpeedGovernorRenewal = new SimpleObjectProperty<>();
        this.numSeats = new SimpleIntegerProperty();
        this.registrationDate = new SimpleObjectProperty<>();
        this.servicingDate = new SimpleObjectProperty<>();
        this.cost = new SimpleDoubleProperty();
    }

    public String getModel() {
        return model.get();
    }

    public StringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getRegistrationNum() {
        return registrationNum.get();
    }

    public StringProperty registrationNumProperty() {
        return registrationNum;
    }

    public void setRegistrationNum(String registrationNum) {
        this.registrationNum.set(registrationNum);
    }

    public LocalDate getPreviousInspectionDate() {
        return previousInspectionDate.get();
    }

    public ObjectProperty<LocalDate> previousInspectionDateProperty() {
        return previousInspectionDate;
    }

    public void setPreviousInspectionDate(LocalDate previousInspectionDate) {
        this.previousInspectionDate.set(previousInspectionDate);
    }

    public LocalDate getPreviousSpeedGovernorRenewal() {
        return previousSpeedGovernorRenewal.get();
    }

    public ObjectProperty<LocalDate> previousSpeedGovernorRenewalProperty() {
        return previousSpeedGovernorRenewal;
    }

    public void setPreviousSpeedGovernorRenewal(LocalDate previousSpeedGovernorRenewal) {
        this.previousSpeedGovernorRenewal.set(previousSpeedGovernorRenewal);
    }

    public LocalDate getPreviousNTSALicenseRenewal() {
        return previousNTSALicenseRenewal.get();
    }

    public ObjectProperty<LocalDate> previousNTSALicenseRenewalProperty() {
        return previousNTSALicenseRenewal;
    }

    public void setPreviousNTSALicenseRenewal(LocalDate previousNTSALicenseRenewal) {
        this.previousNTSALicenseRenewal.set(previousNTSALicenseRenewal);
    }

    public FleetCategory getCategory() {
        return category;
    }

    public void setCategory(FleetCategory category) {
        this.category = category;
    }

    public StringProperty categoryProperty() {
        if (getCategory() != null) {
            return new SimpleStringProperty(getCategory().toString());
        }
        return null;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public StringProperty typeProperty() {
        if (getType() != null) {
            return new SimpleStringProperty(getType().toString());
        }
        return null;
    }


    public int getNumSeats() {
        return numSeats.get();
    }

    public IntegerProperty numSeatsProperty() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats.set(numSeats);
    }

    public double getCost() {
        return cost.get();
    }

    public DoubleProperty costProperty() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost.set(cost);
    }

    public VehicleInsurance getInsurance() {
        return insurance;
    }

    public void setInsurance(VehicleInsurance insurance) {
        this.insurance = insurance;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate.get();
    }

    public ObjectProperty<LocalDate> registrationDateProperty() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate.set(registrationDate);
    }

    public boolean isPurchasedOnLoan() {
        return purchasedOnLoan;
    }

    public void setPurchasedOnLoan(boolean purchasedOnLoan) {
        this.purchasedOnLoan = purchasedOnLoan;
    }

    public LocalDate getServicingDate() {
        return servicingDate.get();
    }

    public ObjectProperty<LocalDate> servicingDateProperty() {
        return servicingDate;
    }

    public void setServicingDate(LocalDate servicingDate) {
        this.servicingDate.set(servicingDate);
    }


    public String getOwnerId() {
        return ownerId.get();
    }

    public StringProperty ownerIdProperty() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId.set(ownerId);
    }

    public String getMake() {
        return make.get();
    }

    public StringProperty makeProperty() {
        return make;
    }

    public void setMake(String make) {
        this.make.set(make);
    }
}
