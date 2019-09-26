package main.java.model;

public enum ExpenseCategory {
    FUEL("Fuel"),
    NORMAL_SERVICE("Normal service"),
    INSURANCE("Insurance"),
    COUNTY_OPERATING_LICENSE("County operating license"),
    NTSA_OPERATING_LICENSE("NTSA operating license"),
    SHORT_TERM_OPERATING_LICENSE("Short term operating license"),
    INSPECTION_COST("Inspection cost"),
    ADVANCE_TAX("Advance Tax"),
    NEW_TIRES("New Tires"),
    ACCIDENT_REPAIRS("Accident Repairs"),
    ROUTE_OPERATION_EXPENSES("Route operation expenses"),
    EMPLOYEE_WAGES("Driver and conductor wages"),
    LOAN_INTEREST("Loan interest"),
    PRINCIPAL_LOAN_REPAYMENT("Principal loan repayment"),
    LEGAL_EXPENSES_AND_FINES("Legal expenses & fines"),
    OTHER("Others")
    ;
    private String category;
    ExpenseCategory(String category){
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}
