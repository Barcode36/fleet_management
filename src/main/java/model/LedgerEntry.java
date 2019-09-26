package main.java.model;

import javafx.beans.property.*;
import main.java.model.*;
import main.java.util.*;

import java.time.LocalDate;

public class LedgerEntry {
    private IntegerProperty id;
    private StringProperty details;
    private DoubleProperty income, expense;
    private ObjectProperty<LocalDate> dateCreated;

    public LedgerEntry() {
        this.id = new SimpleIntegerProperty(0);
        this.details = new SimpleStringProperty();
        this.income = new SimpleDoubleProperty(0);
        this.dateCreated = new SimpleObjectProperty<>();
        this.expense = new SimpleDoubleProperty(0);

    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getDetails() {
        return details.get();
    }

    public StringProperty detailsProperty() {
        return details;
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public double getIncome() {
        return income.get();
    }

    public DoubleProperty incomeProperty() {
        return income;
    }

    public void setIncome(double income) {
        this.income.set(income);
    }

    public double getExpense() {
        return expense.get();
    }

    public DoubleProperty expenseProperty() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense.set(expense);
    }
}
