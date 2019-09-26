package main.java.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class Employee {
    private StringProperty nationalId, firstName, lastName, licenseNo, phoneNumber, residence;
    private ObjectProperty<LocalDate> licenseIssueDate, licenseExpiryDate;
    private Category category;
    public Employee() {
        this.nationalId = new SimpleStringProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.licenseNo = new SimpleStringProperty();
        this.licenseExpiryDate = new SimpleObjectProperty<>();
        this.licenseIssueDate = new SimpleObjectProperty<>();
        this.phoneNumber = new SimpleStringProperty();
        this.residence = new SimpleStringProperty();
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

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getFirstName()).append(" ");
        if (getLastName() != null) {
            builder.append(getLastName());
        }
        return builder.toString();
    }

    public String getLicenseNo() {
        return licenseNo.get();
    }

    public StringProperty licenseNoProperty() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo.set(licenseNo);
    }

    public LocalDate getLicenseIssueDate() {
        return licenseIssueDate.get();
    }

    public ObjectProperty<LocalDate> licenseIssueDateProperty() {
        return licenseIssueDate;
    }

    public void setLicenseIssueDate(LocalDate licenseIssueDate) {
        this.licenseIssueDate.set(licenseIssueDate);
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate.get();
    }

    public ObjectProperty<LocalDate> licenseExpiryDateProperty() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) {
        this.licenseExpiryDate.set(licenseExpiryDate);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public String getResidence() {
        return residence.get();
    }

    public StringProperty residenceProperty() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence.set(residence);
    }

    public enum Category {
        DRIVER("Driver"), CONDUCTOR("Conductor");
        private String category;

        Category(String category) {
            this.category = category;
        }

        @Override
        public String toString() {
            return category;
        }
    }
}
