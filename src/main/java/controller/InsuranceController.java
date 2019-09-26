package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

import java.io.IOException;
import java.util.Optional;

public class InsuranceController {
    @FXML
    private VBox container;
    @FXML
    private TableView<String> tableView;
    @FXML
    private TableColumn<String, String> name, options;

    @FXML
    private void initialize() {
        setUpTable();
        getInsuranceList();
    }

    private void getInsuranceList() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return Dao.getInsuranceCompanies();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void deleteInsurance(String insurance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + insurance + "'", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DbUtil.executeStatement("delete from insurance_companies where name = '" + insurance + "'")) {
                DbUtil.saveActivityLog("Deleted insurance '" + insurance + "'");
                AlertUtil.showAlert("Delete Insurance", "'" + insurance + "' has been successfully deleted!", Alert.AlertType.INFORMATION);
                tableView.getItems().remove(insurance);
            } else{
                AlertUtil.showGenericError();
            }
        }

    }

    @FXML
    private void onCreateInsurance() {
        showEditWindow(null);
    }

    private void showEditWindow(String insurance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-insurance.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);
            EditInsuranceController controller = loader.getController();
            controller.setStage(stage);
            controller.setInsurance(insurance);
            stage.showAndWait();
            getInsuranceList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpTable() {
        Label label = new Label("No registered insurance companies!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));
        options.setCellFactory(param -> new TableCell<String, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        showEditWindow(tableView.getItems().get(index));
                    });

                    Button delete = new Button("Delete");
                    delete.getStyleClass().add("btn-danger-outline");
                    delete.setOnAction(event -> {
                        deleteInsurance(tableView.getItems().get(index));
                    });
                    delete.setDisable(!Main.userPermissions.get(Permission.DELETE_INSURANCE));
                    HBox hBox = new HBox(5.0, edit, delete);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }
}
