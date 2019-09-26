package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Document;
import main.java.model.Employee;
import main.java.model.FleetOwner;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DocumentsController {
    private final int VERTICAL_MARGIN = 110;
    private final int HORIZONTAL_MARGIN = 160;
    private final int DEFAULT_DIMENSIONS = 200;
    @FXML
    private TableView<Document> tableView;
    @FXML
    private TableColumn<Document, String> name, options;
    @FXML
    private Label dialogTitle;
    @FXML
    private VBox container;
    private String regNumber;
    private Employee employee;
    private FleetOwner fleetOwner;
    private Stage stage;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        Label label = new Label("No documents found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        name.setCellValueFactory(param -> param.getValue().nameProperty());
        options.setCellFactory(param -> new TableCell<Document, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button view = new Button("View");
                    view.getStyleClass().add("btn-info-outline");
                    view.setOnAction(event -> {
                        viewFile(tableView.getItems().get(index));
                    });
                    Button delete = new Button("Delete");
                    delete.setOnAction(event -> {
                        deleteFile(tableView.getItems().get(index));
                    });
                    if (fleetOwner != null) {
                        delete.setDisable(!Main.userPermissions.get(Permission.EDIT_FLEET_OWNERS));
                    } else if (employee != null) {
                        delete.setDisable(!Main.userPermissions.get(Permission.EDIT_EMPLOYEES));
                    } else if (regNumber != null) {
                        delete.setDisable(!Main.userPermissions.get(Permission.EDIT_VEHICLES));
                    }
                    delete.getStyleClass().add("btn-danger-outline");
                    HBox hBox = new HBox(5.0, view, delete);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void deleteFile(Document document) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete document?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DbUtil.executeStatement("delete from documents where id = " + document.getId())) {
                if (regNumber != null) {
                    DbUtil.saveActivityLog("Deleted " + document.getName() + " for vehicle '" + regNumber + "'");
                } else if (employee != null) {
                    DbUtil.saveActivityLog("Deleted " + document.getName() + " for employee '" + employee
                            .getFirstName() + " " + employee.getLastName() + "'");
                } else {
                    DbUtil.saveActivityLog("Deleted " + document.getName() + " for fleet owner '" + fleetOwner
                            .getFirstName() + " " + fleetOwner.getLastName() + "'");
                }

                AlertUtil.showAlert("", "Document successfully deleted", Alert.AlertType.INFORMATION);
                tableView.getItems().remove(document);
            } else {
                AlertUtil.showGenericError();
            }
        }
    }

    private void viewFile(Document document) {
        try {
            int image_height = document.getHeight() != 0 ? document.getHeight() : DEFAULT_DIMENSIONS;
            int image_width = document.getWidth() != 0 ? document.getWidth() : DEFAULT_DIMENSIONS;
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPrefSize(image_width + HORIZONTAL_MARGIN * 2, VERTICAL_MARGIN * 2 + image_height);
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(image_width);
            imageView.setFitHeight(image_height);
            vBox.getChildren().add(imageView);

            //get image from database
            ResultSet resultSet = DbUtil.executeQuery("select file from documents where id = " + document.getId());
            if (resultSet != null && resultSet.next()) {
                if (resultSet.getBlob("file") != null) {
                    InputStream inputStream = resultSet.getBlob("file").getBinaryStream();
                    imageView.setImage(new Image(inputStream));
                }
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(vBox));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setTitle(document.getName());
            stage.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        dialogTitle.setText(regNumber + " Documents");
        getDocuments(regNumber);
    }



    private void getDocuments(String ownerId) {
        Task<ObservableList<Document>> task = new Task<ObservableList<Document>>() {
            @Override
            protected ObservableList<Document> call() throws Exception {
                return Dao.getDocuments(ownerId);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        dialogTitle.setText(employee.getFirstName() + "'s Documents");
        getDocuments(employee.getNationalId());
    }

    void setFleetOwner(FleetOwner fleetOwner) {
        this.fleetOwner = fleetOwner;
        dialogTitle.setText(fleetOwner.getFirstName() + "'s Documents");
        getDocuments(fleetOwner.getNationalId());
    }
}
