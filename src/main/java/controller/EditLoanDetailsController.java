package main.java.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.InterestRateType;
import main.java.model.Loan;
import main.java.util.*;

import java.io.IOException;

public class EditLoanDetailsController {
    private Stage stage;
    private Loan loan;
    @FXML
    private VBox container;
    @FXML
    private TextField principal, annualInterest, numYears;
    @FXML
    private ChoiceBox<InterestRateType> interestRateTypeChoiceBox;
    @FXML
    private DatePicker datePicker;

    @FXML
    private void initialize() {
        datePicker.setConverter(DateUtil.getDatePickerConverter());
        interestRateTypeChoiceBox.setItems(FXCollections.observableArrayList(InterestRateType.values()));
    }

    @FXML
    private void onClose() {
       stage.close();
    }

    @FXML
    private void onSave() {
        if (validInput()) {
            loan = createLoan();
        }
        AlertUtil.showAlert("", "Loan details saved!", Alert.AlertType.INFORMATION);
        stage.close();
    }

    @FXML
    private void onViewRepaymentSchedule() {
        if (validInput()) {
            Loan loan = createLoan();
            showPaymentSchedule(loan);
        }
    }

    private void showPaymentSchedule(Loan loan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/loan-payment-schedule.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            LoanRepaymentScheduleController controller = loader.getController();
            controller.setLoan(loan);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Loan createLoan() {
        Loan loan = new Loan();
        loan.setPrincipal(CurrencyUtil.parseCurrency(principal.getText()));
        loan.setFirstPayment(datePicker.getValue());
        loan.setInterestRateType(interestRateTypeChoiceBox.getValue());
        loan.setNumYears(NumberUtil.stringToInt(numYears.getText()));
        loan.setRate(NumberUtil.stringToDouble(annualInterest.getText()));
        return loan;
    }

    private boolean validInput() {
        String errorMsg = "";
        if (CurrencyUtil.parseCurrency(principal.getText()) == -1) {
            errorMsg += "Invalid principal value!\n";
        }
        double interest = NumberUtil.stringToDouble(annualInterest.getText());
        if (interest <= 0 || interest >= 100) {
            errorMsg += "Invalid annual interest value!\n";
        }
        if (NumberUtil.stringToInt(numYears.getText()) == -1) {
            errorMsg += "Invalid number of years!\n";
        }
        if (datePicker.getValue() == null) {
            errorMsg += "Invalid start date!\n";
        }
        if (interestRateTypeChoiceBox.getValue() == null) {
            errorMsg += "Interest type required!\n";
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

    public Loan getLoan() {
        return loan;
    }

    public void setRegNumber(String regNumber) {
        Task<Loan> task = new Task<Loan>() {
            @Override
            protected Loan call() {
                return Dao.getLoanDetails(regNumber);
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() != null) {
                setLoanDetails(task.getValue());
            }
        });
        new Thread(task).start();
    }

    private void setLoanDetails(Loan loan) {
        interestRateTypeChoiceBox.getSelectionModel().select(loan.getInterestRateType());
        principal.setText(CurrencyUtil.formatCurrency(loan.getPrincipal()));
        numYears.setText(loan.getNumYears() + "");
        annualInterest.setText(Double.toString(loan.getRate()));
        datePicker.setValue(loan.getFirstPayment());
    }
}
