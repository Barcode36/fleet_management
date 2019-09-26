package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.model.ScheduledAction;
import main.java.util.AlertUtil;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.sql.ResultSet;
import java.time.LocalDate;

public class RenewLicenseController {
    @FXML
    private Label title;
    @FXML
    private HBox prevInspection;
    @FXML
    private DatePicker renewalDate, nextRenewal;
    private Stage stage;
    private String regNumber;

    @FXML
    private void initialize() {
        renewalDate.setConverter(DateUtil.getDatePickerConverter());
        nextRenewal.setConverter(DateUtil.getDatePickerConverter());
        renewalDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nextRenewal.setValue(newValue.plusYears(1));
            }
        });
    }
    @FXML
    private void onSave() {
        if (validInput()) {
            String sql = "update vehicles set license_renewal_date = '" + renewalDate.getValue() + "' where reg_number = '" + regNumber + "'";
            if (DbUtil.executeStatement(sql) && DbUtil.scheduleNextAction(regNumber, ScheduledAction.NTSA_LICENSE_RENEWAL, nextRenewal.getValue())) {
                DbUtil.saveActivityLog("Recorded license renewal { Vehicle: " + regNumber + ", Date: " + DateUtil.formatDateLong(renewalDate.getValue()) + " }");
                AlertUtil.showAlert("NTSA License Renewal", "Next renewal date for '" + regNumber + "' has been recorded.", Alert.AlertType.INFORMATION);
                onClose();
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (renewalDate.getValue() == null) {
            errorMsg += "License renewal date required!\n";
        } else if (renewalDate.getValue().isBefore(LocalDate.now())) {
            errorMsg += "License renewal date should not be in the past!\n";
        }
        if (nextRenewal.getValue() == null) {
            errorMsg += "Next renewal date required!\n";
        }
        if (renewalDate.getValue() != null && nextRenewal.getValue() != null) {
            if (nextRenewal.getValue().isBefore(renewalDate.getValue())) {
                errorMsg += "Next renewal date should be after " + DateUtil.formatDateLong(renewalDate.getValue());
            }
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText(regNumber  +  " NTSA Operation License Renewal");
        getLastRenewal();
    }

    private void getLastRenewal() {
        Task<LocalDate> task = new Task<LocalDate>() {
            @Override
            protected LocalDate call() throws Exception {
                String sql = "select license_renewal_date from vehicles " +
                        "where reg_number = '" + regNumber + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    if (resultSet.getObject("license_renewal_date") != null) {
                        return resultSet.getObject("license_renewal_date", LocalDate.class);
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                Label label = new Label("Last renewal was on ");
                prevInspection.getChildren().add(label);
                label = new Label(DateUtil.formatDate(task.getValue()));
                label.getStyleClass().add("fw-500");
                prevInspection.getChildren().add(label);
            } else{
                prevInspection.getChildren().add(new Label("No license renewal history found"));
                prevInspection.getChildren().get(0).setStyle("-fx-text-fill: #ff8800");
            }
        });
        new Thread(task).start();
    }
}
