package main.java.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import main.java.model.Employee;
import main.java.util.AlertUtil;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EditEmployeeController {

    private Stage stage;
    @FXML
    private Region container;
    @FXML
    private TextField firstName, lastName, nationalId, licenseNumber, residence, phoneNumber;
    @FXML
    private DatePicker licenseIssueDate, licenseExpiryDate;
    @FXML
    private ChoiceBox<Employee.Category> categoryChoiceBox;
    private Employee employee;
    @FXML
    private Button uploadPhotoBtn, uploadLicenseBtn, uploadIdBtn;

    @FXML
    private void initialize() {
        licenseExpiryDate.setConverter(DateUtil.getDatePickerConverter());
        licenseIssueDate.setConverter(DateUtil.getDatePickerConverter());

        categoryChoiceBox.setItems(FXCollections.observableArrayList(Employee.Category.values()));
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Employee.Category.CONDUCTOR) {
                licenseNumber.textProperty().bind(nationalId.textProperty());
            } else {
                licenseNumber.textProperty().unbind();
            }
        });
        categoryChoiceBox.getSelectionModel().select(Employee.Category.DRIVER);
        //the license number text property is bound to the national id when employee category = conductor but the
        // user can opt to use a different license number
        licenseNumber.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                licenseNumber.textProperty().unbind();
            }
        });

        for (Button button : new Button[]{uploadIdBtn, uploadLicenseBtn, uploadPhotoBtn}) {
            button.setOnAction(event -> {
                uploadDocument(button.getId());
            });
        }

    }

    private void uploadDocument(String fileName) {
        if (nationalId.getText() == null || nationalId.getText().isEmpty()) {
            AlertUtil.showAlert("", "Please register employee first!", Alert.AlertType.ERROR);
        } else {
            new UploadDocumentController().showUploadDocumentDialog(fileName, nationalId.getText(), container);
        }
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            if (saveEmployee()) {
                if (employee == null) {
                    if (categoryChoiceBox.getValue() == Employee.Category.DRIVER) {
                        DbUtil.saveActivityLog("Registered driver '" + firstName.getText() + " " + lastName.getText() + "'");
                    } else {
                        DbUtil.saveActivityLog("Registered conductor '" + firstName.getText() + " " + lastName.getText() + "'");
                    }
                } else {
                    if (categoryChoiceBox.getValue() == Employee.Category.DRIVER) {
                        DbUtil.saveActivityLog("Changed driver's details { Name: " + firstName.getText() + " " + lastName.getText() + ", National ID: " + nationalId.getText() + " }");
                    } else {
                        DbUtil.saveActivityLog("Changed conductor's details { Name: " + firstName.getText() + " " + lastName.getText() + ", National ID: " + nationalId.getText() + " }");
                    }
                }
                AlertUtil.showAlert("Save Employee", "Employee details have been successfully saved", Alert.AlertType.INFORMATION);
                onCancel();
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save employee details", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean saveEmployee() {
        Employee employee = new Employee();
        employee.setFirstName(firstName.getText());
        employee.setLastName(lastName.getText());
        employee.setNationalId(nationalId.getText());
        employee.setLicenseIssueDate(licenseIssueDate.getValue());
        employee.setLicenseNo(licenseNumber.getText());
        employee.setLicenseExpiryDate(licenseExpiryDate.getValue());
        employee.setResidence(residence.getText());
        employee.setPhoneNumber(phoneNumber.getText());
        employee.setCategory(categoryChoiceBox.getValue());
        return DbUtil.saveEmployee(employee);
    }

    private boolean validInput() {
        String errorMsg = "";
        if (nationalId.getText() == null || nationalId.getText().isEmpty()) {
            errorMsg += "National ID required!\n";
        } else if (!nationalId.isDisabled()) {
            if (nationalIdUsed()) {
                errorMsg += "National ID has already been registered!\n";
            }
        }
        if (firstName.getText() == null || firstName.getText().isEmpty()) {
            errorMsg += "First name is required!\n";
        }
        if (lastName.getText() == null || lastName.getText().isEmpty()) {
            errorMsg += "Last name is required!\n";
        }
        if (licenseNumber.getText() == null || licenseNumber.getText().isEmpty()) {
            errorMsg += "License number is required!\n";
        }
        if (licenseIssueDate.getValue() == null) {
            errorMsg += "License issue date required!\n";
        }

        if (licenseExpiryDate.getValue() == null) {
            errorMsg += "License expiry date required!\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    private boolean nationalIdUsed() {
        String id = nationalId.getText();
        String sql = "select national_id from employees " +
                "where national_id = '" + id + "' ";

        ResultSet resultSet = DbUtil.executeQuery(sql);
        try {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) {
            firstName.setText(employee.getFirstName());
            lastName.setText(employee.getLastName());
            nationalId.setText(employee.getNationalId());
            licenseIssueDate.setValue(employee.getLicenseIssueDate());
            licenseExpiryDate.setValue(employee.getLicenseExpiryDate());
            licenseNumber.setText(employee.getLicenseNo());
            categoryChoiceBox.getSelectionModel().select(employee.getCategory());
            residence.setText(employee.getResidence());
            phoneNumber.setText(employee.getPhoneNumber());
            nationalId.setDisable(true);
        }
    }

}
