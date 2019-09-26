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

public class AnnualInspectionController {
    private String regNumber;
    @FXML
    private DatePicker inspectionDate, nextInspection;
    @FXML
    private HBox prevInspection;
    @FXML
    private Label title;
    private Stage stage;

    @FXML
    private void initialize() {
        inspectionDate.setConverter(DateUtil.getDatePickerConverter());
        nextInspection.setConverter(DateUtil.getDatePickerConverter());

        inspectionDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nextInspection.setValue(newValue.plusYears(1));
            }
        });
    }


    @FXML
    private void onSave() {
        if (validInput()) {
            String sql = "update vehicles set inspection_date = '" + inspectionDate.getValue() + "' where reg_number = '" + regNumber + "'";
             if (DbUtil.executeStatement(sql) && DbUtil.scheduleNextAction(regNumber, ScheduledAction.INSPECTION, nextInspection.getValue())) {
                 DbUtil.saveActivityLog("Recorded annual inspection { Vehicle: " + regNumber + ", Date: " + DateUtil.formatDateLong(inspectionDate.getValue()) + " }");
                 AlertUtil.showAlert("Annual Inspection", "Inspection date for vehicle '" + regNumber + "' has been recorded.", Alert.AlertType.INFORMATION);
                 onClose();
             } else{
                 AlertUtil.showGenericError();
             }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (inspectionDate.getValue() == null) {
            errorMsg += "Inspection date required!\n";
        } else if (inspectionDate.getValue().isBefore(LocalDate.now())) {
            errorMsg += "Inspection date should not be in the past!\n";
        }
        if (nextInspection.getValue() == null) {
            errorMsg += "Next inspection date required!\n";
        }
        if (inspectionDate.getValue() != null && nextInspection.getValue() != null) {
            if (nextInspection.getValue().isBefore(inspectionDate.getValue())) {
                errorMsg += "Next inspection date should be after current inspection date!\n";
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

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText(regNumber + " Annual Inspection");
        getLastInspection();
    }

    private void getLastInspection() {
        Task<LocalDate> task = new Task<LocalDate>() {
            @Override
            protected LocalDate call() throws Exception {
                String sql = "select inspection_date from vehicles " +
                        "where reg_number = '" + regNumber + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    if (resultSet.getObject("inspection_date") != null) {
                        return resultSet.getObject("inspection_date", LocalDate.class);
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                Label label = new Label("Last inspection was on ");
                prevInspection.getChildren().add(label);
                label = new Label(DateUtil.formatDate(task.getValue()));
                label.getStyleClass().add("fw-500");
                prevInspection.getChildren().add(label);
            } else{
                prevInspection.getChildren().add(new Label("No inspection history found"));
                prevInspection.getChildren().get(0).setStyle("-fx-text-fill: #ff8800");
            }
        });
        new Thread(task).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

