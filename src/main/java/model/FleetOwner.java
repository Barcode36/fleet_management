package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FleetOwner {
    private StringProperty nationalId, firstName, middleName, lastName, residence, occupation, phoneNumber;

    public FleetOwner() {
        this.phoneNumber = new SimpleStringProperty();
        this.nationalId = new SimpleStringProperty();
        this.firstName = new SimpleStringProperty();
        this.middleName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.residence = new SimpleStringProperty();
        this.occupation = new SimpleStringProperty();
    }

    public FleetOwner(String firstName, String middleName, String lastName) {
        this.firstName = new SimpleStringProperty(firstName);
        this.middleName = new SimpleStringProperty(middleName);
        this.lastName  = new SimpleStringProperty(lastName);
    }

    public String getNationalId() {
        return nationalId.get();
    }

    public StringProperty nationalIdProperty() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId.set(nationalId);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getMiddleName() {
        return middleName.get();
    }

    public StringProperty middleNameProperty() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName.set(middleName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getResidence() {
        return residence.get();
    }

    public StringProperty residenceProperty() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence.set(residence);
    }

    public String getOccupation() {
        return occupation.get();
    }

    public StringProperty occupationProperty() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation.set(occupation);
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getMiddleName() + " " + getLastName();
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }
}
