package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AccidentsController {
    static final String ACCIDENT_ID = "accident_id";
    static final String DATE_REPORTED_INSURANCE = "insurance_date_report";
    static final String DATE = "date";
    static final String REG_NUMBER = "reg_number";
    static final String PLACE = "place";
    static final String DRIVER_NAME = "name";
    static final String DRIVER_ID = "driver_id";
    static final String DRIVER_LICENSE = "license";
    static final String POLICE_STATION = "station";
    static final String DESCRIPTION = "description";

    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, policeStation, description, regNumber, driverDetails, insurance, options;
    @FXML
    private Button addRecord;

    @FXML
    private void initialize() {
        setUpTable();
        getRecords();
        addRecord.setDisable(!Main.userPermissions.get(Permission.RECORD_ACCIDENT));
    }

    private void getRecords() {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getAccidentRecords();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getAccidentRecords() throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select accidents.*, national_id, first_name , last_name, license_number " +
                "from accidents " +
                "inner join employees on employees.national_id = accidents.driver_id";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(ACCIDENT_ID, resultSet.getString("accident_id"));
                map.put(DRIVER_NAME, resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                LocalTime time = resultSet.getObject("time", LocalTime.class);
                LocalDate date = resultSet.getObject("date", LocalDate.class);
                map.put(DATE, DateUtil.formatDateTime(LocalDateTime.of(date, time)));
                map.put(PLACE, resultSet.getString("place"));
                map.put(POLICE_STATION, resultSet.getString("station_reported"));

                if (resultSet.getObject("date_reported_insurance") != null) {
                    map.put(DATE_REPORTED_INSURANCE, DateUtil.formatDate(resultSet.getObject("date_reported_insurance", LocalDate.class)));
                } else{
                    map.put(DATE_REPORTED_INSURANCE, null);
                }
                map.put(REG_NUMBER, resultSet.getString("reg_number"));
                map.put(DRIVER_ID, resultSet.getString("national_id"));
                map.put(DRIVER_LICENSE, resultSet.getString("license_number"));
                map.put(DESCRIPTION, resultSet.getString("description"));
                list.add(map);
            }
        }
        return list;
    }

    private void setUpTable() {
        Label placeHolder = new Label("No accident records found!");
        placeHolder.getStyleClass().add("missing-content");
        tableView.setPlaceholder(placeHolder);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        insurance.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE_REPORTED_INSURANCE)));
        policeStation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(POLICE_STATION)));
        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));
        driverDetails.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> entry = tableView.getItems().get(index);
                    Text text = new Text();
                    text.setText("Name: " + entry.get(DRIVER_NAME) + "\n" + "National ID: " + entry.get(DRIVER_ID) + "\n" +
                            "License No: " + entry.get(DRIVER_LICENSE));
                    text.setLineSpacing(1.8);
                    text.wrappingWidthProperty().bind(driverDetails.widthProperty());
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    setGraphic(text);
                } else{
                    setGraphic(null);
                }
            }
        });
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        editRecord(tableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.MODIFY_ACCIDENT));

                    Button delete = new Button("Delete");
                    delete.getStyleClass().add("btn-danger-outline");
                    delete.setOnAction(event -> {
                        deleteRecord(tableView.getItems().get(index));
                    });
                    delete.setDisable(!Main.userPermissions.get(Permission.MODIFY_ACCIDENT));
                    VBox vBox = new VBox(5.0, edit, delete);
                    setGraphic(vBox);
                    vBox.setAlignment(Pos.CENTER);
                } else{
                    setGraphic(null);
                }

            }
        });
        description.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Text text = new Text(tableView.getItems().get(index).get(DESCRIPTION));
                    text.setLineSpacing(1.8);
                    text.wrappingWidthProperty().bind(description.widthProperty());
                    setText(tableView.getItems().get(index).get(DESCRIPTION));
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    setGraphic(text);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void deleteRecord(Map<String, String> map) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete accident record?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "delete from accidents where accident_id = " + map.get(ACCIDENT_ID);
            if (DbUtil.executeStatement(sql)) {
                DbUtil.saveActivityLog("Deleted accident record { Vehicle: " + map.get(REG_NUMBER) + ", Date: " + map.get(DATE) + " }");
                AlertUtil.showAlert("Delete Accident", "Accident record successfully deleted!", Alert.AlertType.INFORMATION);
                tableView.getItems().remove(map);
            } else{
                AlertUtil.showAlert("Delete Error", "An error occurred while attempting to delete record.", Alert.AlertType.ERROR);
            }
        }
    }

    private void editRecord(Map<String, String> map) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-accident.fxml"));

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            EditAccidentController controller = loader.getController();
            controller.setStage(stage);
            if (map != null) {
                controller.setRecord(map);
            }
            stage.showAndWait();
            getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewRecord() {
        editRecord(null);
    }
}

