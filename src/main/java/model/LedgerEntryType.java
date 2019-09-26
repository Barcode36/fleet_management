package main.java.model;

import main.java.model.*;
import main.java.util.*;

public enum LedgerEntryType {

    INCOME("Income"), EXPENSE("Expenses");
    private String type;
    LedgerEntryType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
