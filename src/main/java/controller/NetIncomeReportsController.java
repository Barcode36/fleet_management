package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.FleetOwner;
import main.java.util.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.controller.IncomeExpenseController.*;

public class NetIncomeReportsController {

    private static final String NET_INCOME = "net_income";
    private static final String OWNER = "fleet_owner";

    private static final String OWNER_ID = "owner_id";
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private ChoiceBox<Month> monthChoiceBox;
    @FXML
    private TextField yearField;
    @FXML
    private ChoiceBox<FleetOwner> fleetOwnerChoiceBox;
    @FXML
    private ChoiceBox<String> regNumberChoiceBox;
    @FXML
    private Label incomeTotal, expenseTotal, netIncomeTotal, reportTitle;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> fleetOwner, income, expense, netIncome, details;
    @FXML
    private Accordion datePickerAccordion;
    @FXML
    private TitledPane datePickerPane;
    @FXML
    private VBox reportContainer;
    private IncomeExpenseController context;

    @FXML
    private void initialize() {
        datePickerAccordion.setExpandedPane(datePickerPane);
        setUpTable();
    }

    private void setUpTable() {
        Label label = new Label("No results to display!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        fleetOwner.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(OWNER)));
        income.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(INCOME)));
        expense.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(EXPENSE)));
        netIncome.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(NET_INCOME)));
        details.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button details = new Button("Details");
                    details.getStyleClass().add("btn-primary-outline");
                    details.setOnAction(event -> {
                        showIncomeExpenses(tableView.getItems().get(index).get(OWNER_ID));
                    });
                    Button pdf = new Button("Export");
                    pdf.getStyleClass().add("btn-primary-outline");
                    pdf.setOnAction(event -> {
                        printReportDetails(tableView.getItems().get(index).get(OWNER_ID));
                    });
                    HBox hBox = new HBox(5.0, details, pdf);
                    hBox.setAlignment(Pos.CENTER);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void printReportDetails(String fleetOwnerID) {
        //create parameters
        Map<String, Object> params = new HashMap<>();

        FleetOwner fleetOwner = Dao.getFleetOwnerByNationalID(fleetOwnerID);
        if (fleetOwner != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("income/expense report for fleet owner '").append(fleetOwner.getFirstName()).append(" ")
                    .append(fleetOwner.getLastName()).append("'");
            builder.append(" (");
            if (monthChoiceBox.getValue() != null) {
                builder.append(monthChoiceBox.getValue().toString()).append(" ").append(yearField.getText());
            } else {
                builder.append(DateUtil.formatDateLong(startDate.getValue())).append(" - ").append(DateUtil.formatDateLong(endDate.getValue()));
            }
            builder.append(")");
            params.put("title", builder.toString().toUpperCase());
        }
        double totalIncome = 0, totalExpense = 0, netIncome = 0;
        List<Map<String, String>> tableData = new ArrayList<>();
        ObservableList<Map<String, String>> details = searchRecordsByOwner(fleetOwnerID);
        for (Map<String, String> item : details) {
            totalIncome += CurrencyUtil.parseCurrency(item.get(INCOME));
            totalExpense += CurrencyUtil.parseCurrency(item.get(EXPENSE));
            netIncome += (CurrencyUtil.parseCurrency(item.get(INCOME)) - CurrencyUtil.parseCurrency(item.get(EXPENSE)));
            Map<String, String> entry = new HashMap<>();
            entry.put("date", item.get(DATE));
            entry.put("reg_num", item.get(REG_NUMBER));
            entry.put("income", item.get(INCOME));
            entry.put("expense", item.get(EXPENSE));
            entry.put("details", "Driver: " + item.get(DRIVER) + "\n" + "Conductor: " + item.get(CONDUCTOR));
            tableData.add(entry);
        }
        params.put("data", tableData);
        params.put("total_income", CurrencyUtil.formatCurrency(totalIncome));
        params.put("total_expense", CurrencyUtil.formatCurrency(totalExpense));
        params.put("net_income", CurrencyUtil.formatCurrency(netIncome));
        new PrintReport().showReport(params, "net_income_details");
    }

    private void showIncomeExpenses(String ownerId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/net-income-details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(reportContainer.getScene().getWindow());
            NetIncomeDetailsController controller = loader.getController();
            controller.setData(searchRecordsByOwner(ownerId));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Map<String, String>> searchRecordsByOwner(String ownerId) {
        ObservableList<Map<String, String>> dataList = FXCollections.observableArrayList();
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        String sql = "select date, ledger_entry.reg_number, sum(income) as income, " +
                "sum(expense) as expenses, " +
                "d.first_name, d.last_name, c.first_name, c.last_name " +
                "from ledger_entry " +
                "inner join employees d on d.national_id = ledger_entry.driver_id " +
                "inner join employees c on c.national_id = ledger_entry.conductor_id " +
                "inner join vehicles v on v.reg_number = ledger_entry.reg_number " +
                "inner join fleet_owners f on f.national_id = v.owner_id " +
                "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "' " +
                "and owner_id = '" + ownerId + "' " ;
        if (regNumberChoiceBox.getValue() != null) {
            sql += " and ledger_entry.reg_number = '" + regNumberChoiceBox.getValue() + "'";
        }
        sql += " group by date, reg_number, d.first_name, d.last_name, c.first_name, c.last_name " +
                "order by date";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put(INCOME, CurrencyUtil.formatCurrency(resultSet.getDouble("income")));
                    entry.put(EXPENSE, CurrencyUtil.formatCurrency(resultSet.getDouble("expenses")));
                    entry.put(REG_NUMBER, resultSet.getString("reg_number"));
                    entry.put(DATE, DateUtil.formatDate(resultSet.getObject("date", LocalDate.class)));
                    entry.put(DRIVER, resultSet.getString("d.first_name") + " " + resultSet.getString("d.last_name"));
                    entry.put(CONDUCTOR, resultSet.getString("c.first_name") + " " + resultSet.getString("c.last_name"));
                    dataList.add(entry);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return dataList;
    }

    private void setTotals(ObservableList<Map<String, String>> data) {
        List<Double> totals = getTotals(data);
        incomeTotal.setText(CurrencyUtil.formatCurrency(totals.get(0)));
        expenseTotal.setText(CurrencyUtil.formatCurrency(totals.get(1)));
        netIncomeTotal.setText(CurrencyUtil.formatCurrency(totals.get(2)));
        if (totals.get(2) < 0) {
            netIncomeTotal.getStyleClass().add("color-danger");
            netIncomeTotal.getStyleClass().remove("color-success");
        }
    }

    private List<Double> getTotals(ObservableList<Map<String, String>> data) {
        List<Double> totals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            totals.add(0.0);
        }
        for (Map<String, String> item : data) {
            totals.set(0, totals.get(0) + CurrencyUtil.parseCurrency(item.get(INCOME)));
            totals.set(1, totals.get(1) + CurrencyUtil.parseCurrency(item.get(EXPENSE)));
            totals.set(2, totals.get(2) + CurrencyUtil.parseCurrency(item.get(NET_INCOME)));
        }
        return totals;
    }

    void setContext(IncomeExpenseController context) {
        this.context = context;
        context.configureSearchParams(fleetOwnerChoiceBox, regNumberChoiceBox);
        context.configureSearchDateRange(monthChoiceBox, startDate, endDate, yearField);
        context.toggleVisibility(reportContainer, false);
    }

    @FXML
    private void onExportToPDF() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify a range of dates or select month and year!", Alert.AlertType.ERROR);
        } else {
            Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                @Override
                protected ObservableList<Map<String, String>> call() throws Exception {
                    return getNetIncome(dates);
                }
            };
            task.setOnSucceeded(event -> {
                //create params
                Map<String, Object> params = createReportParameters(task.getValue());
                new PrintReport().showReport(params, "net_income_report");
            });
            new Thread(task).start();

        }
    }

    private Map<String, Object> createReportParameters(ObservableList<Map<String, String>> data) {
        Map<String, Object> params = new HashMap<>();
        List<Double> totals = getTotals(data);
        params.put("total_income", CurrencyUtil.formatCurrency(totals.get(0)));
        params.put("total_expense", CurrencyUtil.formatCurrency(totals.get(1)));
        params.put("net_income", CurrencyUtil.formatCurrency(totals.get(2)));
        params.put("title", getReportTitle());
        params.put("data", new ArrayList<>(data));
        return params;
    }

    private String getReportTitle() {
        StringBuilder builder = new StringBuilder();
        builder.append("net income report ");

        if (fleetOwnerChoiceBox.getSelectionModel().getSelectedIndex() > 0) {
            builder.append(" for fleet owner '").append(fleetOwnerChoiceBox.getValue().getFirstName()).append(" ")
                    .append(fleetOwnerChoiceBox.getValue().getLastName()).append("'");
        }
        if (regNumberChoiceBox.getValue() != null) {
            builder.append(" vehicle no. ").append(regNumberChoiceBox.getValue());
        }
        builder.append(" (");
        if (monthChoiceBox.getValue() != null) {
            builder.append(monthChoiceBox.getValue().toString()).append(" ").append(yearField.getText());
        } else {
            builder.append(DateUtil.formatDateLong(startDate.getValue())).append(" - ").append(DateUtil.formatDateLong(endDate.getValue()));
        }
        builder.append(")");
        return builder.toString().toUpperCase();
    }

    @FXML
    private void onGenerateReport() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify a range of dates or select month and year!", Alert.AlertType.ERROR);
        } else {
            Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                @Override
                protected ObservableList<Map<String, String>> call() throws Exception {
                    return getNetIncome(dates);
                }
            };
            task.setOnSucceeded(event -> {
                tableView.setItems(task.getValue());
                reportTitle.setText(getReportTitle());
                setTotals(task.getValue());
                context.toggleVisibility(reportContainer, true);
            });
            new Thread(task).start();

        }
    }

    private ObservableList<Map<String, String>> getNetIncome(List<LocalDate> dates) throws SQLException {
        ObservableList<Map<String, String>> dataList = FXCollections.observableArrayList();
        String sql = "select sum(income) as income, sum(expense) as expense, (sum(income) - sum(expense)) as net, " +
                "f.first_name, f.last_name, f.national_id " +
                "from ledger_entry " +
                "inner join vehicles v on v.reg_number = ledger_entry.reg_number " +
                "inner join fleet_owners f on f.national_id = v.owner_id " +
                "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "'";

        if (fleetOwnerChoiceBox.getSelectionModel().getSelectedIndex() > 0) {
            sql += " and owner_id = '" + fleetOwnerChoiceBox.getValue().getNationalId() + "'";
        }
        if (regNumberChoiceBox.getValue() != null) {
            sql += " and ledger_entry.reg_number = '" + regNumberChoiceBox.getValue() + "'";
        }
        sql += " group by first_name, last_name, national_id";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> entry = new HashMap<>();
                entry.put(INCOME, CurrencyUtil.formatCurrency(resultSet.getDouble("income")));
                entry.put(EXPENSE, CurrencyUtil.formatCurrency(resultSet.getDouble("expense")));
                entry.put(NET_INCOME, CurrencyUtil.formatCurrency(resultSet.getDouble("net")));
                entry.put(OWNER_ID, resultSet.getString("national_id"));
                entry.put(OWNER, resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                dataList.add(entry);
            }
        }

        return dataList;
    }

}

