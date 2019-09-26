package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import main.java.model.User;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class UserActivityController {
    private final String DATE = "date";
    private final String DESCRIPTION = "description";
    @FXML
    private DatePicker startDate, endDate;
    private User user;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, description;
    @FXML
    private Label title;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        startDate.setValue(LocalDate.now());

        endDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setValue(LocalDate.now());
        setUpTable();
    }
    private void setUpTable() {
        Label label = new Label("No Activity found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));

        description.prefWidthProperty().bind(tableView.widthProperty().divide(1));
        description.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Text text = new Text(tableView.getItems().get(index).get(DESCRIPTION));
                    text.wrappingWidthProperty().bind(description.widthProperty());
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    setGraphic(text);
                } else{
                    setGraphic(null);
                }
            }
        });
    }
    public void setUser(User user) {
        this.user = user;
        title.setText(user.getFirstName() + "'s Activity");
        onSearch();
    }

    @FXML
    private void onSearch() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now();
        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
                ResultSet resultSet = DbUtil.executeQuery("select * from activity_log where user_id = " + user.getUserId() + " and date between '" + start + "' and '" + end + "'");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        Map<String, String> data = new HashMap<>();
                        data.put(DATE, DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class)) + " " + DateUtil.formatTime(resultSet.getObject("time", LocalTime.class)));
                        data.put(DESCRIPTION, resultSet.getString("message"));
                        list.add(data);
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
}
