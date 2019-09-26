package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.FleetOwner;
import main.java.model.Permission;
import main.java.model.Vehicle;
import main.java.util.Dao;
import main.java.util.DateUtil;

import java.io.IOException;

public class VehicleListController {

    @FXML
    private TableView<Vehicle> tableView;
    @FXML
    private Button addBtn;
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Vehicle, String> regNumber, regDate, type, make, model, options;
    @FXML
    private TableColumn<Vehicle, Integer> numSeats;
    @FXML
    private ChoiceBox<FleetOwner> fleetOwnerChoiceBox;
    @FXML
    private TabPane tabPane;
    private String regNumberToEdit = null;
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setUpTable();
        getFleetOwners();
        getVehicles();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterResultsByName(newValue);
        });
        fleetOwnerChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            filterResultsByFleetOwner(newValue.intValue());
        });

        ImageView imageView = new ImageView(new Image("main/resources/images/add.png"));
        imageView.setFitWidth(15.0);
        imageView.setFitHeight(15.0);
        addBtn.setGraphic(imageView);
        addBtn.setDisable(!Main.userPermissions.get(Permission.EDIT_VEHICLES));
    }

    @FXML
    private void onViewVehicleBrands() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/vehicle-brands.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterResultsByFleetOwner(int index) {
        if (index == 0) {
            tableView.setItems(vehicles);
        } else{
            ObservableList<Vehicle> filteredList = FXCollections.observableArrayList();
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getOwnerId().equals(fleetOwnerChoiceBox.getItems().get(index).getNationalId())) {
                    filteredList.add(vehicle);
                }
            }
            tableView.setItems(filteredList);
        }
    }

    private void getFleetOwners() {
        Task<ObservableList<FleetOwner>> task = new Task<ObservableList<FleetOwner>>() {
            @Override
            protected ObservableList<FleetOwner> call() throws Exception {
                return Dao.getFleetOwners();
            }
        };
        task.setOnSucceeded(event -> {
            fleetOwnerChoiceBox.setItems(task.getValue());
            FleetOwner fleetOwner = new FleetOwner("All fleet owners", "", "");
            fleetOwnerChoiceBox.getItems().add(0, fleetOwner);
            fleetOwnerChoiceBox.getSelectionModel().select(0);
        });
        new Thread(task).start();
    }

    private void filterResultsByName(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            tableView.setItems(vehicles);
        } else {
            ObservableList<Vehicle> filteredList = FXCollections.observableArrayList();
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getRegistrationNum().toLowerCase().contains(newValue.toLowerCase())) {
                    filteredList.add(vehicle);
                }
            }
            tableView.setItems(filteredList);
        }
    }

    private void getVehicles() {
        Task<ObservableList<Vehicle>> task = new Task<ObservableList<Vehicle>>() {
            @Override
            protected ObservableList<Vehicle> call() {
                return Dao.getVehicles();
            }
        };
        task.setOnSucceeded(event -> {
            vehicles = task.getValue();
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        Label label = new Label("No registered vehicles");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        for (TableColumn column : tableView.getColumns()) {
            if (column == options) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(4));
            } else {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(8));
            }
        }

        regNumber.setCellValueFactory(param -> param.getValue().registrationNumProperty());
        regDate.setCellValueFactory(param -> DateUtil.dateStringProperty(param.getValue().getRegistrationDate()));
        type.setCellValueFactory(param -> param.getValue().typeProperty());
        make.setCellValueFactory(param -> param.getValue().makeProperty());
        model.setCellValueFactory(param -> param.getValue().modelProperty());
        numSeats.setCellValueFactory(param -> param.getValue().numSeatsProperty().asObject());
        options.setCellFactory(param -> new TableCell<Vehicle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button details = new Button("Details");
                    details.getStyleClass().add("btn-info-outline");
                    details.setOnAction(event -> {
                        showDetails(tableView.getItems().get(index).getRegistrationNum());
                    });
                    setGraphic(details);
                    details.setDisable(!Main.userPermissions.get(Permission.VIEW_VEHICLE_DETAILS));

                    Button panel = new Button("Control Panel");
                    panel.setOnAction(event -> {
                        showControlPanel(tableView.getItems().get(index).getRegistrationNum());
                    });
                    panel.getStyleClass().add("btn-info-outline");
                    HBox hBox = new HBox(5.0, details, panel);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void showControlPanel(String registrationNum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/control-panel.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());
            stage.setResizable(false);

            ControlPanelController controller = loader.getController();
            controller.setRegNumber(registrationNum);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(String registrationNum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/vehicle-details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            VehicleDetailsController controller = loader.getController();
            controller.setRegNumber(registrationNum);
            controller.setStage(stage);

            stage.showAndWait();

            if (controller.getEdit()) {
                regNumberToEdit = registrationNum;
                showEditWindow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewRegistration() {
        showEditWindow();
    }

    private void showEditWindow() {
        //close current editing window
        if (tabPane.getTabs().size() > 1) {
            tabPane.getTabs().remove(tabPane.getTabs().size() - 1);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/vehicle-registration.fxml"));

            //create tab
            Tab tab = new Tab("Edit Vehicle");
            tab.setClosable(true);
            tab.setContent(loader.load());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            VehicleRegistrationController controller = loader.getController();
            controller.setContext(this);
            if (regNumberToEdit != null) {
                controller.setRegNumber(regNumberToEdit);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void exitEditMode() {
        tabPane.getTabs().remove(tabPane.getTabs().size() - 1);
        getVehicles();
    }
}
