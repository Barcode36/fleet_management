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

public class RenewSpeedGovController {
    @FXML
    private Label title;
    @FXML
    private DatePicker renewalDate, nextRenewal;
    @FXML
    private HBox prevInspection;
    private String regNumber;
    private Stage stage;

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
    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            String sql = "update vehicles set speed_gov_renewal_date = '" + renewalDate.getValue() + "' where reg_number = '" + regNumber + "'";
            if (DbUtil.executeStatement(sql) && DbUtil.scheduleNextAction(regNumber, ScheduledAction.SPEED_GOV_RENEWAL, nextRenewal.getValue())) {
                DbUtil.saveActivityLog("Recorded speed governor renewal { Vehicle: " + regNumber + ", Date: " + DateUtil.formatDateLong(renewalDate.getValue()) + " }");
                AlertUtil.showAlert("Speed Gov. Renewal", "Speed governor renewal for vehicle '" + regNumber + "' has been recorded. Next renewal is on " + DateUtil.formatDateLong(nextRenewal.getValue()), Alert.AlertType.INFORMATION);
                onClose();
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (renewalDate.getValue() == null) {
            errorMsg += "Renewal date required!\n";
        } else if (renewalDate.getValue().isBefore(LocalDate.now())) {
            errorMsg += "Renewal date should not be in the past!\n";
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

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText(regNumber + " Speed Governor Renewal");
        getPrevRenewal();
    }

    private void getPrevRenewal() {
        Task<LocalDate> task = new Task<LocalDate>() {
            @Override
            protected LocalDate call() throws Exception {
                String sql = "select speed_gov_renewal_date from vehicles " +
                        "where reg_number = '" + regNumber + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    if (resultSet.getObject("speed_gov_renewal_date") != null) {
                        return resultSet.getObject("speed_gov_renewal_date", LocalDate.class);
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                Label label = new Label("Last renewal date was on ");
                prevInspection.getChildren().add(label);
                label = new Label(DateUtil.formatDate(task.getValue()));
                label.getStyleClass().add("fw-500");
                prevInspection.getChildren().add(label);
            } else{
                prevInspection.getChildren().add(new Label("No renewal history found!"));
                prevInspection.getChildren().get(0).setStyle("-fx-text-fill: #ff8800");
            }
        });
        new Thread(task).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
