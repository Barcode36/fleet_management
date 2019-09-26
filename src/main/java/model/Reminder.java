package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Reminder {
    private StringProperty regNumber, dueDate;
    private Category category;

    public Reminder() {
        this.regNumber = new SimpleStringProperty();
        this.dueDate = new SimpleStringProperty();
    }

    public String getRegNumber() {
        return regNumber.get();
    }

    public StringProperty regNumberProperty() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber.set(regNumber);
    }


    public String getDueDate() {
        return dueDate.get();
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate.set(dueDate);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    public StringProperty categoryProperty() {
        return new SimpleStringProperty(getCategory().toString());
    }

    public enum Category{
        ALL("All"),
        INSURANCE_RENEWAL("Insurance Renewal"),
        ANNUAL_INSPECTION_RENEWAL("Annual Inspection"),
        SERVICE_RENEWAL("Servicing"),
        SPEED_GOV_RENEWAL("Speed Governor Renewal"),
        LICENSE_RENEWAL("NTSA Operation License Renewal");

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
