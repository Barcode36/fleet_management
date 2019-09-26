package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.model.Loan;
import main.java.util.CurrencyUtil;
import main.java.util.LoanUtility;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
public class LoanRepaymentScheduleController {

    @FXML
    private Label principal, monthlyPayments, numPayments, totalPayments;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> periodCol, amountCol, interestCol, principalCol, balanceCol;

    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        periodCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(LoanUtility.PERIOD)));
        amountCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(Math.round(CurrencyUtil.parseCurrency(param.getValue().get(LoanUtility.PAYMENT_AMOUNT))))));
        interestCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(Math.round(CurrencyUtil.parseCurrency(param.getValue().get(LoanUtility.INTEREST))))));
        principalCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(Math.round(CurrencyUtil.parseCurrency(param.getValue().get(LoanUtility.PRINCIPAL))))));
        balanceCol.setCellValueFactory(param -> new SimpleStringProperty(CurrencyUtil.formatCurrency(Math.round(CurrencyUtil.parseCurrency(param.getValue().get(LoanUtility.BALANCE))))));
    }

    @FXML
    public void setLoan(Loan loan) {
        principal.setText("Ksh. " + CurrencyUtil.formatCurrency(loan.getPrincipal()));
        double payments = LoanUtility.calculateMonthlyInstalments(loan);
        monthlyPayments.setText("Ksh. " + CurrencyUtil.formatCurrency(Math.round(payments)));
        numPayments.setText(loan.getNumYears() * 12 + "");
        totalPayments.setText("Ksh. " + CurrencyUtil.formatCurrency(Math.round(payments * loan.getNumYears() * 12)));

        Task<List<Map<String, String>>> task = new Task<List<Map<String, String>>>() {
            @Override
            protected List<Map<String, String>> call() {
                return LoanUtility.getRepaymentSchedule(loan);
            }
        };
        task.setOnSucceeded(event -> {
            tableView.setItems(FXCollections.observableArrayList(task.getValue()));
            int currentDateIndex = getCurrentDateIndex();
            tableView.scrollTo(currentDateIndex);
            tableView.getSelectionModel().select(currentDateIndex);
        });
        new Thread(task).start();
    }

    private int getCurrentDateIndex() {
        LocalDate localDate = LocalDate.now();
        for (Map<String, String> entry : tableView.getItems()) {
            if (entry.get(LoanUtility.PERIOD).equals(localDate.getMonth() + " " + localDate.getYear())) {
                return tableView.getItems().indexOf(entry);
            }
        }
        return 0;
    }
}
