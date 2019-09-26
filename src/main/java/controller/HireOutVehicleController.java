package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.model.Employee;
import main.java.model.VehicleHire;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.time.LocalDate;

public class HireOutVehicleController {
    private Stage stage;
    private String regNumber;

    @FXML
    private Label title;
    @FXML
    private TextField hireeField;
    @FXML
    private ChoiceBox<Employee> driverChoiceBox;
    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        getDrivers();
    }

    private void getDrivers() {
        Task<ObservableList<Employee>> task = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.DRIVER + "'");
            }
        };
        task.setOnSucceeded(event -> {
            driverChoiceBox.setItems(task.getValue());
        });

        new Thread(task).start();
    }


    @FXML
    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            if (DbUtil.saveVehicleHire(createHire())) {
                DbUtil.saveActivityLog("Created hire record for vehicle '" + regNumber + "'");
                AlertUtil.showAlert("Vehicle Hire", "Hiring details have been successfully saved!", Alert.AlertType.INFORMATION);
                onClose();
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    private VehicleHire createHire() {
        VehicleHire hire = new VehicleHire();
        hire.setDriverId(driverChoiceBox.getValue().getNationalId());
        hire.setEndDate(endDate.getValue());
        hire.setStartDate(startDate.getValue());
        hire.setHiree(hireeField.getText());
        hire.setRegNumber(regNumber);
        return hire;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (startDate.getValue() == null) {
            errorMsg += "Start date required!\n";
        }
        if (endDate.getValue() == null) {
            errorMsg += "End date required!\n";
        }
        if (startDate.getValue() != null && startDate.getValue() != null) {

            if (endDate.getValue().isBefore(startDate.getValue())) {
                errorMsg += "End date should be after start date!\n";
            }

            if (startDate.getValue().isBefore(LocalDate.now())) {
                errorMsg += "Start date should not be before today!\n";
            }
            String vehicleStatus = Dao.getVehicleStatus(startDate.getValue(), endDate.getValue(), regNumber);
            if (vehicleStatus != null) {
                errorMsg += vehicleStatus + "\n";
            }

        }

        if (driverChoiceBox.getValue() == null) {
            errorMsg += "Driver required!\n";
        } else {
            String assignedStatus = Dao.getDriverStatus(startDate.getValue(), endDate.getValue(),driverChoiceBox.getValue().getNationalId());
            if (assignedStatus != null) {
                errorMsg += assignedStatus + "\n";
            }
        }
        if (hireeField.getText() == null || hireeField.getText().isEmpty()) {
            errorMsg += "Hiree required!\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText("Hire out " + regNumber);
    }
}
