package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Loan;
import main.java.model.Permission;
import main.java.model.Vehicle;
import main.java.util.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleDetailsController {
    private boolean edit;
    private Stage stage;
    private String regNumber;

    @FXML
    private VBox container;
    @FXML
    private Button viewLoanButton, viewDocuments, editButton;
    @FXML
    private Label regNumberLabel, status, regDate, make, model, type, numOfSeats, cost, insuranceCompany, policyType, policyNumber, policyStartDate, policyExpiryDate, lastInspectionDate, speedGovernorRenewal, licenseRenewalDate, loanBalance;

    public void setRegNumber(String registrationNum) {
        this.regNumber = registrationNum;
        regNumberLabel.setText(registrationNum + " : Vehicle Summary");
        getVehicleInfo();
        getCurrentStatus();
        getLoanStatus();
        viewLoanButton.setDisable(!Main.userPermissions.get(Permission.VIEW_LOAN_AMORTIZATION));
        editButton.setDisable(!Main.userPermissions.get(Permission.EDIT_VEHICLES));
        viewDocuments.setDisable(!Main.userPermissions.get(Permission.VIEW_VEHICLE_DETAILS));
    }

    private void getLoanStatus() {
        //get loan
        Task<Loan> task = new Task<Loan>() {
            @Override
            protected Loan call() {
                return Dao.getLoanDetails(regNumber);
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() == null) {
                loanBalance.setText("N/A");
                viewLoanButton.setDisable(true);
            } else {
                loanBalance.setText(CurrencyUtil.formatCurrency(getLoanBalance(task.getValue())));
                viewLoanButton.setOnAction(e -> {
                    viewLoanPaymentSchedule(task.getValue());
                });

            }
        });
        new Thread(task).start();
    }



    private void viewLoanPaymentSchedule(Loan loan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/loan-payment-schedule.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            LoanRepaymentScheduleController controller = loader.getController();
            controller.setLoan(loan);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getLoanBalance(Loan loan) {
        List<Map<String, String>> list = LoanUtility.getRepaymentSchedule(loan);
        LocalDate localDate = LocalDate.now();
        for (Map<String, String> entry : list) {
            if (entry.get(LoanUtility.PERIOD).equals(localDate.getMonth() + " " + localDate.getYear())) {
                return CurrencyUtil.parseCurrency(entry.get(LoanUtility.BALANCE));
            }
        }
        return 0;
    }

    private void getCurrentStatus() {
        Task<String[]> task = new Task<String[]>() {
            @Override
            protected String[] call() throws Exception {
                String[] result = new String[2];
                String sql = "select drivers.first_name, drivers.last_name, " +
                        "conductors.first_name, conductors.last_name " +
                        "from vehicle_assignments " +
                        "inner join employees drivers on drivers.national_id = " +
                        "vehicle_assignments.driver_id " +
                        "inner join employees conductors on conductors.national_id = " +
                        "vehicle_assignments.conductor_id " +
                        "where reg_number = '" + regNumber + " ' " +
                        "and date = '" + LocalDate.now() + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    result[0] = resultSet.getString("drivers.first_name") + " " + resultSet.getString("drivers.last_name");
                    result[1] = resultSet.getString("conductors.first_name") + " " + resultSet.getString("conductors.last_name");
                    return result;
                } else {
                    return null;
                }

            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null && task.getValue().length == 2) {
                status.setText("Assigned to " + task.getValue()[0] + " (Driver) and " + task.getValue()[1] + " (Conductor) ");
            } else {
                getHireStatus();
            }
        });
        new Thread(task).start();
    }

    private void getHireStatus() {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                String sql = "select hiree, start_date, end_date from vehicle_hire " +
                        "where reg_number = '" + regNumber + "' " +
                        "and end_date <= '" + LocalDate.now() + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    list.add(resultSet.getString("hiree"));
                    list.add(DateUtil.formatDateLong(resultSet.getObject("start_date", LocalDate.class)));
                    list.add(DateUtil.formatDateLong(resultSet.getObject("end_date", LocalDate.class)));
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue().size() == 3) {
                status.setText("Hired out to " + task.getValue().get(0) + " from " + task.getValue().get(1) + " to " + task.getValue().get(1));
            } else {
                status.setText("No drivers/conductors assigned!");
                status.getStyleClass().add("missing-content");
            }
        });
        new Thread(task).start();
    }

    private void getVehicleInfo() {
        Task<Vehicle> task = new Task<Vehicle>() {
            @Override
            protected Vehicle call() {
                return Dao.getVehicleDetails(regNumber);
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                setDetails(task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void setDetails(Vehicle vehicle) {
        insuranceCompany.setText(vehicle.getInsurance().getCompanyName());
        policyType.setText(vehicle.getInsurance().getPolicyType().toString());
        policyExpiryDate.setText(DateUtil.formatDateLong(vehicle.getInsurance().getExpiryDate()));
        policyStartDate.setText(DateUtil.formatDateLong(vehicle.getInsurance().getStartDate()));
        policyNumber.setText(vehicle.getInsurance().getPolicyNumber());

        make.setText(vehicle.getMake().toString());
        model.setText(vehicle.getModel());
        type.setText(vehicle.getType().toString());
        regDate.setText(DateUtil.formatDateLong(vehicle.getRegistrationDate()));
        numOfSeats.setText(vehicle.getNumSeats() + "");
        cost.setText(CurrencyUtil.formatCurrency(vehicle.getCost()));

        lastInspectionDate.setText(DateUtil.formatDateLong(vehicle.getPreviousInspectionDate()));
        speedGovernorRenewal.setText(DateUtil.formatDateLong(vehicle.getPreviousSpeedGovernorRenewal()));
        licenseRenewalDate.setText(DateUtil.formatDateLong(vehicle.getPreviousNTSALicenseRenewal()));
    }

    @FXML
    private void onViewDocuments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/view-documents.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            DocumentsController controller = loader.getController();
            controller.setRegNumber(regNumber);
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEdit() {
        edit = true;
        stage.close();
    }

    boolean getEdit() {
        return edit;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
