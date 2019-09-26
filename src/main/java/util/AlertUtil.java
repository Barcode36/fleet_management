package main.java.util;

import javafx.scene.control.Alert;

public class AlertUtil {
    public static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showGenericError() {
        showAlert("Error", "An error occurred while attempting to save details", Alert.AlertType.ERROR);
    }
}
