package main.java.util;

import javafx.collections.ObservableList;
import main.Main;
import main.java.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

public class DbUtil {
    private static Connection connection;
    private static final String DATABASE = "fleet_db";

    public static boolean getConnection(String userName, String password) {
        String url = "jdbc:mysql://localhost:3306/";
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);
   
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, properties);
            if (connection != null) {
                String sql = "use " + DATABASE;
                return executeStatement(sql);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
           
        } 
        return false;
    }

    public static ResultSet executeQuery(String sql) {
        ResultSet resultSet;
        Statement statement;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean executeStatement(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveVehicleDetails(Vehicle vehicle) {
        String sql = "insert into vehicles values(?,?,?,?,?,?,?,?,?,?,?,?, ? ,?) on duplicate key update " +
                "model = ?, inspection_date = ?, speed_gov_renewal_date = ?, license_renewal_date = ?, reg_date = ?, category = ?, type = ?, make = ?, num_seats = ?, cost = ?, bought_on_loan = ?, servicing_date = ?, owner_id = ? ";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, vehicle.getRegistrationNum());
            statement.setString(2, vehicle.getModel());
            statement.setObject(3, vehicle.getPreviousInspectionDate());
            statement.setObject(4, vehicle.getPreviousSpeedGovernorRenewal());
            statement.setObject(5, vehicle.getPreviousNTSALicenseRenewal());
            statement.setObject(6, vehicle.getRegistrationDate());
            statement.setString(7, vehicle.getCategory().name());
            statement.setString(8, vehicle.getType().name());
            statement.setString(9, vehicle.getMake());
            statement.setInt(10, vehicle.getNumSeats());
            statement.setDouble(11, vehicle.getCost());
            statement.setBoolean(12, vehicle.isPurchasedOnLoan());
            statement.setObject(13, vehicle.getServicingDate());
            statement.setString(14, vehicle.getOwnerId());
            statement.setString(15, vehicle.getModel());
            statement.setObject(16, vehicle.getPreviousInspectionDate());
            statement.setObject(17, vehicle.getPreviousSpeedGovernorRenewal());
            statement.setObject(18, vehicle.getPreviousNTSALicenseRenewal());
            statement.setObject(19, vehicle.getRegistrationDate());
            statement.setString(20, vehicle.getCategory().name());
            statement.setString(21, vehicle.getType().name());
            statement.setString(22, vehicle.getMake());
            statement.setInt(23, vehicle.getNumSeats());
            statement.setDouble(24, vehicle.getCost());
            statement.setBoolean(25, vehicle.isPurchasedOnLoan());
            statement.setObject(26, vehicle.getServicingDate());
            statement.setString(27, vehicle.getOwnerId());

            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    private static void closeStatement(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveLoanDetails(Loan loan, String regNumber) {
        String sql = "insert into loans values(?,?,?,?,?,?) on duplicate key update interest_type =?, " +
                "rate = ?, num_years = ?, principal = ?, start_date = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, regNumber);
            statement.setString(2, loan.getInterestRateType().name());
            statement.setDouble(3, loan.getRate());
            statement.setInt(4, loan.getNumYears());
            statement.setDouble(5, loan.getPrincipal());
            statement.setObject(6, loan.getFirstPayment());
            statement.setString(7, loan.getInterestRateType().name());
            statement.setDouble(8, loan.getRate());
            statement.setInt(9, loan.getNumYears());
            statement.setDouble(10, loan.getPrincipal());
            statement.setObject(11, loan.getFirstPayment());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveInsuranceDetails(VehicleInsurance insurance, String regNumber) {
        String sql = "insert into vehicle_insurance values(?,?,?,?,?,?,?) " +
                "on duplicate  key update company = ?, policy_type =?, policy_number = ?, " +
                "premium_amount = ?, start_date = ? , expiry_date = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, regNumber);
            statement.setString(2, insurance.getCompanyName());
            statement.setString(3, insurance.getPolicyType().name());
            statement.setString(4, insurance.getPolicyNumber());
            statement.setDouble(5, insurance.getPremiumAmount());
            statement.setObject(6, insurance.getStartDate());
            statement.setObject(7, insurance.getExpiryDate());
            statement.setString(8, insurance.getCompanyName());
            statement.setString(9, insurance.getPolicyType().name());
            statement.setString(10, insurance.getPolicyNumber());
            statement.setDouble(11, insurance.getPremiumAmount());
            statement.setObject(12, insurance.getStartDate());
            statement.setObject(13, insurance.getExpiryDate());
            statement.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveLedgeEntry(LedgerEntry entry, String categoryName,  String regNumber, String
            driverId, String
            conductorId) {
        String sql = "insert into ledger_entry values(?,?,?,?,?,?,?,?,?) on duplicate key update reg_number = ?, " +
                "income = ?, expense = ?, " +
                "category = ?, driver_id = ?, conductor_id = ?, date = ?, details = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, entry.getId());
            statement.setString(2, regNumber);
            statement.setDouble(3, entry.getIncome());
            statement.setDouble(4, entry.getExpense());
            statement.setString(5, categoryName);
            statement.setString(6, driverId);
            statement.setString(7, conductorId);
            statement.setObject(8, entry.getDateCreated());
            statement.setString(9, entry.getDetails());
            statement.setString(10, regNumber);
            statement.setDouble(11, entry.getIncome());
            statement.setDouble(12, entry.getExpense());
            statement.setString(13, categoryName);
            statement.setString(14, driverId);
            statement.setString(15, conductorId);
            statement.setObject(16, entry.getDateCreated());
            statement.setString(17, entry.getDetails());

            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

//    public static boolean saveEmployee(Employee employee) {
//        String sql = "insert into conductors values(?,?,?) on duplicate key update first_name = ?, " +
//                "last_name = ?";
//        PreparedStatement statement = null;
//        try {
//            statement = connection.prepareStatement(sql);
//            statement.setString(1, employee.getNationalId());
//            statement.setString(2, employee.getFirstName());
//            statement.setString(3, employee.getLastName());
//            statement.setString(4, employee.getFirstName());
//            statement.setString(5, employee.getLastName());
//            statement.execute();
//            return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeStatement(statement);
//        }
//        return false;
//    }

    public static boolean saveEmployee(Employee employee) {
        String sql = "insert into employees values(?, ?,?,?,?,?,?, ?,?) on duplicate key update category =?, " +
                "first_name = " +
                "?, " +
                "last_name = ?, " +
                "license_number = ?, license_issue_date = ?, license_expiry_date = ?, residence = ?, phone_number = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, employee.getCategory().name());
            statement.setString(2, employee.getNationalId());
            statement.setString(3, employee.getFirstName());
            statement.setString(4, employee.getLastName());
            statement.setString(5, employee.getLicenseNo());
            statement.setObject(6, employee.getLicenseIssueDate());
            statement.setObject(7, employee.getLicenseExpiryDate());
            statement.setString(8, employee.getResidence());
            statement.setString(9, employee.getPhoneNumber());
            statement.setString(10, employee.getCategory().name());
            statement.setString(11, employee.getFirstName());
            statement.setString(12, employee.getLastName());
            statement.setString(13, employee.getLicenseNo());
            statement.setObject(14, employee.getLicenseIssueDate());
            statement.setObject(15, employee.getLicenseExpiryDate());
            statement.setString(16, employee.getResidence());
            statement.setString(17, employee.getPhoneNumber());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveAccident(Accident accident, String driverNationalId, String regNumber) {
        String sql = "insert into accidents values(?,?,?,?,?,?,?,?,?) on duplicate key " +
                "update driver_id = ?, reg_number =?, description = ?, date_reported_insurance = ?, date = ?, time = ?, place = ?,  station_reported = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, accident.getAccidentId());
            statement.setString(2, driverNationalId);
            statement.setString(3, regNumber);
            statement.setString(4, accident.getDescription());
            statement.setObject(5, accident.getDateReportedToInsurance());
            statement.setObject(6, accident.getDate());
            statement.setObject(7, accident.getTime());
            statement.setString(8, accident.getPlace());
            statement.setString(9, accident.getPoliceStationReported());
            statement.setString(10, driverNationalId);
            statement.setString(11, regNumber);
            statement.setString(12, accident.getDescription());
            statement.setObject(13, accident.getDateReportedToInsurance());
            statement.setObject(14, accident.getDate());
            statement.setObject(15, accident.getTime());
            statement.setString(16, accident.getPlace());
            statement.setString(17, accident.getPoliceStationReported());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveAssignments(List<Assignment> assignments, LocalDate date) {
        String sql = "insert into vehicle_assignments values(?,?,?,?) on duplicate key " +
                "update driver_id = ?, conductor_id = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Assignment assignment : assignments) {
                statement.setString(1, assignment.getRegNumber());
                statement.setString(2, assignment.getDriverId());
                statement.setString(3, assignment.getConductorId());
                statement.setObject(4, date);
                statement.setString(5, assignment.getDriverId());
                statement.setString(6, assignment.getConductorId());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveVehicleHire(VehicleHire hire) {
        String sql = "insert into vehicle_hire values(?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, hire.getRegNumber());
            statement.setString(2, hire.getHiree());
            statement.setString(3, hire.getDriverId());
            statement.setObject(4, hire.getStartDate());
            statement.setObject(5, hire.getEndDate());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean scheduleNextAction(String regNumber, ScheduledAction action, LocalDate date) {
        String sql = "insert into scheduled_services(reg_number, " + action.getSqlField() + ") values(?, ?) " +
                "on duplicate key update " + action.getSqlField() + " = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, regNumber);
            statement.setObject(2, date);
            statement.setObject(3, date);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveSettings(List<Setting> settings) {
        String sql = "insert into settings values(?,?,?) on duplicate key update " +
                "duration = ?, time_unit = ? ";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Setting setting : settings) {
                statement.setString(1, setting.getAction().name());
                statement.setInt(2, setting.getDuration());
                statement.setString(3, setting.getTimeUnit().name());
                statement.setInt(4, setting.getDuration());
                statement.setString(5, setting.getTimeUnit().name());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveInsuranceCompany(String insurance) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into insurance_companies values (?) on duplicate key update name = ?");
            statement.setString(1, insurance);
            statement.setString(2, insurance);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveVehicleBrand(String brand) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into vehicle_brands values (?) on duplicate key update name = ?");
            statement.setString(1, brand);
            statement.setString(2, brand);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveFleetOwner(FleetOwner fleetOwner) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into fleet_owners values (?,?,?,?,?,?,?) on duplicate key" +
                    " update first_name = ?, middle_name = ?, last_name = ?, occupation = ?, residence = ?, " +
                    "phone_number = ?");
            statement.setString(1, fleetOwner.getNationalId());
            statement.setString(2, fleetOwner.getFirstName());
            statement.setString(3, fleetOwner.getMiddleName());
            statement.setString(4, fleetOwner.getLastName());
            statement.setString(5, fleetOwner.getOccupation());
            statement.setString(6, fleetOwner.getResidence());
            statement.setString(7, fleetOwner.getPhoneNumber());
            statement.setString(8, fleetOwner.getFirstName());
            statement.setString(9, fleetOwner.getMiddleName());
            statement.setString(10, fleetOwner.getLastName());
            statement.setString(11, fleetOwner.getOccupation());
            statement.setString(12, fleetOwner.getResidence());
            statement.setString(13, fleetOwner.getPhoneNumber());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static void saveActivityLog(String message) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into activity_log values (?, ?, ?, ?)");
            statement.setInt(1, Main.currentUser.getUserId());
            statement.setString(2, message);
            statement.setObject(3, LocalDate.now());
            statement.setObject(4, LocalTime.now());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static boolean createUser(String username, String password) {
        Statement statement = null;
        String sql = "create user  '" + username + "'@'localhost' IDENTIFIED  by '" + password + "'";

        try {
            statement = connection.createStatement();
            statement.addBatch(sql);
            statement.addBatch("GRANT all privileges ON fleet_db.* TO '" + username + "'@'localhost'");
            statement.addBatch("flush privileges");
            statement.executeBatch();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void saveUser(User user) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into users values (?,?,?,?,?) on duplicate key update first_name = ?, last_name = ?, category = ?");
            statement.setInt(1, user.getUserId());
            statement.setString(2, user.getUserName());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getCategory().name());
            statement.setString(6, user.getFirstName());
            statement.setString(7, user.getLastName());
            statement.setString(8, user.getCategory().name());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static void saveUserPermissions(ObservableList<UserPermission> items, int userId) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into user_permissions values (?,?,?) on duplicate key update allowed = ?");
            for (UserPermission userPermission : items) {
                statement.setInt(1, userId);
                statement.setString(2, userPermission.getPermission().name());
                statement.setBoolean(3, userPermission.isAllowed());
                statement.setBoolean(4, userPermission.isAllowed());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static int getNextAutoincrementValue(String tableName) {

        String sql = "select auto_increment from information_schema.tables where table_name ='" + tableName + "' and " +
                "table_schema = DATABASE( )";
        try {
            ResultSet resultSet = DbUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("auto_increment");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean uploadFile(String documentName, String regNumber, File file, int width, int height) {
        String sql = "insert into documents values(?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, 0);
            statement.setString(2, regNumber);
            statement.setBlob(3, new FileInputStream(file));
            statement.setInt(4, width);
            statement.setInt(5, height);
            statement.setString(6, documentName);
            statement.execute();
            return true;
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static void enableCreateUserPermission(String username) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.addBatch("grant create user on *.* to '" + username + "'@'localhost'");
            statement.addBatch("grant delete, update on mysql.* to '" + username + "'@'localhost'");
            statement.addBatch("grant reload on *.* to '" + username + "'@'localhost'");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
