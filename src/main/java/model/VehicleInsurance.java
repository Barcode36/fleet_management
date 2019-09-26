package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class VehicleInsurance {
    private StringProperty companyName, policyNumber;
    private ObjectProperty<LocalDate> startDate;
    private ObjectProperty<LocalDate> expiryDate;
    private DoubleProperty premiumAmount;
    private InsurancePolicyType policyType;

    public VehicleInsurance() {
        this.companyName = new SimpleStringProperty();
        this.policyNumber = new SimpleStringProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.expiryDate = new SimpleObjectProperty<>();
        this.premiumAmount = new SimpleDoubleProperty();
    }

    public String getCompanyName() {
        return companyName.get();
    }

    public StringProperty companyNameProperty() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName.set(companyName);
    }


    public String getPolicyNumber() {
        return policyNumber.get();
    }

    public StringProperty policyNumberProperty() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber.set(policyNumber);
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }

    public LocalDate getExpiryDate() {
        return expiryDate.get();
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate.set(expiryDate);
    }

    public double getPremiumAmount() {
        return premiumAmount.get();
    }

    public DoubleProperty premiumAmountProperty() {
        return premiumAmount;
    }

    public void setPremiumAmount(double premiumAmount) {
        this.premiumAmount.set(premiumAmount);
    }

    public InsurancePolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(InsurancePolicyType policyType) {
        this.policyType = policyType;
    }
}
