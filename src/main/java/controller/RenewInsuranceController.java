package main.java.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.model.InsurancePolicyType;
import main.java.model.ScheduledAction;
import main.java.model.VehicleInsurance;
import main.java.util.AlertUtil;
import main.java.util.CurrencyUtil;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.sql.ResultSet;
import java.time.LocalDate;

public class RenewInsuranceController {
    private Stage stage;
    private String regNumber;

    @FXML
    private Label title;
    @FXML
    private HBox currentExpiry;
    @FXML
    private TextField insuranceCompany, policyNumber, premiumAmount;
    @FXML
    private DatePicker startDate, expiryDate;
    @FXML
    private ChoiceBox<InsurancePolicyType> policyTypeChoiceBox;

    @FXML
    private void initialize() {
        startDate.setConverter(DateUtil.getDatePickerConverter());
        expiryDate.setConverter(DateUtil.getDatePickerConverter());
        policyTypeChoiceBox.setItems(FXCollections.observableArrayList(InsurancePolicyType.values()));
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            if (DbUtil.saveInsuranceDetails(getInsurance(), regNumber) && DbUtil.scheduleNextAction(regNumber, ScheduledAction.INSURANCE, expiryDate.getValue())) {
                DbUtil.saveActivityLog("Updated vehicle insurance { Vehicle: " + regNumber + ", Expiry Date: " + DateUtil.formatDateLong(expiryDate.getValue()) + " }");
                AlertUtil.showAlert("Insurance", "Insurance details for vehicle '" + regNumber + "' have been successfully saved!", Alert.AlertType.INFORMATION);
                onClose();
            } else{
                AlertUtil.showGenericError();
            }
        }
    }

    private VehicleInsurance getInsurance() {
        VehicleInsurance insurance = new VehicleInsurance();
        insurance.setExpiryDate(expiryDate.getValue());
        insurance.setPolicyType(policyTypeChoiceBox.getValue());
        insurance.setPolicyNumber(policyNumber.getText());
        insurance.setCompanyName(insuranceCompany.getText());
        insurance.setPremiumAmount(CurrencyUtil.parseCurrency(premiumAmount.getText()));
        insurance.setStartDate(startDate.getValue());
        return insurance;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (insuranceCompany.getText() == null || insuranceCompany.getText().isEmpty()) {
            errorMsg += "Insurance company required!\n";
        }
        if (startDate.getValue() == null) {
            errorMsg += "Policy start date required!\n";
        }
        if (expiryDate.getValue() == null) {
            errorMsg += "Policy expiry date required!\n";
        }
        if (startDate.getValue() != null && expiryDate.getValue()!= null) {
            if (expiryDate.getValue().isBefore(startDate.getValue())) {
                errorMsg += "Expiry date should be after start date!\n";
            }
        }
        if (policyTypeChoiceBox.getValue() == null) {
            errorMsg += "Policy type required!\n";
        }
        if (policyNumber.getText() == null || policyNumber.getText().isEmpty()) {
            errorMsg += "Policy number required!\n";
        }
        if (CurrencyUtil.parseCurrency(premiumAmount.getText()) == -1) {
            errorMsg += "Invalid premium amount!\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    @FXML
    private void onClose() {
        stage.close();
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
        title.setText(regNumber + " Insurance");
        getInsuranceDetails();
    }

    private void getInsuranceDetails() {
        Task<VehicleInsurance> task = new Task<VehicleInsurance>() {
            @Override
            protected VehicleInsurance call() throws Exception {
                ResultSet resultSet = DbUtil.executeQuery("select * from vehicle_insurance where reg_number = '" + regNumber + "'");
                if (resultSet != null && resultSet.next()) {
                    VehicleInsurance insurance = new VehicleInsurance();
                    insurance.setCompanyName(resultSet.getString("company"));
                    insurance.setPolicyNumber(resultSet.getString("policy_number"));
                    insurance.setPolicyType(InsurancePolicyType.valueOf(resultSet.getString("policy_type")));
                    insurance.setExpiryDate(resultSet.getObject("expiry_date", LocalDate.class));
                    insurance.setPremiumAmount(resultSet.getDouble("premium_amount"));
                    return insurance;
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            VehicleInsurance insurance = task.getValue();
            if (insurance != null) {
                insuranceCompany.setText(insurance.getCompanyName());
                policyTypeChoiceBox.getSelectionModel().select(insurance.getPolicyType());
                policyNumber.setText(insurance.getPolicyNumber());
                premiumAmount.setText(CurrencyUtil.formatCurrency(insurance.getPremiumAmount()));
                Label label = new Label("Current expiry date is ");
                currentExpiry.getChildren().add(label);
                label = new Label(DateUtil.formatDateLong(insurance.getExpiryDate()));
                label.getStyleClass().add("fw-500");
                currentExpiry.getChildren().add(label);
            }
        });
        new Thread(task).start();
    }
}
