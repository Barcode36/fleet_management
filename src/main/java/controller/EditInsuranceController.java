package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.util.AlertUtil;
import main.java.util.DbUtil;

public class EditInsuranceController {
    @FXML
    private TextField editField;
    private Stage stage;
    private String insurance;

    @FXML
    private void onSave() {
        if (editField.getText() != null && !editField.getText().isEmpty()) {
            if (DbUtil.saveInsuranceCompany(editField.getText())) {
                if (insurance == null) {
                    //new
                    DbUtil.saveActivityLog("Created insurance '" + editField.getText() + "'");
                } else if (!insurance.toLowerCase().equals(editField.getText().toLowerCase())) {
                    DbUtil.saveActivityLog("Changed insurance '" + insurance + "' to '" + editField.getText() + "'");
                }
                AlertUtil.showAlert("Success", "Insurance company name has been successfully saved!", Alert.AlertType.INFORMATION);
                stage.close();
            } else {
                AlertUtil.showGenericError();
            }
        } else {
            AlertUtil.showAlert("Name Required", "Name of insurance company required!", Alert.AlertType.ERROR);
        }
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
        editField.setText(insurance);

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

