package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.Main;
import main.java.model.Assignment;
import main.java.model.Employee;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DateUtil;
import main.java.util.DbUtil;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

public class AssignVehiclesController {
    private final String REG_NUMBER = "reg_number";
    private final String CONDUCTOR = "conductor";
    private final String DRIVER = "driver";
    private final String DRIVER_ID = "driver_id";
    private final String CONDUCTOR_ID = "conductor_id";
    private ObservableList<Employee> drivers = FXCollections.observableArrayList(), conductors = FXCollections
            .observableArrayList();
    private LocalDate date;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> regNumber, conductor, driver;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField regNumberField;
    @FXML
    private Button saveButton;
    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());
        getVehiclesList();
        getEmployeeData();
        setUpTable();
        saveButton.setDisable(!Main.userPermissions.get(Permission.ASSIGN_EMPLOYEES));
    }

    private void getEmployeeData() {
        //conductors
        Task<ObservableList<Employee>> employeeListTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.CONDUCTOR.name() +
                        "'");
            }
        };
        employeeListTask.setOnSucceeded(event -> {
            conductors = employeeListTask.getValue();
        });
        new Thread(employeeListTask).start();

        //drivers
        Task<ObservableList<Employee>> driverListTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() throws Exception {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.DRIVER.name() +
                        "'");
            }
        };
        driverListTask.setOnSucceeded(event -> {
            drivers = driverListTask.getValue();
        });
        new Thread(driverListTask).start();
    }

    private void setUpTable() {
        Label label = new Label("No registered vehicles!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        //enable cell selection
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        //data
        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));

        Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>> cellFactory = (param -> new EditingCell(false));
        conductor.setCellFactory(cellFactory);

        cellFactory = (param -> new EditingCell(true));
        driver.setCellFactory(cellFactory);

    }


    private void getVehiclesList() {
        Task<Set<String>> task = new Task<Set<String>>() {
            @Override
            protected Set<String> call() {
                return new HashSet<>(Dao.getRegNumbers());
            }
        };
        task.setOnSucceeded(event -> {
            setUpAutocomplete(task.getValue());
            getCurrentAssignments(task.getValue());

        });

        new Thread(task).start();
    }

    private void setUpAutocomplete(Set<String> regNumbers) {
        AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(regNumberField, FXCollections.observableArrayList(regNumbers));
        binding.setOnAutoCompleted(event -> {
            int row = getRowIndex(event.getCompletion());
            tableView.scrollTo(row);
            tableView.getSelectionModel().select(row);
            tableView.edit(row, driver);
            regNumberField.clear();
        });
    }

    private int getRowIndex(String regNumber) {
        for (int i = 0 ; i < tableView.getItems().size(); i++) {
            if (tableView.getItems().get(i).get(REG_NUMBER).equals(regNumber)) {
                return i;
            }
        }
        return 0;
    }

    @FXML
    private void onSaveAssignments() {
        //only save assignments that have both driver and conductor
        List<Assignment> assignments = getValidAssignments();
        if (assignments.isEmpty()) {
            AlertUtil.showAlert("", "No valid assignments. A vehicle should be assigned BOTH a driver and a conductor", Alert.AlertType.ERROR);
        } else{
            if (DbUtil.saveAssignments(assignments, date)) {
                DbUtil.saveActivityLog("Assigned " + getAssignedRegNumbers(assignments) + "");
                AlertUtil.showAlert("Success", "Assignment(s) successfully saved!", Alert.AlertType.INFORMATION);

                onClose();
            } else{
                AlertUtil.showAlert("Error", "An error occurred while attempting to save vehicle assignments", Alert.AlertType.ERROR);
            }
        }
    }

    private String getAssignedRegNumbers(List<Assignment> assignments) {
        int numAssignments = assignments.size();
        if (numAssignments == 1) {
            return "'" +  assignments.get(0).getRegNumber() + "'";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numAssignments; i++) {
            stringBuilder.append("'").append(assignments.get(i).getRegNumber()).append("'");
            if (i == numAssignments - 2) {
                //second last
                stringBuilder.append(" and ");
            } else if (i < numAssignments - 2) {
                //everthing else except second last and last
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private List<Assignment> getValidAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        for (Map<String, String> entry : tableView.getItems()) {
            if (entry.get(DRIVER_ID) != null && entry.get(CONDUCTOR_ID) != null) {
                Assignment assignment = new Assignment();
                assignment.setDriverId(entry.get(DRIVER_ID));
                assignment.setConductorId(entry.get(CONDUCTOR_ID));
                assignment.setRegNumber(entry.get(REG_NUMBER));
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    private void getCurrentAssignments(Set<String> allVehicles) {
        date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        datePicker.setValue(date);


        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                List<String> assignedRegs = new ArrayList<>();
                String sql = "select vehicle_assignments.*, drivers.first_name, drivers.last_name, conductors.first_name, " +
                        "conductors.last_name " +
                        "from vehicle_assignments " +
                        "inner join conductors on conductors.national_id = vehicle_assignments.conductor_id " +
                        "inner join drivers on drivers.national_id = vehicle_assignments.driver_id " +
                        "where date = '" + date + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);

                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> map = new HashMap<>();
                        map.put(CONDUCTOR, resultSet.getString("conductors.first_name") + " " + resultSet.getString("conductors.last_name"));
                        map.put(DRIVER, resultSet.getString("drivers.first_name") + " " + resultSet.getString("drivers.last_name"));
                        map.put(REG_NUMBER, resultSet.getString("reg_number"));
                        map.put(DRIVER_ID, resultSet.getString("driver_id"));
                        map.put(CONDUCTOR_ID, resultSet.getString("conductor_id"));
                        assignedRegs.add(map.get(REG_NUMBER));
                        list.add(map);
                    }
                }
                for (String regNumber : allVehicles) {
                    if (!assignedRegs.contains(regNumber)) {
                        Map<String, String> map = new HashMap<>();
                        map.put(REG_NUMBER, regNumber);
                        list.add(map);
                    }
                }
                return list;

            }
        };

        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }



    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void onClose() {
        stage.close();
    }

    @FXML
    private void onSearch() {
        date = datePicker.getValue();
        if (date == null || date.isBefore(LocalDate.now())) {
            date = LocalDate.now();
        }
        datePicker.setValue(date);
        getVehiclesList();

    }

    private class EditingCell extends TableCell<Map<String, String>, String> {
        private TextField textField;
        private boolean isDriver;
        EditingCell(boolean isDriver) {
            this.isDriver = isDriver;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            if (index >= 0 && index < tableView.getItems().size()) {
                Map<String, String> entry = tableView.getItems().get(index);
                textField = createTextField(index);
                setGraphic(textField);

                if (isDriver) {
                    AutoCompletionBinding<Employee> binding = TextFields.bindAutoCompletion(textField, drivers);
                    binding.setOnAutoCompleted(event ->  {
                        Employee driver = event.getCompletion();
                        String driverStatus = Dao.getDriverStatus(datePicker.getValue(), datePicker.getValue(), driver.getNationalId());
                        if (driverStatus != null) {
                            AlertUtil.showAlert("Driver Unavailable", driverStatus, Alert.AlertType.ERROR);
                            textField.clear();
                        } else{
                            entry.put(DRIVER, driver.getFirstName() + " " + driver.getLastName());
                            entry.put(DRIVER_ID, driver.getNationalId());
                        }
                    });
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            entry.put(DRIVER_ID, null);
                        }
                    });
                } else{
                    AutoCompletionBinding<Employee> binding = TextFields.bindAutoCompletion(textField, conductors);
                    binding.setOnAutoCompleted(event ->  {
                        Employee employee = event.getCompletion();
                        String conductorStatus = Dao.getConductorStatus(datePicker.getValue(), employee.getNationalId());
                        if (conductorStatus != null) {
                            AlertUtil.showAlert("Conductor Unavailable", conductorStatus, Alert.AlertType.ERROR);
                            textField.clear();
                        } else{
                            entry.put(CONDUCTOR, employee.getFirstName() + " " + employee.getLastName());
                            entry.put(CONDUCTOR_ID, employee.getNationalId());
                        }

                    });
                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            entry.put(CONDUCTOR_ID, null);
                        }
                    });
                }
            } else{
                setGraphic(null);
            }

        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (textField != null) {
                textField.requestFocus();
            }
        }

        private TextField createTextField(int index) {
            TextField textField = new TextField();
            if (isDriver) {
                textField.setText(tableView.getItems().get(index).get(DRIVER));
            } else{
                textField.setText(tableView.getItems().get(index).get(CONDUCTOR));
            }
            return textField;
        }
    }
}
