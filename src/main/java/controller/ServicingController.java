package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.model.ScheduledAction;
import main.java.util.AlertUtil;
import main.java.util.DateUtil;
import main.java.util.DbUtil;
import main.java.util.NumberUtil;

import java.sql.ResultSet;
import java.time.LocalDate;

public class ServicingController {
    private Stage stage;
    private String regNumber;

    @FXML
    private Label title;
    @FXML
    private HBox prevInspection;
    @FXML
    private DatePicker servicingDate, nextServicing;
    @FXML
    private TextField numDays;

    @FXML
    private void initialize() {
        servicingDate.setConverter(DateUtil.getDatePickerConverter());
        nextServicing.setConverter(DateUtil.getDatePickerConverter());
        numDays.textProperty().addListener((observable, oldValue, newValue) -> {
            if (NumberUtil.stringToInt(newValue) != -1) {
                if (servicingDate.getValue() != null) {
                    nextServicing.setValue(servicingDate.getValue().plusDays(NumberUtil.stringToInt(newValue)));
                }
            }
        });
        servicingDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (NumberUtil.stringToInt(numDays.getText()) != -1) {
                if (newValue != null) {
                    nextServicing.setValue(newValue.plusDays(NumberUtil.stringToInt(numDays.getText())));
                }
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
            String sql = "update vehicles set servicing_date = '" + servicingDate.getValue() + "' where reg_number = '" + regNumber + "'";
            if (DbUtil.executeStatement(sql) && DbUtil.scheduleNextAction(regNumber, ScheduledAction.SERVICING, nextServicing.getValue())) {
                DbUtil.saveActivityLog("Recorded vehicle servicing { Vehicle: " + regNumber + ", Date: " + DateUtil.formatDateLong(servicingDate.getValue()) + " }");
                AlertUtil.showAlert("Servicing", "Next servicing date for vehicle '" + regNumber + "' has been saved.", Alert.AlertType.INFORMATION);
                onClose();
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (servicingDate.getValue() == null) {
            errorMsg += "Servicing date required!\n";
        } else if (servicingDate.getValue().isBefore(LocalDate.now())) {
            errorMsg += "Servicing date should not be in the past!\n";
        }
        if (nextServicing.getValue() == null) {
            errorMsg += "Next servicing date required!\n";
        }
        if (servicingDate.getValue() != null && nextServicing.getValue() != null) {
            if (nextServicing.getValue().isBefore(servicingDate.getValue())) {
                errorMsg += "Next servicing date should be after current servicing date!\n";
            }
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
        title.setText(regNumber + " Servicing");
        getPrevServicing();
    }

    private void getPrevServicing() {
        Task<LocalDate> task = new Task<LocalDate>() {
            @Override
            protected LocalDate call() throws Exception {
                String sql = "select servicing_date from vehicles " +
                        "where reg_number = '" + regNumber + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null && resultSet.next()) {
                    if (resultSet.getObject("servicing_date") != null) {
                        return resultSet.getObject("servicing_date", LocalDate.class);
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                Label label = new Label("Last servicing was on ");
                prevInspection.getChildren().add(label);
                label = new Label(DateUtil.formatDate(task.getValue()));
                label.getStyleClass().add("fw-500");
                prevInspection.getChildren().add(label);
            } else {
                prevInspection.getChildren().add(new Label("No servicing history found!"));
                prevInspection.getChildren().get(0).setStyle("-fx-text-fill: #ff8800");
            }
        });
        new Thread(task).start();
    }

}
