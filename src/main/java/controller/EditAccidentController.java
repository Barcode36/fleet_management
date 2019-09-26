package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.model.Accident;
import main.java.model.Employee;
import main.java.model.TimePeriod;
import main.java.util.*;

import java.time.LocalDateTime;
import java.util.Map;

import static main.java.controller.AccidentsController.*;

public class EditAccidentController {
    private Stage stage;
    private TimePicker timePicker;
    private Map<String, String> record;
    @FXML
    private ChoiceBox<String> hourPicker, minutePicker, regNumbersChoiceBox;
    @FXML
    private ChoiceBox<Employee> driverChoiceBox;
    @FXML
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    @FXML
    private TextField place, driverId, driverLicense, policeStation, description;
    @FXML
    private DatePicker accidentDate, insuranceReportDate;

    @FXML
    private void initialize() {
        timePicker = new TimePicker(hourPicker, minutePicker, timePeriodChoiceBox);
        getVehicles();
        getDrivers();
        driverChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                driverLicense.setText(newValue.getLicenseNo());
                driverId.setText(newValue.getNationalId());
            }
        });
    }

    private void getVehicles() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                return Dao.getRegNumbers();
            }
        };
        task.setOnSucceeded(event -> {
            regNumbersChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getDrivers() {

        Task<ObservableList<Employee>> driversListTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.DRIVER + "'");
            }
        };
        driversListTask.setOnSucceeded(event -> {
            driverChoiceBox.setItems(driversListTask.getValue());
            for (Employee driver : driverChoiceBox.getItems()) {
                if (driver.getNationalId().equals(record.get(DRIVER_ID))) {
                    driverChoiceBox.setValue(driver);
                }
            }
        });
        new Thread(driversListTask).start();
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRecord(Map<String, String> record) {
        this.record = record;
        regNumbersChoiceBox.setValue(record.get(REG_NUMBER));
        LocalDateTime dateTime = DateUtil.parseDateTime(record.get(DATE));

        if (dateTime != null) {
            accidentDate.setValue(dateTime.toLocalDate());
            timePicker.setTime(dateTime.toLocalTime());
        }
        place.setText(record.get(PLACE));
        driverId.setText(record.get(DRIVER_ID));
        driverLicense.setText(record.get(DRIVER_LICENSE));
        policeStation.setText(record.get(POLICE_STATION));
        if (record.get(DATE_REPORTED_INSURANCE) != null) {
            insuranceReportDate.setValue(DateUtil.parseDate(record.get(DATE_REPORTED_INSURANCE)));
        }
        description.setText(record.get(DESCRIPTION));
        getDrivers();
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            if (saveDetails()) {
                if (record == null) {
                    //new accident record
                    DbUtil.saveActivityLog("Recorded accident { Vehicle: " + regNumbersChoiceBox.getValue() + ", Date: " + DateUtil.formatDateLong(accidentDate.getValue()) + ", Driver : " + driverChoiceBox.getValue().toString() + " }");
                } else {
                    DbUtil.saveActivityLog("Modified accident record { Vehicle: " + regNumbersChoiceBox.getValue() + ", Date: " + DateUtil.formatDateLong(accidentDate.getValue()) + ", Driver : " + driverChoiceBox.getValue().toString() + " }");
                }
                AlertUtil.showAlert("Accident Record", "Accident record has been successfully saved!", Alert.AlertType.INFORMATION);
                onCancel();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save accident record.", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean saveDetails() {
        Accident accident = new Accident();
        if (record != null) {
            accident.setAccidentId(Integer.parseInt(record.get(ACCIDENT_ID)));
        }
        accident.setPlace(place.getText());
        accident.setPoliceStationReported(policeStation.getText());
        accident.setDescription(description.getText());
        accident.setDate(accidentDate.getValue());
        accident.setDateReportedToInsurance(insuranceReportDate.getValue());
        accident.setTime(timePicker.getSelectedTime());

        return DbUtil.saveAccident(accident, driverId.getText(), regNumbersChoiceBox.getValue());
    }

    private boolean validInput() {
        String errorMsg = "";

        if (regNumbersChoiceBox.getValue() == null) {
            errorMsg += "Vehicle registration number required!\n";
        }
        if (accidentDate.getValue() == null) {
            errorMsg += "Date of accident required!\n";
        }
        if (timePicker.getSelectedTime() == null) {
            errorMsg += "Time of accident required!\n";
        }
        if (place.getText() == null || place.getText().isEmpty()) {
            errorMsg += "Place of accident required!\n";
        }
        if (driverChoiceBox.getValue() == null) {
            errorMsg += "Driver required\n";
        }
        if (policeStation.getText() == null || policeStation.getText().isEmpty()) {
            errorMsg += "Police station where accident was reported required!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    public void setRegNumber(String regNumber) {
        regNumbersChoiceBox.setValue(regNumber);
    }
}
