package main.java.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static main.java.controller.IncomeExpenseController.*;

public class Dao {
    public static ObservableList<Vehicle> getVehicles() {
        ObservableList<Vehicle> list = FXCollections.observableArrayList();
        String sql = "select vehicles.*, vehicle_insurance.* from vehicles " +
                "inner join vehicle_insurance on vehicles.reg_number = vehicle_insurance.reg_number ";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Vehicle vehicle = getVehicleFromResultSet(resultSet);
                    list.add(vehicle);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        ;
        return list;
    }

    private static Vehicle getVehicleFromResultSet(ResultSet resultSet) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(resultSet.getString("owner_id"));
        vehicle.setCategory(FleetCategory.valueOf(resultSet.getString("category")));
        vehicle.setCost(resultSet.getDouble("cost"));
        vehicle.setPurchasedOnLoan(resultSet.getBoolean("bought_on_loan"));
        vehicle.setMake(resultSet.getString("make"));
        vehicle.setNumSeats(resultSet.getInt("num_seats"));
        vehicle.setModel(resultSet.getString("model"));
        vehicle.setRegistrationDate(resultSet.getObject("reg_date", LocalDate.class));
        vehicle.setType(VehicleType.valueOf(resultSet.getString("type")));
        if (resultSet.getObject("speed_gov_renewal_date") != null) {
            vehicle.setPreviousSpeedGovernorRenewal(resultSet.getObject("speed_gov_renewal_date", LocalDate.class));
        }
        if (resultSet.getObject("license_renewal_date") != null) {
            vehicle.setPreviousNTSALicenseRenewal(resultSet.getObject("license_renewal_date", LocalDate.class));
        }

        if (resultSet.getObject("servicing_date") != null) {
            vehicle.setServicingDate(resultSet.getObject("servicing_date", LocalDate.class));
        }

        if (resultSet.getObject("inspection_date") != null) {
            vehicle.setPreviousInspectionDate(resultSet.getObject("inspection_date", LocalDate.class));
        }
        vehicle.setRegistrationNum(resultSet.getString("reg_number"));

        VehicleInsurance insurance = getInsurance(resultSet);
        vehicle.setInsurance(insurance);
        return vehicle;
    }

    private static FleetOwner getFleetOwner(ResultSet resultSet) throws SQLException {
        FleetOwner fleetOwner = new FleetOwner();
        fleetOwner.setNationalId(resultSet.getString("national_id"));
        fleetOwner.setFirstName(resultSet.getString("first_name"));
        fleetOwner.setLastName(resultSet.getString("last_name"));
        fleetOwner.setOccupation(resultSet.getString("occupation"));
        fleetOwner.setResidence(resultSet.getString("residence"));
        fleetOwner.setMiddleName(resultSet.getString("middle_name"));
        return fleetOwner;
    }

    private static VehicleInsurance getInsurance(ResultSet resultSet) throws SQLException {
        VehicleInsurance insurance = new VehicleInsurance();
        insurance.setCompanyName(resultSet.getString("company"));
        insurance.setExpiryDate(resultSet.getObject("expiry_date", LocalDate.class));
        insurance.setStartDate(resultSet.getObject("start_date", LocalDate.class));
        insurance.setPolicyNumber(resultSet.getString("policy_number"));
        insurance.setPremiumAmount(resultSet.getDouble("premium_amount"));
        insurance.setPolicyType(InsurancePolicyType.valueOf(resultSet.getString("policy_type")));
        return insurance;
    }

    public static ObservableList<String> getVehicleModels() {
        ObservableList<String> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select distinct model from vehicles");
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    list.add(resultSet.getString("model"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<String> getInsuranceCompanies() {
        ObservableList<String> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select name from insurance_companies");
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    list.add(resultSet.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static User getUser(String userName) {
        ResultSet resultSet = DbUtil.executeQuery("select * from users where user_name = '" + userName + "'");
        try {
            if (resultSet != null && resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setCategory(User.Category.valueOf(resultSet.getString("category")));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObservableList<String> getRegNumbers() {
        ObservableList<String> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select reg_number from vehicles");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    list.add(resultSet.getString("reg_number"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Employee> getEmployees(String sql) {
        ObservableList<Employee>list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setCategory(Employee.Category.valueOf(resultSet.getString("category")));
                    employee.setFirstName(resultSet.getString("first_name"));
                    employee.setLastName(resultSet.getString("last_name"));
                    employee.setNationalId(resultSet.getString("national_id"));
                    employee.setLicenseExpiryDate(resultSet.getObject("license_expiry_date", LocalDate.class));
                    employee.setLicenseNo(resultSet.getString("license_number"));
                    employee.setLicenseIssueDate(resultSet.getObject("license_issue_date", LocalDate.class));
                    employee.setPhoneNumber(resultSet.getString("phone_number"));
                    employee.setResidence(resultSet.getString("residence"));
                    list.add(employee);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Employee> getConductors() {
        ObservableList<Employee> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select * from conductors");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setFirstName(resultSet.getString("first_name"));
                    employee.setNationalId(resultSet.getString("national_id"));
                    employee.setLastName(resultSet.getString("last_name"));
                    list.add(employee);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static Vehicle getVehicleDetails(String regNumber) {
        String sql = "select vehicles.*, vehicle_insurance.* from vehicles " +
                "inner join vehicle_insurance on vehicles.reg_number = vehicle_insurance.reg_number " +
                "where vehicles.reg_number = '" + regNumber + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);

        try {
            if (resultSet != null && resultSet.next()) {
                return getVehicleFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Loan getLoanDetails(String regNumber) {
        String sql = "select * from loans where reg_number = '" + regNumber + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                Loan loan = new Loan();
                loan.setRate(resultSet.getDouble("rate"));
                loan.setNumYears(resultSet.getInt("num_years"));
                loan.setInterestRateType(InterestRateType.valueOf(resultSet.getString("interest_type")));
                loan.setFirstPayment(resultSet.getObject("start_date", LocalDate.class));
                loan.setPrincipal(resultSet.getDouble("principal"));
                return loan;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Check if the driver is assigned to between (not including) the listed days */
    /* Returns a string message describing assignment if any, else null*/
    public static String getDriverStatus(LocalDate start, LocalDate end, String driverId) {
        String sql = "select reg_number, start_date, end_date from vehicle_hire " +
                "where (start_date > '" + start + "' and start_date < '" + end + "') " +
                "or (end_date > '" + start + "' and end_date < '" + end + "') " +
                "or (start_date < '" + start + "' and end_date > '" + end + "') " +
                " and driver_id = '" + driverId + "' limit 1";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return "Driver assigned to " + resultSet.getString("reg_number") + " between " + DateUtil.formatDateLong(resultSet.getObject("start_date", LocalDate.class)) + " and " + DateUtil.formatDateLong(resultSet.getObject("end_date", LocalDate.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check normal assignments
        sql = "select date, reg_number from vehicle_assignments where (date > '" + start + "' and date < '" + end + "') and driver_id = '" + driverId + "' limit 1";
        resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return "Driver assigned to " + resultSet.getString("reg_number") + " on " + DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //checks if the vehicle has been hired out between the given days
    //returns a string describing existing assignment, else null
    public static String getVehicleStatus(LocalDate start, LocalDate end, String regNumber) {
        String sql = "select reg_number, start_date, end_date, hiree from vehicle_hire " +
                "where (start_date > '" + start + "' and start_date < '" + end + "') " +
                "or (end_date > '" + start + "' and end_date < '" + end + "') " +
                "or (start_date <= '" + start + "' and end_date >= '" + end + "') " +
                "and reg_number = '" + regNumber + "' limit 1";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("reg_number") + " rented out to " + resultSet.getString("hiree") + " between " + DateUtil.formatDateLong(resultSet.getObject("start_date", LocalDate.class)) + " and " + DateUtil.formatDateLong(resultSet.getObject("end_date", LocalDate.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //normal assignments
        sql = "select date, reg_number, drivers.first_name, drivers.last_name " +
                "from vehicle_assignments " +
                "inner join drivers on vehicle_assignments.driver_id = drivers.national_id " +
                "where " +
                "date > '" + start + "' and date < '" + end + "' " +
                "and reg_number = '" + regNumber + "' limit 1";
        resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("reg_number") + " assigned to " + resultSet.getString("first_name") + " " + resultSet.getString("first_name") + " (Driver) on " + DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getConductorStatus(LocalDate date, String nationalId) {
        String sql = "select date, conductors.first_name, conductors.last_name, reg_number " +
                "from vehicle_assignments " +
                "inner join conductors on conductors.national_id = vehicle_assignments.conductor_id " +
                "where date = '" + date + "' " +
                "and conductor_id = '" + nationalId + "' limit 1";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString("first_name") + " " + resultSet.getString("last_name") + " assigned to vehicle " + resultSet.getString("reg_number") + " on " +
                        "" + DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObservableList<Setting> getSettings() {
        ObservableList<Setting> list = FXCollections.observableArrayList();
        String sql = "select * from settings";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Setting setting = new Setting();
                    setting.setAction(ScheduledAction.valueOf(resultSet.getString("setting")));
                    setting.setDuration(resultSet.getInt("duration"));
                    setting.setTimeUnit(TimeUnit.valueOf(resultSet.getString("time_unit")));
                    list.add(setting);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Reminder> getReminders() {
        ObservableList<Reminder> list = FXCollections.observableArrayList();
        for (ScheduledAction action : ScheduledAction.values()) {
            int numDays = getNumDays(action);
            LocalDate nextDate = LocalDate.now().plusDays(numDays);
            try {
                list.addAll(getRemindersForScheduledAction(action, nextDate));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static ObservableList<Reminder> getRemindersForScheduledAction(ScheduledAction action, LocalDate nextDate) throws SQLException {
        ObservableList<Reminder> list = FXCollections.observableArrayList();
        String sql = "select reg_number," + action.getSqlField() + " from scheduled_services where " + action.getSqlField() + " <= '" + nextDate + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Reminder reminder = new Reminder();
                reminder.setRegNumber(resultSet.getString("reg_number"));
                reminder.setDueDate(DateUtil.formatDate(resultSet.getObject(action.getSqlField(), LocalDate.class)));
                switch (action) {
                    case NTSA_LICENSE_RENEWAL:
                        reminder.setCategory(Reminder.Category.LICENSE_RENEWAL);
                        break;
                    case SPEED_GOV_RENEWAL:
                        reminder.setCategory(Reminder.Category.SPEED_GOV_RENEWAL);
                        break;
                    case INSPECTION:
                        reminder.setCategory(Reminder.Category.ANNUAL_INSPECTION_RENEWAL);
                        break;
                    case SERVICING:
                        reminder.setCategory(Reminder.Category.SERVICE_RENEWAL);
                        break;
                    case INSURANCE:
                        reminder.setCategory(Reminder.Category.INSURANCE_RENEWAL);
                        break;
                }
                list.add(reminder);
            }
        }
        return list;
    }

    private static int getNumDays(ScheduledAction action) {
        ResultSet resultSet = DbUtil.executeQuery("select setting, duration, time_unit from settings " +
                "where setting = '" + action.getSqlField() + "'");
        try {
            if (resultSet != null && resultSet.next()) {
                Setting setting = new Setting(action, resultSet.getInt("duration"), TimeUnit.valueOf(resultSet.getString("time_unit")));
                return setting.getTimeUnit().getmFactor() * setting.getDuration();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ObservableList<User> getUsers() {
        ObservableList<User> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select * from users where user_id != 1"); //all except root user
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setFirstName(resultSet.getString("first_name"));
                    user.setLastName(resultSet.getString("last_name"));
                    user.setUserName(resultSet.getString("user_name"));
                    user.setCategory(User.Category.valueOf(resultSet.getString("category")));
                    user.setUserId(resultSet.getInt("user_id"));
                    list.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<FleetOwner> getFleetOwners() {
        ObservableList<FleetOwner> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select * from fleet_owners");
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    FleetOwner fleetOwner = new FleetOwner();
                    fleetOwner.setPhoneNumber(resultSet.getString("phone_number"));
                    fleetOwner.setNationalId(resultSet.getString("national_id"));
                    fleetOwner.setFirstName(resultSet.getString("first_name"));
                    fleetOwner.setLastName(resultSet.getString("last_name"));
                    fleetOwner.setOccupation(resultSet.getString("occupation"));
                    fleetOwner.setResidence(resultSet.getString("residence"));
                    fleetOwner.setMiddleName(resultSet.getString("middle_name"));
                    list.add(fleetOwner);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ObservableList<UserPermission> getUserPermissions(int userId) {
        ObservableList<UserPermission> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select permission, allowed from user_permissions where user_id = " + userId);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    UserPermission userPermission = new UserPermission(Permission.valueOf(resultSet.getString("permission")), resultSet.getBoolean("allowed"));
                    list.add(userPermission);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static FleetOwner getFleetOwnerByNationalID(String nationalId) {
        String sql = "select * from fleet_owners where national_id = '" + nationalId + "'";
        try {
            ResultSet resultSet = DbUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                return getFleetOwner(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObservableList<Map<String, String>> getCashEntries(String sql, LedgerEntryType entry) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> data = new HashMap<>();
                data.put(RECORD_ID, resultSet.getString("entry_id"));
                data.put(REG_NUMBER, resultSet.getString("reg_number"));
                if (entry == LedgerEntryType.INCOME) {
                    data.put(CATEGORY, OperationCategory.valueOf(resultSet.getString("category")).toString());
                    data.put(AMOUNT, CurrencyUtil.formatCurrency(resultSet.getDouble("income")));
                    data.put(INCOME_DETAIL, resultSet.getString("details"));
                } else{
                    data.put(CATEGORY, ExpenseCategory.valueOf(resultSet.getString("category")).toString());
                    data.put(AMOUNT, CurrencyUtil.formatCurrency(resultSet.getDouble("expense")));
                    data.put(EXPENSE_DETAIL, resultSet.getString("details"));
                }
                data.put(DATE, DateUtil.formatDate(resultSet.getObject("date", LocalDate.class)));
                data.put(CONDUCTOR, resultSet.getString("conductors.first_name") + " " + resultSet.getString("conductors.last_name"));
                data.put(DRIVER, resultSet.getString("drivers.first_name") + " " + resultSet.getString("drivers.last_name"));
                data.put(CONDUCTOR_ID, resultSet.getString("conductors.national_id"));
                data.put(DRIVER_ID, resultSet.getString("drivers.national_id"));
                list.add(data);
            }
        }
        return list;
    }

    public static ObservableList<Document> getDocuments(String ownerId) {
        ObservableList<Document> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select id, width, height, name from documents " +
                "where owner_id = '" + ownerId + "'");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    list.add(new Document(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("width"), resultSet.getInt("height")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static Map<Permission, Boolean> getPermissionsMap(int userId) {
        Map<Permission, Boolean> permissionMap = new HashMap<>();
        ResultSet resultSet = DbUtil.executeQuery("select * from user_permissions where user_id = " + userId);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    permissionMap.put(Permission.valueOf(resultSet.getString("permission")), resultSet.getBoolean("allowed"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return permissionMap;
    }

    public static ObservableList<String> getVehicleBrands() {
        ObservableList<String> list = FXCollections.observableArrayList();
        ResultSet resultSet = DbUtil.executeQuery("select name from vehicle_brands");
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    list.add(resultSet.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
