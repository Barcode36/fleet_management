package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Expense extends LedgerEntry {
    private ExpenseCategory category;

    public Expense() {

    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public StringProperty categoryProperty() {
        return new SimpleStringProperty(getCategory().toString());
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

}
