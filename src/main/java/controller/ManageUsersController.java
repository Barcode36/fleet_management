package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.model.User;
import main.java.util.Dao;

import java.io.IOException;

public class ManageUsersController {
    @FXML
    private VBox container;
    @FXML
    private TableView<User> tableView;
    @FXML
    private TableColumn<User, String> firstName, userName, lastName, category, options;
    @FXML
    private Button createUserBtn;

    @FXML
    private void initialize() {
        setUpTable();
        getUsers();
        createUserBtn.setDisable(!Main.userPermissions.get(Permission.CREATE_USERS));
    }

    private void getUsers() {
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return Dao.getUsers();
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if(column == options) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3));
            } else{
                column.prefWidthProperty().bind(tableView.widthProperty().divide(6));
            }
        }
        firstName.setCellValueFactory(param -> param.getValue().firstNameProperty());
        userName.setCellValueFactory(param -> param.getValue().userNameProperty());
        lastName.setCellValueFactory(param -> param.getValue().lastNameProperty());
        category.setCellValueFactory(param -> param.getValue().categoryStringProperty());
        options.setCellFactory(param -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setOnAction(event -> {
                        editUser(tableView.getItems().get(index));
                    });
                    edit.setDisable(!Main.userPermissions.get(Permission.CREATE_USERS));

                    //todo : option to view user activity
                    Button userActivity = new Button("Activity Log");
                    userActivity.getStyleClass().add("btn-info-outline");
                    userActivity.setOnAction(event -> {
                        viewUserActivity(tableView.getItems().get(index));
                    });
                    userActivity.setDisable(tableView.getItems().get(index).getUserId() != Main.currentUser.getUserId() && !Main.userPermissions.get(Permission.VIEW_USER_ACTIVITY));
                    HBox hBox = new HBox(5.0, edit, userActivity);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else{
                    setGraphic(null);
                }
            }
        });
    }

    private void viewUserActivity(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/user-activity.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setScene(new Scene(loader.load()));
            UserActivityController controller = loader.getController();
            controller.setUser(user);
            stage.show();
        } catch (IOException ignored) {}
    }

    private void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/edit-user.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            EditUserController controller = loader.getController();
            controller.setUser(user);
            controller.setStage(stage);
            controller.setContext(this);
            stage.showAndWait();
            getUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewUser() {
        editUser(null);
    }

    boolean containsUserName(String username) {
        for (User user : tableView.getItems()) {
            if (user.getUserName().toLowerCase().equals(username.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

