package main.java.model;

public enum Permission {
    EDIT_VEHICLES("Register new vehicles and edit vehicle details"),
    VIEW_VEHICLE_DETAILS("View vehicle information"),
    UPLOAD_DOCUMENTS("Upload documents"),
    RECORD_INCOME("Record income/expenses"),
    DELETE_INCOME("Delete income/expense records"),
    EDIT_INCOME("Edit income records"),
    RECORD_ACCIDENT("Record accident"),
    MODIFY_ACCIDENT("Modify accident records"),
    VIEW_REMINDERS("View reminders"),
    MODIFY_SETTINGS("Modify reminders settings"),
    DELETE_INSURANCE("Delete insurance company"),
    MODIFY_INSURANCE("Modify vehicle's insurance details"),
    RECORD_RENEWAL_DATES("Record license renewal / speed governor renewal/ annual inspection / servicing"),
    ASSIGN_EMPLOYEES("Assign vehicles to employees"),
    REGISTER_EMPLOYEES("Register drivers and conductors"),
    EDIT_EMPLOYEES("Edit or delete drivers and conductors"),
    REGISTER_FLEET_OWNERS("Register fleet owners"),
    VIEW_FLEET_OWNER_DOCUMENTS("View fleet owners' documents"),
    EDIT_FLEET_OWNERS("Edit or delete fleet owners"),
    CREATE_USERS("Create, update and delete system users"),
    CHANGE_USER_PERMISSIONS("Set or change user permissions"),
    VIEW_LOAN_AMORTIZATION("View loan amortization"),
    VIEW_USER_ACTIVITY("View other users' activity");

    //todo : add more permissions;
    private String permission;
    Permission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return permission;
    }
}
