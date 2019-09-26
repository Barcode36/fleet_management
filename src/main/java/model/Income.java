package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Income extends LedgerEntry {

    private OperationCategory operationCategory;

    public Income() {

    }

    public OperationCategory getOperationCategory() {
        return operationCategory;
    }

    public void setOperationCategory(OperationCategory operationCategory) {
        this.operationCategory = operationCategory;
    }

    public StringProperty operationCategoryProperty() {
        return new SimpleStringProperty(getOperationCategory().toString());
    }

}
