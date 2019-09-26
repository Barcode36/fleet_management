package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.*;
import main.java.util.*;
import javafx.fxml.FXML;

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

public class ConductorPerformanceReports {
    @FXML
    private Label totalIncomeLabel, reportTitle;
    @FXML
    private Accordion datePickerAccordion;
    @FXML
    private TitledPane datePickerTitledPane;
    @FXML
    private ChoiceBox<Month> monthChoiceBox;
    @FXML
    private ChoiceBox<Employee> conductorChoiceBox;
    @FXML
    private CheckBox rankCheckBox;
    @FXML
    private TextField yearField;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private HBox mainReportHolder;
    @FXML
    private TableView<Map<String, String>> conductorIncomeTableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, regNumber, income, category;
    @FXML
    private TableView<Map<String, String>> comparativeTableView;
    @FXML
    private TableColumn<Map<String, String>, String> driver, totalIncome, details, rank;
    @FXML
    private VBox container;
    private IncomeExpenseController context;
    @FXML
    private void initialize() {
        datePickerAccordion.setExpandedPane(datePickerTitledPane);
        setUpTables();
        conductorChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            rankCheckBox.setSelected(newValue.intValue() == 0);
        });
        getConductors();
    }

    private void setUpTables() {
        Label label = new Label("No records found!");
        label.getStyleClass().add("missing-content");
        conductorIncomeTableView.setPlaceholder(label);
        comparativeTableView.setPlaceholder(label);

        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        category.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(CATEGORY)));
        income.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT)));

        driver.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DRIVER)));
        totalIncome.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT)));
        details.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < comparativeTableView.getItems().size()) {
                    Button button = new Button("Details");
                    button.getStyleClass().add("btn-default-outline");
                    button.setOnAction(event -> {
                        viewIncomeDetails(comparativeTableView.getItems().get(index).get(RECORD_ID));
                    });
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        rank.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < comparativeTableView.getItems().size()) {
                    setText(Integer.toString(index + 1));
                } else{
                    setText("");
                }
            }
        });
    }


    private void viewIncomeDetails(String conductorId) {
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getIncomeDetails(conductorId);
            }
        };
        task.setOnSucceeded(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/income_expense_report.fxml"));
                Stage stage = new Stage();
                stage.setResizable(false);
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(container.getScene().getWindow());
                IncomeExpenseReportDetailsController controller = loader.getController();
                controller.setParameters(task.getValue(), LedgerEntryType.INCOME, false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        new Thread(task).start();
    }

    private ObservableList<Map<String, String>> getIncomeDetails(String conductorId) throws SQLException {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            return null;
        }
        String sql = "select ledger_entry.*, drivers.national_id, " +
                "conductors.national_id, drivers.first_name, " +
                "drivers.last_name, conductors.first_name, conductors.last_name " +
                "from ledger_entry " +
                "inner join employees drivers on drivers.national_id = ledger_entry.driver_id " +
                "inner join employees conductors on conductors.national_id = ledger_entry.conductor_id " +
                "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "' " +
                "and expense = 0 and income != 0 " +
                "and conductor_id = '" + conductorId + "' " +
                "order by date";

        return Dao.getCashEntries(sql, LedgerEntryType.INCOME);
    }

    private void getConductors() {
        Task<ObservableList<Employee>> task = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return Dao.getEmployees("select * from employees where category = '" + Employee.Category.CONDUCTOR + "'");
            }
        };
        task.setOnSucceeded(event -> {
            Employee driver = new Employee();
            driver.setFirstName("All conductors");
            conductorChoiceBox.setItems(task.getValue());
            conductorChoiceBox.getItems().add(0, driver);
        });
        new Thread(task).start();
    }
    @FXML
    private void onSearchRecords() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify range of dates or select month and year!", Alert.AlertType.ERROR);
            return;
        }
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getData(dates);
            }
        };
        task.setOnSucceeded(event -> {
            context.toggleVisibility(conductorIncomeTableView, !rankCheckBox.isSelected());
            context.toggleVisibility(comparativeTableView, rankCheckBox.isSelected());
            if (rankCheckBox.isSelected()) {
                comparativeTableView.setItems(task.getValue());
            } else {
                conductorIncomeTableView.setItems(task.getValue());
            }
            reportTitle.setText(getReportTitle());
            totalIncomeLabel.setText("Ksh. " + CurrencyUtil.formatCurrency(getTotalIncome()));
            context.toggleVisibility(mainReportHolder, true);
        });
        new Thread(task).start();
    }


    private double getTotalIncome() {
        double total = 0;
        if (comparativeTableView.isVisible()) {
            for (Map<String, String> data : comparativeTableView.getItems()) {
                total += CurrencyUtil.parseCurrency(data.get(AMOUNT));
            }
        } else {
            for (Map<String, String> data : conductorIncomeTableView.getItems()) {
                total += CurrencyUtil.parseCurrency(data.get(AMOUNT));
            }
        }
        return total;
    }

    private String getReportTitle() {
        StringBuilder builder = new StringBuilder();
        builder.append("Conductor Performance Report ").append(" (");
        if (monthChoiceBox.getValue() != null) {
            builder.append(monthChoiceBox.getValue().toString()).append(" ").append(yearField.getText());
        } else {
            builder.append(DateUtil.formatDateLong(startDate.getValue())).append(" - ").append(DateUtil.formatDateLong(endDate.getValue()));
        }
        builder.append(")");
        return builder.toString().toUpperCase();
    }

    private ObservableList<Map<String, String>> getData(List<LocalDate> dates) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        if (rankCheckBox.isSelected()) {
            String sql = "select sum(income) as amount, conductor_id, first_name, last_name " +
                    "from ledger_entry " +
                    "inner join employees on employees.national_id = ledger_entry.conductor_id " +
                    "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "' " +
                    "group by conductor_id, first_name, last_name " +
                    "order by amount desc";
            ResultSet resultSet = DbUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put(RECORD_ID, resultSet.getString("conductor_id"));
                    data.put(DRIVER, resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                    data.put(AMOUNT, CurrencyUtil.formatCurrency(resultSet.getDouble("amount")));
                    list.add(data);
                }
            }
            return list;
        } else {
            String sql = "select ledger_entry.*, drivers.first_name, drivers.national_id, conductors.national_id,  " +
                    "drivers.last_name, conductors.first_name, " +
                    "conductors.last_name " +
                    "from ledger_entry " +
                    "inner join employees drivers on drivers.national_id = ledger_entry.driver_id " +
                    "inner join employees conductors on conductors.national_id = ledger_entry.conductor_id " +
                    "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "' " +
                    "and conductor_id = '" + conductorChoiceBox.getValue().getNationalId() + "' " +
                    "and expense = 0 and income != 0 " +
                    "order by date";
            return Dao.getCashEntries(sql, LedgerEntryType.INCOME);
        }
    }

    @FXML
    private void onExportToPDF() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify range of dates or select month and year!", Alert.AlertType.ERROR);
            return;
        }
        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getData(dates);
            }
        };
        task.setOnSucceeded(event -> {
            Map<String, Object> parameters = createReportParameters(task.getValue());
            new PrintReport().showReport(parameters, "conductor_performance");
        });
        new Thread(task).start();
    }

    private Map<String, Object> createReportParameters(ObservableList<Map<String, String>> data) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", getReportTitle());
        List<Map<String, String>> tableEntries = new ArrayList<>();
        for (Map<String, String> item : data) {
            Map<String, String> entry = new HashMap<>();
            entry.put("conductor", item.get(DRIVER));
            entry.put("amount", item.get(AMOUNT));
            tableEntries.add(entry);
        }
        parameters.put("data", tableEntries);
        return parameters;
    }

    public void setContext(IncomeExpenseController context) {
        this.context = context;
        context.toggleVisibility(mainReportHolder, false);
        rankCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            context.toggleVisibility(conductorIncomeTableView, !newValue);
            context.toggleVisibility(comparativeTableView, newValue);
            if (newValue) {
                conductorChoiceBox.getSelectionModel().select(0);
            }
        });
        rankCheckBox.setSelected(true);
        context.configureSearchDateRange(monthChoiceBox, startDate, endDate, yearField);
    }
}

