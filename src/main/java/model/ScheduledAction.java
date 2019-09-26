package main.java.model;

public enum ScheduledAction {
    SPEED_GOV_RENEWAL("speed_gov_renewal"),
    NTSA_LICENSE_RENEWAL("ntsa_license_renewal"),
    INSPECTION("inspection"),
    INSURANCE("insurance"),
    SERVICING("servicing");

    private String sqlField;
    ScheduledAction(String sqlField) {
        this.sqlField = sqlField;
    }
    public String getSqlField() {
        return sqlField;
    }

}
