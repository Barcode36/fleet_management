package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.*;
import main.java.util.*;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleRegistrationController {
    private Loan loan;
    private ToggleGroup loanToggleGroup;
    @FXML
    private ScrollPane container;
    @FXML
    private ChoiceBox<FleetCategory> fleetCategoryChoiceBox;
    @FXML
    private ChoiceBox<FleetOwner> fleetOwnerChoiceBox;
    @FXML
    private ChoiceBox<String> insuranceCompanyChoiceBox;
    @FXML
    private ChoiceBox<String> vehicleMakeChoiceBox;
    @FXML
    private ChoiceBox<VehicleType> vehicleTypeChoiceBox;
    @FXML
    private ChoiceBox<InsurancePolicyType> policyTypeChoiceBox;
    @FXML
    private TextField model, numSeats, editRegNumber, policyNumber, cost, premiumAmount;
    @FXML
    private DatePicker regDate, policyStartDatePicker, policyExpiryDatePicker, inspectionDatePicker, servicingDatePicker, speedGovRenewalDatePicker, licenseRenewalDatePicker;
    @FXML
    private RadioButton onLoan, notOnLoan;
    @FXML
    private Button uploadLogBookBtn, uploadInsuranceCert;
    private VehicleListController context;
    private String regNumber;
    private Vehicle vehicle;

    @FXML
    private void initialize() {
        //loan or not
        loanToggleGroup = new ToggleGroup();
        loanToggleGroup.getToggles().addAll(onLoan, notOnLoan);
        loanToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            //TODO disable editing of loan interest, principle loan repayment etc if notOnLoan selected
            RadioButton radioButton = (RadioButton) newValue;
            if (radioButton != null && radioButton.getId().equals(onLoan.getId())) {
                showLoanDetailsWindow();
            }
        });
        loanToggleGroup.selectToggle(notOnLoan);

        for (DatePicker datePicker : new DatePicker[]{regDate, policyExpiryDatePicker, policyStartDatePicker, inspectionDatePicker, servicingDatePicker, speedGovRenewalDatePicker, licenseRenewalDatePicker}) {
            datePicker.setConverter(DateUtil.getDatePickerConverter());
            datePicker.setPromptText("dd-mm-yyyy");
        }
        fleetCategoryChoiceBox.setItems(FXCollections.observableArrayList(FleetCategory.values()));
        vehicleTypeChoiceBox.setItems(FXCollections.observableArrayList(VehicleType.values()));
        policyTypeChoiceBox.setItems(FXCollections.observableArrayList(InsurancePolicyType.values()));
        getInsuranceCompanies();
        getVehicleModels();
        getFleetOwners();
        getVehicleMakes();
        uploadInsuranceCert.setDisable(!Main.userPermissions.get(Permission.UPLOAD_DOCUMENTS));
        uploadInsuranceCert.setOnAction(event -> {
            onUploadDoc(uploadInsuranceCert.getId());
        });
        uploadLogBookBtn.setDisable(!Main.userPermissions.get(Permission.UPLOAD_DOCUMENTS));
        uploadLogBookBtn.setOnAction(event -> {
            onUploadDoc(uploadLogBookBtn.getId());
        });
    }
    private void getVehicleMakes() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                return Dao.getVehicleBrands();
            }
        };
        task.setOnSucceeded(event -> {
            vehicleMakeChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getInsuranceCompanies() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                return Dao.getInsuranceCompanies();
            }
        };
        task.setOnSucceeded(event -> {
            insuranceCompanyChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    private void getVehicleModels() {
        Task<ObservableList<String>> modelsListTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                return Dao.getVehicleModels();
            }
        };
        modelsListTask.setOnSucceeded(event -> {
            TextFields.bindAutoCompletion(model, modelsListTask.getValue());
        });
        new Thread(modelsListTask).start();
    }

    private void getFleetOwners() {
        Task<ObservableList<FleetOwner>> task = new Task<ObservableList<FleetOwner>>() {
            @Override
            protected ObservableList<FleetOwner> call() {
                return Dao.getFleetOwners();
            }
        };
        task.setOnSucceeded(event -> {
            fleetOwnerChoiceBox.setItems(task.getValue());
            if (vehicle != null) {
                String sql = "select distinct national_id from fleet_owners " +
                        "inner join vehicles on vehicles.owner_id = fleet_owners.national_id " +
                        "where reg_number = '" + vehicle.getRegistrationNum() + "'";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                try {
                    if (resultSet != null && resultSet.next()) {
                        for (FleetOwner fleetOwner : fleetOwnerChoiceBox.getItems()) {
                            if (fleetOwner.getNationalId().equals(resultSet.getString("national_id"))) {
                                fleetOwnerChoiceBox.setValue(fleetOwner);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        new Thread(task).start();
    }

    private void showLoanDetailsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/loan-details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());
            stage.setTitle("Loan Details");

            EditLoanDetailsController controller = loader.getController();
            controller.setStage(stage);
            if (regNumber != null) {
                controller.setRegNumber(regNumber);
            }

            stage.showAndWait();
            loan = controller.getLoan();
            if (loan == null) {
                loanToggleGroup.selectToggle(notOnLoan);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onUploadDoc(String fileName) {
        if (editRegNumber.getText() == null || editRegNumber.getText().isEmpty()) {
            AlertUtil.showAlert("", "Registration Number of vehicle required!", Alert.AlertType.ERROR);
        } else{
            new UploadDocumentController().showUploadDocumentDialog(fileName, editRegNumber.getText(), container);
        }
    }

    @FXML
    private void onSaveDetails() {
        if (validInput()) {
            boolean error = !saveVehicle() || !saveInsurance() || !saveLoanDetails();
            if (error) {
                AlertUtil.showAlert("Registration Error", "An error occurred while attempting to save details.", Alert.AlertType.ERROR);
            } else {
                AlertUtil.showAlert("Registration Success", "Vehicle details have been successfully saved!", Alert.AlertType.INFORMATION);
                scheduleServices();
                if (context != null) {
                    context.exitEditMode();
                }
                if (regNumber == null) {
                    DbUtil.saveActivityLog("Registered vehicle '" + editRegNumber.getText() + "'");
                } else {
                    DbUtil.saveActivityLog("Changed details of vehicle '" + regNumber + "'");
                }
            }
        }
    }

    private void scheduleServices() {
        if (inspectionDatePicker.getValue() != null) {
            DbUtil.scheduleNextAction(editRegNumber.getText(), ScheduledAction.INSPECTION, inspectionDatePicker.getValue().plusYears(1));
        }
        if (speedGovRenewalDatePicker.getValue() != null) {
            DbUtil.scheduleNextAction(editRegNumber.getText(), ScheduledAction.SPEED_GOV_RENEWAL, speedGovRenewalDatePicker.getValue().plusYears(1));
        }
        if (licenseRenewalDatePicker.getValue() != null) {
            DbUtil.scheduleNextAction(editRegNumber.getText(), ScheduledAction.NTSA_LICENSE_RENEWAL, licenseRenewalDatePicker.getValue().plusYears(1));
        }
        DbUtil.scheduleNextAction(editRegNumber.getText(), ScheduledAction.INSURANCE, policyExpiryDatePicker.getValue());
        if (servicingDatePicker.getValue() != null) {
            DbUtil.scheduleNextAction(editRegNumber.getText(), ScheduledAction.SERVICING, servicingDatePicker.getValue().plusDays(21));
        }
    }

    private boolean saveLoanDetails() {
        if (loanToggleGroup.getSelectedToggle() == onLoan) {
            return DbUtil.saveLoanDetails(loan, editRegNumber.getText());
        }
        return true;
    }

    private boolean saveInsurance() {
        VehicleInsurance insurance = new VehicleInsurance();
        insurance.setPolicyType(policyTypeChoiceBox.getValue());
        insurance.setPolicyNumber(policyNumber.getText());
        insurance.setStartDate(policyStartDatePicker.getValue());
        insurance.setExpiryDate(policyExpiryDatePicker.getValue());
        insurance.setCompanyName(insuranceCompanyChoiceBox.getValue());
        insurance.setPremiumAmount(CurrencyUtil.parseCurrency(premiumAmount.getText()));
        return DbUtil.saveInsuranceDetails(insurance, editRegNumber.getText());
    }

    private boolean saveVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(vehicleTypeChoiceBox.getValue());
        vehicle.setPreviousInspectionDate(inspectionDatePicker.getValue());
        vehicle.setPreviousNTSALicenseRenewal(licenseRenewalDatePicker.getValue());
        vehicle.setRegistrationDate(regDate.getValue());
        vehicle.setModel(model.getText());
        vehicle.setMake(vehicleMakeChoiceBox.getValue());
        vehicle.setRegistrationNum(editRegNumber.getText());
        vehicle.setNumSeats(NumberUtil.stringToInt(numSeats.getText()));
        if (vehicle.getNumSeats() == -1) {
            vehicle.setNumSeats(0);
        }
        vehicle.setOwnerId(fleetOwnerChoiceBox.getValue().getNationalId());
        vehicle.setCost(CurrencyUtil.parseCurrency(cost.getText()));
        vehicle.setPreviousSpeedGovernorRenewal(speedGovRenewalDatePicker.getValue());
        vehicle.setPurchasedOnLoan(loanToggleGroup.getSelectedToggle() == onLoan);
        vehicle.setCategory(fleetCategoryChoiceBox.getValue());

        return DbUtil.saveVehicleDetails(vehicle);
    }

    private boolean validInput() {
        String errorMsg = "";
        if (fleetCategoryChoiceBox.getValue() == null) {
            errorMsg += "Fleet category required!\n";
        }
        if (vehicleTypeChoiceBox.getValue() == null) {
            errorMsg += "Vehicle type required!\n";
        }

        if (fleetOwnerChoiceBox.getValue() == null) {
            errorMsg += "Fleet owner required!\n";
        }

        if (vehicleMakeChoiceBox.getValue() == null) {
            errorMsg += "Vehicle make required!\n";
        }
        if (editRegNumber.getText() == null || editRegNumber.getText().isEmpty()) {
            errorMsg += "Registration number required!\n";
        }
        if (regDate.getValue() == null) {
            errorMsg += "Registration date required!\n";
        }

        if (numSeats.getText() != null && !numSeats.getText().isEmpty()) {
            if (NumberUtil.stringToInt(numSeats.getText()) == -1) {
                errorMsg += "Invalid number of seats!\n";
            }
        }

        if (insuranceCompanyChoiceBox.getValue() == null) {
            errorMsg += "Insurance company required!\n";
        }
        if (policyTypeChoiceBox.getValue() == null) {
            errorMsg += "Insurance policy type required!\n";
        }
        if (policyStartDatePicker.getValue() == null) {
            errorMsg += "Insurance policy start date required!\n";
        }
        if (policyExpiryDatePicker.getValue() == null) {
            errorMsg += "Insurance policy expiry date required!\n";
        }
        if (policyNumber.getText() == null || policyNumber.getText().isEmpty()) {
            errorMsg += "Insurance policy number required!\n";
        }
        if (CurrencyUtil.parseCurrency(premiumAmount.getText()) == -1) {
            errorMsg += "Premium amount is required!\n";
        }
        if (CurrencyUtil.parseCurrency(cost.getText()) <= 0) {
            errorMsg += "Invalid cost!\n";
        }

        //TODO : more required fields
        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    void setContext(VehicleListController context) {
        this.context = context;
    }

    public void setRegNumber(String regNumber) {

        this.regNumber = regNumber;
        Task<Vehicle> task = new Task<Vehicle>() {
            @Override
            protected Vehicle call() {
                return Dao.getVehicleDetails(regNumber);
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                setVehicleDetails(task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void setVehicleDetails(Vehicle vehicle) {
        this.vehicle = vehicle;
        editRegNumber.setText(vehicle.getRegistrationNum());
        editRegNumber.setEditable(false);
        regDate.setValue(vehicle.getRegistrationDate());
        licenseRenewalDatePicker.setValue(vehicle.getPreviousNTSALicenseRenewal());
        fleetCategoryChoiceBox.getSelectionModel().select(vehicle.getCategory());
        vehicleTypeChoiceBox.getSelectionModel().select(vehicle.getType());
        vehicleMakeChoiceBox.getSelectionModel().select(vehicle.getMake());
        model.setText(vehicle.getModel());
        numSeats.setText(vehicle.getNumSeats() + "");

        insuranceCompanyChoiceBox.getSelectionModel().select(vehicle.getInsurance().getCompanyName());
        policyTypeChoiceBox.getSelectionModel().select(vehicle.getInsurance().getPolicyType());
        policyNumber.setText(vehicle.getInsurance().getPolicyNumber());
        policyStartDatePicker.setValue(vehicle.getInsurance().getStartDate());
        policyExpiryDatePicker.setValue(vehicle.getInsurance().getExpiryDate());
        premiumAmount.setText(CurrencyUtil.formatCurrency(vehicle.getInsurance().getPremiumAmount()));

        inspectionDatePicker.setValue(vehicle.getPreviousInspectionDate());
        servicingDatePicker.setValue(vehicle.getServicingDate());
        cost.setText(CurrencyUtil.formatCurrency(vehicle.getCost()));
        getFleetOwners();
    }

}
