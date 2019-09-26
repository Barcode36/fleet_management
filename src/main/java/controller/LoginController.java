package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.model.User;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;

import java.util.HashMap;
import java.util.Map;

public class LoginController {
    private static final String ROOT_USER = "root";
    private boolean loginSuccessful = false;
    private Stage stage;

    public boolean loginSuccessful() {
        return loginSuccessful;
    }
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void loginUser() {
        if (validInput()) {
            if (DbUtil.getConnection(userNameField.getText(), passwordField.getText())) {
                User user = new User();
                if (userNameField.getText().equals(ROOT_USER)) {
                    user.setCategory(User.Category.ADMIN);
                    user.setFirstName("Admin");
                    user.setLastName("");
                    user.setUserId(1);
                    //root user by default has all permissions
                    Map<Permission, Boolean> permissionMap =new HashMap<>();
                    for (Permission permission : Permission.values()) {
                        permissionMap.put(permission, true);
                    }
                    Main.setUserPermissions(permissionMap);
                } else{
                    user = Dao.getUser(userNameField.getText());
                    if (user == null) {
                        AlertUtil.showAlert("Login", "The user name does not exist", Alert.AlertType.ERROR);
                        return;
                    }
                    Main.setUserPermissions(Dao.getPermissionsMap(user.getUserId()));
                }
                loginSuccessful = true;
                Main.setUser(user);
                AlertUtil.showAlert("Login", "Successfully logged in as " + userNameField.getText(), Alert.AlertType.INFORMATION);
                getStage().close();

            } else{
                AlertUtil.showAlert("Login Error", "An error occurred while attempting to login", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validInput() {
        String errorMsg = "";
        if (userNameField.getText() == null || userNameField.getText().isEmpty()) {
            errorMsg += "Invalid user name!\n";
        }
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errorMsg += "Invalid password!";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Error", errorMsg, Alert.AlertType.ERROR);

        return false;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
