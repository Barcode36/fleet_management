package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

public class VehicleBrandsController {
    @FXML
    private TableView<String> tableView;
    @FXML
    private TableColumn<String, String> brandName, edit;
    @FXML
    private TextField editField;

    @FXML
    private void initialize() {
        onSearch();
        ;
        //table
        brandName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));
        edit.setCellFactory(param -> new TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button button = new Button("Edit");
                    button.getStyleClass().add("btn-info-outline");
                    button.setOnAction(event -> {
                        editField.setText(tableView.getItems().get(index));
                        editField.requestFocus();
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        Label label = new Label("No brands found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);
    }

    @FXML
    private void onSave() {
        if (editField.getText() != null) {
            if (DbUtil.saveVehicleBrand(editField.getText())) {
                AlertUtil.showAlert("", "Brand name saved!", Alert.AlertType.INFORMATION);
                onSearch();
                editField.clear();
            } else {
                AlertUtil.showGenericError();
            }
        } else {
            AlertUtil.showAlert("", "Name required!", Alert.AlertType.ERROR);
        }
    }

    private void onSearch() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {

                return Dao.getVehicleBrands();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();

    }
}

