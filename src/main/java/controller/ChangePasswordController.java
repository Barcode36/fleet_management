package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import main.java.model.User;
import main.java.util.AlertUtil;
import main.java.util.DbUtil;

/**
 * Created by alfonce on 01/08/2017.
 */
public class ChangePasswordController {

    private Stage stage;
    private User user;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private Label prompt;

    @FXML
    private void onSave() {
        String alertMsg = "";
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password == null || password.isEmpty()) {
            alertMsg += "Password is required!\n";
        } else if (!password.equals(confirmPassword)) {
            alertMsg += "Passwords do not match!";
        }
        if (alertMsg.isEmpty()) {
            String sql = "SET PASSWORD FOR '" + user.getUserName() + "'@'localhost' = PASSWORD('" + password + "')";
            if (DbUtil.executeStatement(sql)) {
                AlertUtil.showAlert("Password Reset",  "Password for '" + user.getUserName() + "' has been successfully reset!", Alert.AlertType.INFORMATION);
                DbUtil.saveActivityLog("Created new password for user " + user.toString());
                onCancel();

            } else {
               AlertUtil.showGenericError();
            }
        } else {
            AlertUtil.showAlert("Error", alertMsg, Alert.AlertType.ERROR);
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setUser(User user) {
        this.user = user;
        prompt.setText("Enter new password for '" + user.getUserName() + "'");
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

}
