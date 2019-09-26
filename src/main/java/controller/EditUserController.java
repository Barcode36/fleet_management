package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.model.User;
import main.java.model.UserPermission;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EditUserController {
    @FXML
    private Button deleteButton, resetPasswordButton;
    @FXML
    private TextField firstName, lastName, loginName;
    @FXML
    private PasswordField passwordField, confirmPasswordField;
    @FXML
    private ChoiceBox<User.Category> categoryChoiceBox;
    @FXML
    private ListView<UserPermission> listView;
    @FXML
    private VBox container;
    private User user;
    private Stage stage;
    private ManageUsersController context;

    @FXML
    private void initialize() {
        for (Permission permission : Permission.values()) {
            listView.getItems().add(new UserPermission(permission, false));
        }
        listView.setCellFactory(CheckBoxListCell.forListView(UserPermission::allowedProperty));

        categoryChoiceBox.setItems(FXCollections.observableArrayList(User.Category.values()));
        categoryChoiceBox.getSelectionModel().select(User.Category.REGULAR);
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == User.Category.ADMIN) {
                for (UserPermission userPermission : listView.getItems()) {
                    userPermission.setAllowed(true);
                }
            }
        });
        deleteButton.setDisable(!Main.userPermissions.get(Permission.CREATE_USERS));
        deleteButton.setVisible(false);

        resetPasswordButton.setDisable(!Main.userPermissions.get(Permission.CREATE_USERS));
        resetPasswordButton.setVisible(false);

        listView.setDisable(!Main.userPermissions.get(Permission.CHANGE_USER_PERMISSIONS));
    }

    @FXML
    private void onSaveUserInfo() {
        if (validInput()) {
            if (user == null) {
                //new user
                if (!DbUtil.createUser(loginName.getText(), passwordField.getText())) {
                    AlertUtil.showGenericError();
                    return;
                }
            }
            user = getUser();
            DbUtil.saveUser(user);
            DbUtil.saveUserPermissions(listView.getItems(), user.getUserId());
            if (getCreateUserPermission()) {
                DbUtil.enableCreateUserPermission(user.getUserName());
            }
            if (!passwordField.isDisabled()) {
                //new user
                DbUtil.saveActivityLog("Created user " + user.toString());
            } else {
                DbUtil.saveActivityLog("Modified user " + user.toString());
            }
            AlertUtil.showAlert("", "User details have been successfully saved!", Alert.AlertType.INFORMATION);
            stage.close();
        }
    }

    private boolean getCreateUserPermission() {
        for (UserPermission userPermission : listView.getItems()) {
            if (userPermission.getPermission() == Permission.CREATE_USERS) {
                return userPermission.isAllowed();
            }
        }
        return false;
    }

    private User getUser() {
        User user = new User();
        if (this.user != null) {
            user.setUserId(this.user.getUserId());
        } else {
            user.setUserId(DbUtil.getNextAutoincrementValue("users"));
        }
        user.setFirstName(firstName.getText());
        user.setCategory(categoryChoiceBox.getValue());
        user.setLastName(lastName.getText());
        user.setUserName(loginName.getText());

        return user;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (firstName.getText() == null || firstName.getText().isEmpty()) {
            errorMsg += "First name is required!\n";
        }
        if (lastName.getText() == null || lastName.getText().isEmpty()) {
            errorMsg += "Last name is required!\n";
        }

        if (loginName.getText() == null || loginName.getText().isEmpty()) {
            errorMsg += "Login name is required!\n";
        } else if (user == null && context.containsUserName(loginName.getText())) {
            errorMsg += "Login name already used!\n";
        }

        if (user == null) {
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                errorMsg += "Password is required!\n";
            } else if (!confirmPasswordField.getText().equals(passwordField.getText())) {
                errorMsg += "Passwords do not match!\n";
            }
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onDeleteUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete user '" + user.getUserName() + "'?", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DbUtil.executeStatement("delete from users where user_id = " + user.getUserId())) {
                if (DbUtil.executeStatement("delete from user_permissions where user_id = " + user.getUserId())) {

                    ResultSet resultSet = DbUtil.executeQuery("select user from mysql.user where user = '" + user.getUserName() + "'");
                    try {
                        if (resultSet != null && resultSet.next()) {
                            DbUtil.executeStatement("drop user '" + user.getUserName() + "'@'localhost'");
                        }
                    } catch (SQLException e) {
                        AlertUtil.showGenericError();
                    }
                    AlertUtil.showAlert("", "User '" + user.getUserName() + "' successfully deleted", Alert.AlertType.INFORMATION);
                    DbUtil.saveActivityLog("Deleted user " + user.toString());
                    stage.close();
                } else {
                    AlertUtil.showGenericError();
                }
            } else {
                AlertUtil.showGenericError();
            }
            stage.close();
        }
    }

    @FXML
    private void onResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/change_password.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            ChangePasswordController controller = loader.getController();
            controller.setStage(stage);
            controller.setUser(user);
            stage.showAndWait();

        } catch (IOException ignored) {
        }
    }

    @FXML
    private void onCloseDialog() {
        stage.close();
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            setUserDetails();
            getUserPermissions();
            deleteButton.setVisible(user.getUserId() != Main.currentUser.getUserId());
            resetPasswordButton.setVisible(true);
        }
    }

    private void setUserDetails() {
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        loginName.setText(user.getUserName());
        categoryChoiceBox.getSelectionModel().select(user.getCategory());
        passwordField.setDisable(true);
        loginName.setDisable(true);
        confirmPasswordField.setDisable(true);
        passwordField.setDisable(true);
    }

    private void getUserPermissions() {
        Task<ObservableList<UserPermission>> task = new Task<ObservableList<UserPermission>>() {
            @Override
            protected ObservableList<UserPermission> call() throws Exception {
                return Dao.getUserPermissions(user.getUserId());
            }
        };
        task.setOnSucceeded(event -> {
            listView.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setContext(ManageUsersController context) {
        this.context = context;
    }
}
