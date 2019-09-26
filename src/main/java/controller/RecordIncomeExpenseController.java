package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import main.java.model.*;
import main.java.util.*;

import java.time.LocalDate;
import java.util.Map;

import static main.java.controller.IncomeExpenseController.*;

public class RecordIncomeExpenseController {
    @FXML
    private ChoiceBox<Employee> conductorChoiceBox, driverChoiceBox;
    @FXML
    private ChoiceBox<String> regNumberChoiceBox;
    @FXML
    private TextField income, expense, expenseDetails, incomeDetails;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<OperationCategory> incomeCategoryChoiceBox;
    @FXML
    private ChoiceBox<ExpenseCategory> expenseCategoryChoiceBox;
    private Map<String, String> recordedData;

    @FXML
    private void initialize() {
        getRegNumbersList();
        getEmployees();
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        datePicker.setValue(LocalDate.now());
        incomeCategoryChoiceBox.setItems(FXCollections.observableArrayList(OperationCategory.values()));
        expenseCategoryChoiceBox.setItems(FXCollections.observableArrayList(ExpenseCategory.values()));

    }

    private void getEmployees() {
        //conductors
        Task<ObservableList<Employee>> conductorsListTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.CONDUCTOR.name() +
                        "'");
            }
        };
        conductorsListTask.setOnSucceeded(event -> {
            conductorChoiceBox.setItems(conductorsListTask.getValue());

            //drivers
            Task<ObservableList<Employee>> driverListTask = new Task<ObservableList<Employee>>() {
                @Override
                protected ObservableList<Employee> call() {
                    return Dao.getEmployees("select * from employees where category = '" + Employee.Category.DRIVER.name() +
                            "'");
                }
            };
            driverListTask.setOnSucceeded(e -> {
                driverChoiceBox.setItems(driverListTask.getValue());
                if (recordedData != null) {
                    for (Employee driver : driverChoiceBox.getItems()) {
                        if (driver.getNationalId().equals(recordedData.get(DRIVER_ID))) {
                            driverChoiceBox.getSelectionModel().select(driver);
                        }
                    }
                    for (Employee conductor : conductorChoiceBox.getItems()) {
                        if (conductor.getNationalId().equals(recordedData.get(CONDUCTOR_ID))) {
                            conductorChoiceBox.getSelectionModel().select(conductor);
                        }
                    }
                }
            });
            new Thread(driverListTask).start();
        });
        new Thread(conductorsListTask).start();

    }

    private void getRegNumbersList() {
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                return Dao.getRegNumbers();
            }
        };
        task.setOnSucceeded(event -> {
            regNumberChoiceBox.setItems(task.getValue());
        });
        new Thread(task).start();
    }

    @FXML
    private void onSaveDetails() {
        if (CurrencyUtil.parseCurrency(income.getText()) == -1 && CurrencyUtil.parseCurrency(expense.getText()) == -1) {
            AlertUtil.showAlert("", "No valid amount to save!", Alert.AlertType.ERROR);
        }
        if (CurrencyUtil.parseCurrency(income.getText()) != -1) {
            saveIncome();
        }
        if (CurrencyUtil.parseCurrency(expense.getText()) != -1) {
            saveExpense();
        }
    }

    private void saveIncome() {
        if (validInput(true)) {
            if (DbUtil.saveLedgeEntry(getLedgerEntry(LedgerEntryType.INCOME), incomeCategoryChoiceBox.getValue().name(),
                    regNumberChoiceBox
                            .getValue
                                    (),
                    driverChoiceBox.getValue()
                            .getNationalId(),
                    conductorChoiceBox.getValue().getNationalId())) {
                DbUtil.saveActivityLog("Recorded income { Vehicle: " + regNumberChoiceBox.getValue() + ", Amount: " + CurrencyUtil.parseCurrency(income.getText()) + " }");
                AlertUtil.showAlert("", "Income amount of Ksh. " + CurrencyUtil.formatCurrency(CurrencyUtil.parseCurrency(income.getText())) + " has been successfully recorded.", Alert.AlertType.INFORMATION);
                income.clear();
                incomeDetails.clear();
                incomeCategoryChoiceBox.setValue(null);
                recordedData = null;
            }
        }
    }

    private LedgerEntry getLedgerEntry(LedgerEntryType entryType) {
        LedgerEntry entry = new LedgerEntry();
        if (recordedData != null) {
            entry.setId(Integer.parseInt(recordedData.get(RECORD_ID)));
        }
        if (entryType == LedgerEntryType.EXPENSE) {
            entry.setExpense(CurrencyUtil.parseCurrency(this.expense.getText()));
            entry.setDetails(expenseDetails.getText());
        } else {
            entry.setIncome(CurrencyUtil.parseCurrency(this.income.getText()));
            entry.setDetails(incomeDetails.getText());
        }
        entry.setDateCreated(datePicker.getValue());
        return entry;
    }

    private void saveExpense() {
        if (validInput(false)) {
            if (DbUtil.saveLedgeEntry(getLedgerEntry(LedgerEntryType.EXPENSE), expenseCategoryChoiceBox.getValue()
                            .name(), regNumberChoiceBox.getValue(), driverChoiceBox.getValue().getNationalId(),
                    conductorChoiceBox.getValue().getNationalId())) {
                DbUtil.saveActivityLog("Recorded expense { Vehicle: " + regNumberChoiceBox.getValue() + ", Amount: " + CurrencyUtil.parseCurrency(expense.getText()) + " }");
                AlertUtil.showAlert("Record Expense", "An expense of amount Ksh. " + CurrencyUtil.formatCurrency(CurrencyUtil.parseCurrency(expense.getText())) + " has been successfully saved!", Alert.AlertType.INFORMATION);
                expenseCategoryChoiceBox.setValue(null);
                expense.clear();
                expenseDetails.clear();
                recordedData = null;
            } else {
                AlertUtil.showAlert("Error", "An error occurred while attempting to save expense record.", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validInput(boolean isIncome) {
        String errorMsg = "";
        if (regNumberChoiceBox.getValue() == null) {
            errorMsg += "Vehicle registration number required!\n";
        }

        if (datePicker.getValue() == null) {
            errorMsg += "Date required!\n";
        }

        if (driverChoiceBox.getValue() == null) {
            errorMsg += "Driver of vehicle required!\n";
        }
        if (conductorChoiceBox.getValue() == null) {
            errorMsg += "Conductor of vehicle required!\n";
        }

        if (isIncome) {
            if (incomeCategoryChoiceBox.getValue() == null) {
                errorMsg += "Income category required!\n";
            }
        } else {
            if (expenseCategoryChoiceBox.getValue() == null) {
                errorMsg += "Expense category required!\n";
            } else if (expenseCategoryChoiceBox.getValue() == ExpenseCategory.ACCIDENT_REPAIRS) {
                if (expenseDetails.getText() == null || expenseDetails.getText().isEmpty()) {
                    errorMsg += "Details required for category 'Accident Repairs'\n";
                }
            }
        }

        if (errorMsg.isEmpty()) {
            return true;
        }
        AlertUtil.showAlert("Input Error(s)", errorMsg, Alert.AlertType.ERROR);
        return false;
    }

    void setRegNumber(String regNumber) {
        regNumberChoiceBox.setValue(regNumber);
    }

    void setDataRecord(Map<String, String> data, LedgerEntryType entryType) {
        this.recordedData = data;
        regNumberChoiceBox.setValue(data.get(REG_NUMBER));
        datePicker.setValue(DateUtil.parseDate(data.get(DATE)));
        if (entryType == LedgerEntryType.INCOME) {
            for (OperationCategory category : incomeCategoryChoiceBox.getItems()) {
                if (category.toString().equals(data.get(CATEGORY))) {
                    incomeCategoryChoiceBox.setValue(category);
                }
            }
            income.setText(data.get(AMOUNT));
            incomeDetails.setText(data.get(INCOME_DETAIL));
        } else {
            for (ExpenseCategory category : expenseCategoryChoiceBox.getItems()) {
                if (category.toString().equals(data.get(CATEGORY))) {
                    expenseCategoryChoiceBox.setValue(category);
                }
            }
            expense.setText(data.get(AMOUNT));
            expenseDetails.setText(data.get(EXPENSE_DETAIL));
        }
        getEmployees();
    }
}
