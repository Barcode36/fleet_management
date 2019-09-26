package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.java.model.OperationCategory;
import main.java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class OperationHistoryController {
    private final String DATE = "date";
    private final String OPERATION = "operation";
    private final String DRIVER = "driver";
    private final String CONDUCTOR = "conductor";
    private final String HIRE_START_DATE = "hire_start";
    private final String HIRE_END_DATE = "hire_end";
    private final String HIREE = "hiree";
    @FXML
    private ChoiceBox<Month> monthChoiceBox;
    @FXML
    private TextField yearField;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, operation, details;
    private String regNumber;
    @FXML
    private Label title;

    @FXML
    private void initialize() {
        monthChoiceBox.setItems(FXCollections.observableArrayList(Month.values()));
        monthChoiceBox.getSelectionModel().select(LocalDate.now().getMonth());
        yearField.setText(LocalDate.now().getYear() + "");
        setUpTable();
    }

    private void setUpTable() {
        Label label = new Label("No operation history for specified period!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        operation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OPERATION)));
        details.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> data = tableView.getItems().get(index);
                    VBox vBox = new VBox(5.0);
                    vBox.getStyleClass().add("padding-5");
                    ManageAssignmentsController.setHireDetails(vBox, data);
                    Label driver = new Label("Driver : " + data.get(DRIVER));
                    vBox.getChildren().add(driver);
                    if (data.get(OPERATION).equals(OperationCategory.NORMAL_OPERATION.toString())) {
                        Label conductor = new Label("Conductor : " + data.get(CONDUCTOR));
                        vBox.getChildren().add(conductor);
                    }
                    setGraphic(vBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText((regNumber + " Operation History ").toUpperCase());
        getData();
    }

    @FXML
    private void getData() {
        int year = NumberUtil.stringToInt(yearField.getText());

        if (year == -1) {
            AlertUtil.showAlert("Invalid Year", "Please enter a valid year", Alert.AlertType.ERROR);
            return;
        }
        LocalDate startDate = LocalDate.of(year, monthChoiceBox.getValue(), 1);
        LocalDate end = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getOperationsDetails(startDate, end);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();

    }

    private ObservableList<Map<String, String>> getOperationsDetails(LocalDate startDate, LocalDate end) throws SQLException {

        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select date, drivers.first_name, drivers.last_name, conductors.first_name, " +
                "conductors.last_name " +
                "from vehicle_assignments " +
                "inner join drivers on drivers.national_id = vehicle_assignments.driver_id " +
                "inner join conductors on conductors.national_id = vehicle_assignments.conductor_id " +
                "where reg_number = '" + regNumber + "' " +
                "and date between '" + startDate + "' and '" + end + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DATE, DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class)));
                map.put(DRIVER, resultSet.getString("drivers.first_name") + " " + resultSet.getString("drivers.last_name"));
                map.put(CONDUCTOR, resultSet.getString("conductors.first_name") + " " + resultSet.getString("conductors.last_name"));
                map.put(OPERATION, OperationCategory.NORMAL_OPERATION.toString());

                list.add(map);
            }
        }
        list.addAll(getHireOperationDetails(startDate, end));
        list.sort(new DateComparator());
        return list;
    }

private ObservableList<Map<String, String>> getHireOperationDetails(LocalDate startDate, LocalDate end) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select hiree, start_date, end_date, drivers.first_name, drivers.last_name " +
                "from vehicle_hire " +
                "inner join drivers on drivers.national_id = vehicle_hire.driver_id " +
                "where reg_number = '" + regNumber + "' " +
                "and start_date between '" + startDate + "' and '" + end + "' " +
                "or end_date between '" + startDate + "' and '" + end + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(DATE, DateUtil.formatDateLong(resultSet.getObject("start_date", LocalDate.class)));
                map.put(HIRE_START_DATE, map.get(DATE));
                map.put(HIRE_END_DATE, DateUtil.formatDateLong(resultSet.getObject("end_date", LocalDate.class)));
                map.put(HIREE, resultSet.getString("hiree"));
                map.put(DRIVER, resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                map.put(OPERATION, OperationCategory.HIRE_OPERATION.toString());
                list.add(map);
            }
        }
        return list;
    }


}
