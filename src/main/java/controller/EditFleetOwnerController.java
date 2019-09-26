package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.model.FleetOwner;
import main.java.util.AlertUtil;
import main.java.util.DbUtil;

public class EditFleetOwnerController {
    private Stage stage;
    @FXML
    private VBox container;
    @FXML
    private TextField nationalId, firstName, middleName, lastName, residence, occupation, phoneNumber;

    @FXML
    private void onSaveDetails() {
        if (validInput()) {
            FleetOwner fleetOwner = createFleetOwner();
            if (DbUtil.saveFleetOwner(fleetOwner)) {
                AlertUtil.showAlert("", "Fleet owner details successfully saved!", Alert.AlertType.INFORMATION);
                if (nationalId.isEditable()) {
                    DbUtil.saveActivityLog("Created fleet owner '" + fleetOwner.toString() + "'");
                } else{
                    DbUtil.saveActivityLog("Changed details of fleet owner '" + fleetOwner.toString() + "'");
                }
                stage.close();
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    @FXML
    private void onUploadDocuments() {
        if (nationalId.getText() == null ||nationalId.getText().isEmpty()) {
            AlertUtil.showAlert("", "Please save fleet owner details first!", Alert.AlertType.ERROR);
        } else{
            new UploadDocumentController().showUploadDocumentDialog(null, nationalId.getText(), container);
        }
    }

    private FleetOwner createFleetOwner() {
        FleetOwner fleetOwner = new FleetOwner();
        fleetOwner.setPhoneNumber(phoneNumber.getText());
        fleetOwner.setMiddleName(middleName.getText());
        fleetOwner.setResidence(residence.getText());
        fleetOwner.setOccupation(occupation.getText());
        fleetOwner.setLastName(lastName.getText());
        fleetOwner.setFirstName(firstName.getText());
        fleetOwner.setNationalId(nationalId.getText());
        return fleetOwner;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (nationalId.getText() == null || nationalId.getText().isEmpty()) {
            errorMsg += "National ID is required!\n";
        }
        if (firstName.getText() == null || firstName.getText().isEmpty()) {
            errorMsg += "First name is required!\n";
        }
        if (lastName.getText() == null || lastName.getText().isEmpty()) {
            errorMsg += "Last name is required!\n";
        }
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void setFleetOwner(FleetOwner fleetOwner) {
        if (fleetOwner != null) {
            setDetails(fleetOwner);
        }
    }

    private void setDetails(FleetOwner fleetOwner) {
        nationalId.setText(fleetOwner.getNationalId());
        nationalId.setEditable(false);
        firstName.setText(fleetOwner.getFirstName());
        middleName.setText(fleetOwner.getMiddleName());
        lastName.setText(fleetOwner.getLastName());
        occupation.setText(fleetOwner.getOccupation());
        residence.setText(fleetOwner.getResidence());
        phoneNumber.setText(fleetOwner.getPhoneNumber());
    }

}

