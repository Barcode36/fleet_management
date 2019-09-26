package main.java.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.java.model.OperationCategory;
import main.java.util.Dao;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManageAssignmentsController {
    private final String REG_NUMBER = "reg_number";
    private static final String OPERATION = "operation";
    private final String DRIVER = "driver";
    private final String CONDUCTOR = "conductor";
    private static final String HIRE_START_DATE = "hire_start";
    private static final String HIRE_END_DATE = "hire_end";
    private static final String HIREE = "hiree";
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> regNumber, driver, details, operation;
    @FXML
    private Label title;

    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());
        Platform.runLater(() -> {
            tableView.requestFocus(); //remove focus from date picker
        });
        getData();

        setUpTable();
    }

    private void setUpTable() {
        Label label = new Label("No vehicle assignments for today");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        operation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OPERATION)));
        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));
        driver.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DRIVER)));
        details.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    VBox vBox = new VBox(5.0);
                    vBox.getStyleClass().add("padding-5");
                    Map<String, String> data = tableView.getItems().get(index);
                    setHireDetails(vBox, data);
                    if ( data.get(OPERATION) != null &&
                            data.get(OPERATION).equals(OperationCategory.NORMAL_OPERATION.toString())) {
                        vBox.getChildren().add(new Label("Conductor : " + data.get(CONDUCTOR)));
                    }
                    setGraphic(vBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }

    static void setHireDetails(VBox vBox, Map<String, String> data) {
        if ( data.get(OPERATION) != null &&
                data.get(OPERATION).equals(OperationCategory.HIRE_OPERATION.toString())) {
            Label hiree = new Label("Hiree : " + data.get(HIREE));
            Label start = new Label("Start Date : " + data.get(HIRE_START_DATE));
            Label end = new Label("End Date : " + data.get(HIRE_END_DATE));
            vBox.getChildren().addAll(hiree, start, end);
        }
    }

    @FXML
    private void getData() {
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getAssignments(date);

            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
            title.setText("Vehicle Status : " + DateUtil.formatDateLong(date));
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getAssignments(LocalDate date) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select reg_number, drivers.first_name, drivers.last_name, conductors.first_name " +
                ",conductors.last_name from vehicle_assignments " +
                "inner join drivers on vehicle_assignments.driver_id = drivers.national_id " +
                "inner join conductors on vehicle_assignments.conductor_id = conductors.national_id " +
                "where date = '" + date + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(REG_NUMBER, resultSet.getString("reg_number"));
                map.put(DRIVER, resultSet.getString("drivers.first_name") + " " + resultSet.getString("drivers.last_name"));
                map.put(CONDUCTOR, resultSet.getString("conductors.first_name") + " " + resultSet.getString("conductors.last_name"));
                map.put(OPERATION, OperationCategory.NORMAL_OPERATION.toString());
                list.add(map);
            }
        }
        list.addAll(getHireOperations(date));
        list.addAll(getUnassignedVehicles(list));
        return list;
    }

    private ObservableList<Map<String, String>> getUnassignedVehicles(ObservableList<Map<String, String>> list) {
        ObservableList<Map<String, String>> unassigned = FXCollections.observableArrayList();
        ObservableList<String> allRegNumbers = Dao.getRegNumbers();
        Set<String> assigned = new HashSet<>();
        for (Map<String, String> data : list) {
            assigned.add(data.get(REG_NUMBER));
        }
        for (String regNumber : allRegNumbers) {
            if (!assigned.contains(regNumber)) {
                Map<String, String> data = new HashMap<>();
                data.put(REG_NUMBER, regNumber);
                data.put(DRIVER, null);
                data.put(OPERATION, null);
                unassigned.add(data);
            }
        }
        return unassigned;
    }

    private ObservableList<Map<String, String>> getHireOperations(LocalDate date) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select vehicle_hire.*, drivers.first_name, drivers.last_name " +
                "from vehicle_hire " +
                "inner join drivers on drivers.national_id = vehicle_hire.driver_id " +
                "where start_date <= '" + date + "' and end_date >= '" + date + "'";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> data = new HashMap<>();
                data.put(REG_NUMBER, resultSet.getString("reg_number"));
                data.put(HIRE_END_DATE, DateUtil.formatDateLong(resultSet.getObject("end_date", LocalDate.class)));
                data.put(HIRE_START_DATE, DateUtil.formatDateLong(resultSet.getObject("start_date", LocalDate.class)));
                data.put(OPERATION, OperationCategory.HIRE_OPERATION.toString());
                data.put(HIREE, resultSet.getString("hiree"));
                data.put(DRIVER, resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                list.add(data);
            }
        }
        return list;
    }
}
