package main.java.model;

public enum InsurancePolicyType {
    COMPREHENSIVE_INSURANCE("Comprehensive insurance"),
    THIRD_PARTY("Third party insurance"),
    ;
    private String type;
    InsurancePolicyType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
