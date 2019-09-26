package main.java.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Employee;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.io.IOException;
import java.util.Optional;

public class EmployeesController {
    private final String CONDUCTORS = "conductors";
    private final String DRIVERS = "drivers";
    @FXML
    private TableView<Employee> tableView;
    @FXML
    private TableColumn<Employee, String> name, ntsaBadge, nationalID, residence, options, phoneNumber;
    @FXML
    private VBox container;
    @FXML
    private ChoiceBox<String> categoryChoiceBox;
    @FXML
    private Button addButton;
    private ObservableList<Employee> employees = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setUpTable();
        categoryChoiceBox.setItems(FXCollections.observableArrayList(CONDUCTORS, DRIVERS));
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
        searchEmployees();
        addButton.setDisable(!Main.userPermissions.get(Permission.REGISTER_EMPLOYEES));
    }

    private void filterResults(String employeeType) {
        //0:conductors
        //1:drivers
        ObservableList<Employee> filteredList = FXCollections.observableArrayList();
        if (employeeType.equals(CONDUCTORS)) {
            for (Employee employee : employees) {
                if (employee.getCategory() == Employee.Category.CONDUCTOR) {
                    filteredList.add(employee);
                }
            }
        } else {
            for (Employee employee : employees) {
                if (employee.getCategory() == Employee.Category.DRIVER) {
                    filteredList.add(employee);
                }
            }
        }
        tableView.setItems(filteredList);
    }

    private void searchEmployees() {
        Task<ObservableList<Employee>> task = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees");
            }
        };
        task.setOnSucceeded(event -> {
            employees = task.getValue();
            categoryChoiceBox.setValue(DRIVERS);
            filterResults(DRIVERS);
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        //drivers
        Label label = new Label("No employees found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().toString()));
        name.setCellFactory(param -> new TableCell<Employee, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                setWrapText(true);
                if (index >= 0 && index < tableView.getItems().size()) {
                    setText(tableView.getItems().get(index).toString());
                } else{
                    setText(null);
                }
            }
        });
        nationalID.setCellValueFactory(param -> param.getValue().nationalIdProperty());
        phoneNumber.setCellValueFactory(param -> param.getValue().phoneNumberProperty());
        residence.setCellValueFactory(param -> param.getValue().residenceProperty());
        ntsaBadge.setCellFactory(param -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Text text = new Text(getBadgeDetails(index));
                    text.wrappingWidthProperty().bind(ntsaBadge.widthProperty());
                    text.setStyle("-fx-padding: 5 5 5 5");
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                    setGraphic(text);
                } else {
                    setGraphic(null);
                }
            }

            private String getBadgeDetails(int index) {
                Employee employee = tableView.getItems().get(index);
                return " Badge No: " + employee.getLicenseNo() + "\n\n" +
                        " Issue Date: " + DateUtil.formatDate(employee.getLicenseIssueDate()) + "\n\n" +
                        " Expiry Date: " + DateUtil.formatDate(employee.getLicenseExpiryDate());
            }
        });
        options.setCellFactory(param -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        showEditEmployeeWindow(tableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.EDIT_EMPLOYEES));

                    Button delete = new Button("Delete");
                    delete.setOnAction(event -> {
                        deleteEmployee(tableView.getItems().get(index));
                    });
                    delete.setDisable(!Main.userPermissions.get(Permission.EDIT_EMPLOYEES));
                    delete.getStyleClass().add("btn-danger-outline");

                    Button documents = new Button("Documents");
                    documents.getStyleClass().add("btn-info-outline");
                    documents.setOnAction(event -> {
                        viewDocuments(tableView.getItems().get(index));
                    });
                    VBox vBox = new VBox(5.0, edit, documents, delete);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void viewDocuments(Employee employee) {
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
            controller.setEmployee(employee);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete selected employee? " +
                "This action cannot be undone. ", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DbUtil.executeStatement("delete from employees where national_id  = '" + employee.getNationalId() + "'")) {
                AlertUtil.showAlert("Delete Employee", " Employee has been successfully deleted from " +
                                "system.",
                        Alert.AlertType.INFORMATION);
                if (employee.getCategory() == Employee.Category.CONDUCTOR) {
                    DbUtil.saveActivityLog("Deleted conductor '" + employee.getFirstName() + " " + employee.getLastName() + "'");
                } else {
                    DbUtil.saveActivityLog("Deleted driver '" + employee.getFirstName() + " " + employee.getLastName
                            () + "'");
                }
                tableView.getItems().remove(employee);
            } else {
                AlertUtil.showGenericError();
            }

        }
    }

    @FXML
    private void onNewEmployee() {
        showEditEmployeeWindow(null);
    }

    private void showEditEmployeeWindow(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-employee.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);

            EditEmployeeController controller = loader.getController();
            controller.setStage(stage);
            controller.setEmployee(employee);
            stage.showAndWait();
            searchEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
