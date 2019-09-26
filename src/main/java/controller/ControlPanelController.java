package main.java.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;

import java.io.IOException;

public class ControlPanelController {

    private String regNumber;
    @FXML
    private VBox container;
    @FXML
    private Label regNumberLabel;
    @FXML
    private Button annualInspection, hireOut, renewLicense, recordIncomeExpense, recordAccident, servicing, renewInsurance, renewSpeedGovernor, operationHistory;
    @FXML
    private FlowPane controlPanel;

    @FXML
    private void initialize() {
        for (Node node : controlPanel.getChildren()) {
            node.setOnMouseClicked(event -> {
                handleClicked((Button) node);
            });
        }
        //permissions
        for (Button button : new Button[] {annualInspection, renewLicense, servicing, renewSpeedGovernor}) {
            button.setDisable(!Main.userPermissions.get(Permission.RECORD_RENEWAL_DATES));
        }
        recordIncomeExpense.setDisable(!Main.userPermissions.get(Permission.RECORD_INCOME));
        recordAccident.setDisable(!Main.userPermissions.get(Permission.RECORD_ACCIDENT));
        renewInsurance.setDisable(!Main.userPermissions.get(Permission.MODIFY_INSURANCE));
        hireOut.setDisable(!Main.userPermissions.get(Permission.ASSIGN_EMPLOYEES));
    }

    private void handleClicked(Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + button.getId() + ".fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);

            if (button == recordIncomeExpense) {
                RecordIncomeExpenseController controller = loader.getController();
                controller.setRegNumber(regNumber);
            } else if (button == recordAccident) {
                EditAccidentController controller = loader.getController();
                controller.setStage(stage);
                controller.setRegNumber(regNumber);
            } else if (button == hireOut) {
                HireOutVehicleController controller = loader.getController();
                controller.setStage(stage);
                controller.setRegNumber(regNumber);
            } else if (button == operationHistory) {
                OperationHistoryController controller = loader.getController();
                controller.setRegNumber(regNumber);
            } else if (button == annualInspection) {
                AnnualInspectionController controller = loader.getController();
                controller.setRegNumber(regNumber);
                controller.setStage(stage);
            } else if (button == renewSpeedGovernor) {
                RenewSpeedGovController controller = loader.getController();
                controller.setRegNumber(regNumber);
                controller.setStage(stage);
            } else if (button == servicing) {
                ServicingController controller = loader.getController();
                controller.setStage(stage);
                controller.setRegNumber(regNumber);
            } else if (button == renewLicense) {
                RenewLicenseController controller = loader.getController();
                controller.setStage(stage);
                controller.setRegNumber(regNumber);
            } else if (button == renewInsurance) {
                RenewInsuranceController controller = loader.getController();
                controller.setStage(stage);
                controller.setRegNumber(regNumber);
            }

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        regNumberLabel.setText(regNumber);
    }
}
