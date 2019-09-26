package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Employee;
import main.java.model.FleetOwner;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

import java.io.IOException;
import java.util.Optional;

public class FleetOwnersController {
    @FXML
    private VBox container;
    @FXML
    private TableView<FleetOwner> tableView;
    @FXML
    private TableColumn<FleetOwner, String> nationalId, name, residence, occupation,
    phoneNumber, options;
    @FXML
    private Button addOwner;
    @FXML
    private void initialize() {
        setUpTable();
        getFleetOwners();
        addOwner.setDisable(!Main.userPermissions.get(Permission.REGISTER_FLEET_OWNERS));
    }

    private void getFleetOwners() {
        Task<ObservableList<FleetOwner>> task = new Task<ObservableList<FleetOwner>>() {
            @Override
            protected ObservableList<FleetOwner> call() throws Exception {
                return Dao.getFleetOwners();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No registered fleet owners!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        nationalId.setCellValueFactory(param -> param.getValue().nationalIdProperty());
        residence.setCellFactory(param -> new TableCell<FleetOwner, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    setText(tableView.getItems().get(index).getResidence());
                } else {
                    setText(null);
                }
                setWrapText(true);
            }
        });
        name.setCellFactory(param -> new TableCell<FleetOwner, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    setText(tableView.getItems().get(index).toString());
                } else {
                    setText(null);
                }
                setWrapText(true);
            }
        });
        occupation.setCellValueFactory(param -> param.getValue().occupationProperty());
        phoneNumber.setCellValueFactory(param -> param.getValue().phoneNumberProperty());
        options.setCellFactory(param -> new TableCell<FleetOwner, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.setPrefWidth(100);
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        showRegistrationWindow(tableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.EDIT_FLEET_OWNERS));

                    Button documents = new Button("Documents");
                    documents.getStyleClass().add("btn-info-outline");
                    documents.setOnAction(event -> {
                        viewDocuments(tableView.getItems().get(index));
                    });
                    documents.setDisable(!Main.userPermissions.get(Permission.VIEW_FLEET_OWNER_DOCUMENTS));
                    Button delete = new Button("Delete");
                    delete.setPrefWidth(100);
                    delete.getStyleClass().add("btn-danger-outline");
                    delete.setOnAction(event -> {
                        deleteOwner(tableView.getItems().get(index));
                    });
                    delete.setDisable(!Main.userPermissions.get(Permission.EDIT_FLEET_OWNERS));
                    VBox vBox = new VBox(5.0, edit, documents, delete);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }

    private void viewDocuments(FleetOwner fleetOwner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/view-documents.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            DocumentsController controller = loader.getController();
            controller.setStage(stage);
            controller.setFleetOwner(fleetOwner);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteOwner(FleetOwner fleetOwner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove " + fleetOwner.toString() + " from the list of fleet owners?", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DbUtil.executeStatement("delete from fleet_owners where national_id = '" + fleetOwner.getNationalId() + "'")) {
                AlertUtil.showAlert("", "Fleet owner successfully deleted!", Alert.AlertType.INFORMATION);
                DbUtil.saveActivityLog("Deleted fleet owner '" + fleetOwner.toString() + "'");
                tableView.getItems().remove(fleetOwner);
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    @FXML
    private void onRegisterNewOwner() {
        showRegistrationWindow(null);
    }

    private void showRegistrationWindow(FleetOwner fleetOwner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-fleet-owner.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            EditFleetOwnerController controller = loader.getController();
            controller.setStage(stage);
            controller.setFleetOwner(fleetOwner);

            stage.showAndWait();
            getFleetOwners();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
