package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.java.model.FleetOwner;
import main.java.model.LedgerEntryType;
import main.java.model.OperationCategory;
import main.java.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.controller.IncomeExpenseController.*;

public class IncomeReportsController {
    @FXML
    private Accordion datePickerAccordion;
    @FXML
    private TitledPane datePickerPane;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private ChoiceBox<Month> monthChoiceBox;
    @FXML
    private ChoiceBox<OperationCategory> incomeCategoryChoiceBox;
    @FXML
    private ChoiceBox<FleetOwner> fleetOwnerChoiceBox;
    @FXML
    private ChoiceBox<String> regNumberChoiceBox;
    @FXML
    private TextField yearField;
    @FXML
    private VBox reportContainer;

    private ObservableList<Map<String, String>> searchResults = FXCollections.observableArrayList();
    private IncomeExpenseController context;

    @FXML
    private void initialize() {
        datePickerAccordion.setExpandedPane(datePickerPane);
        incomeCategoryChoiceBox.setItems(FXCollections.observableArrayList(OperationCategory.values()));

        incomeCategoryChoiceBox.setItems(FXCollections.observableArrayList(OperationCategory.values()));
        incomeCategoryChoiceBox.getItems().add(0, null);
    }

    private ObservableList<Map<String, String>> getIncomeRecords(List<LocalDate> dates) throws SQLException {
        String sql = "select ledger_entry.*, drivers.first_name, drivers.national_id, conductors.national_id,  " +
                "drivers.last_name, " +
                "conductors.first_name, conductors.last_name " +
                "from ledger_entry " +
                "inner join employees drivers on drivers.national_id = ledger_entry.driver_id " +
                "inner join employees conductors on conductors.national_id = ledger_entry.conductor_id " +
                "inner join vehicles on vehicles.reg_number = ledger_entry.reg_number " +
                "where date between '" + dates.get(0) + "' and '" + dates.get(1) + "' " +
                "and expense = 0 and income != 0 ";
        if (incomeCategoryChoiceBox.getValue() != null) {
            sql += " and ledger_entry.category = '" + incomeCategoryChoiceBox.getValue().name() + "' ";
        }
        if (fleetOwnerChoiceBox.getSelectionModel().getSelectedIndex() > 0) {
            sql += " and owner_id = '" + fleetOwnerChoiceBox.getValue().getNationalId() + "' ";
        }
        if (regNumberChoiceBox.getValue() != null) {
            sql += " and ledger_entry.reg_number = '" + regNumberChoiceBox.getValue() + "'";
        }
        sql += " order by date ";
        return Dao.getCashEntries(sql, LedgerEntryType.INCOME);
    }

    @FXML
    private void onExport() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify a range of dates or select month and year!", Alert.AlertType.ERROR);
        } else{
            Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                @Override
                protected ObservableList<Map<String, String>> call() throws Exception {
                    return getIncomeRecords(dates);
                }
            };
            task.setOnSucceeded(event -> {
                searchResults  = task.getValue();
                //create parameters
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("data", getIncomeValues());
                parameters.put("total", CurrencyUtil.formatCurrency(getTotalIncome()));
                parameters.put("title", getReportTitle());
                new PrintReport().showReport(parameters, "income_report");
            });
            new Thread(task).start();
        }
    }

    private List<Map<String, String>> getIncomeValues() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map<String, String> item : searchResults) {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("reg_num", item.get(REG_NUMBER));
            paramMap.put("category", item.get(CATEGORY));
            paramMap.put("date", item.get(DATE));
            paramMap.put("amount", item.get(AMOUNT));
            paramMap.put("employees", "Driver: " + item.get(DRIVER) + "\n" + "Conductor: " + item.get
                    (CONDUCTOR));
            list.add(paramMap);
        }
        return list;
    }

    @FXML
    private void onGenerateGraph() {
        List<LocalDate> dates = context.getDates(startDate, endDate, monthChoiceBox, yearField);
        if (dates == null) {
            AlertUtil.showAlert("", "Specify a range of dates or select month and year!", Alert.AlertType.ERROR);
        } else {
            Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
                @Override
                protected ObservableList<Map<String, String>> call() throws Exception {
                    return getIncomeRecords(dates);
                }
            };
            task.setOnSucceeded(event -> {
                searchResults = task.getValue();
                getGraphData();

            });
            new Thread(task).start();
        }
    }

    private void getGraphData() {
        Task<XYChart.Series<String, Double>> task = new Task<XYChart.Series<String, Double>>() {
            @Override
            protected XYChart.Series<String, Double> call() throws Exception {
                XYChart.Series<String, Double> series = new XYChart.Series<>();
                series.setName("Income");
                if (regNumberChoiceBox.getValue() != null) {
                    series.setName(regNumberChoiceBox.getValue());
                }
                if (fleetOwnerChoiceBox.getSelectionModel().getSelectedIndex() > 0 && regNumberChoiceBox.getValue() == null) {
                    series.setName(fleetOwnerChoiceBox.getValue().toString() + " Income");
                }
                context.getDataSeries(searchResults, series);

                return series;
            }
        };
        task.setOnSucceeded(event -> {
            showGraph(task.getValue());
        });
        new Thread(task).start();
    }

    private void showGraph(XYChart.Series<String, Double> series) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/line-chart.fxml"));
            Node node = loader.load();
            VBox.setVgrow(node, Priority.ALWAYS);
            LineChartController controller = loader.getController();
            controller.setParameters("Date", "Income");
            ObservableList<XYChart.Series<String, Double>> list = FXCollections.observableArrayList();
            list.add(series);
            controller.setData(list);
            reportContainer.getChildren().clear();
            reportContainer.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    return getIncomeRecords(dates);
                }
            };
            task.setOnSucceeded(event -> {
                searchResults = task.getValue();
                showSearchResult();
            });
            new Thread(task).start();
        }
    }

    private void showSearchResult() {
        //title, total and show table
        //title
        reportContainer.getChildren().clear();
        Label label = new Label(getReportTitle());
        label.getStyleClass().add("fw-500");
        reportContainer.getChildren().add(label);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/income_expense_report.fxml"));
            Node node = loader.load();
            VBox.setVgrow(node, Priority.ALWAYS);
            IncomeExpenseReportDetailsController controller = loader.getController();
            controller.setParameters(searchResults, LedgerEntryType.INCOME, false);
            reportContainer.getChildren().addAll(node, new Separator(Orientation.HORIZONTAL));
        } catch (IOException e) {
            e.printStackTrace();
        }

        HBox hBox = new HBox(5.0);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label = new Label("Total");
        label.getStyleClass().add("h6");
        hBox.getChildren().add(label);
        label = new Label("Ksh. " + CurrencyUtil.formatCurrency(getTotalIncome()));
        label.getStyleClass().addAll("h6", "fw-500", "color-accent");
        hBox.getChildren().add(label);
        reportContainer.getChildren().add(hBox);
    }

    private double getTotalIncome() {
        double total = 0;
        for (Map<String, String> item : searchResults) {
            total += CurrencyUtil.parseCurrency(item.get(AMOUNT));
        }
        return total;
    }

    private String getReportTitle() {
        StringBuilder builder = new StringBuilder();
        builder.append("income report ");

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

    void setContext(IncomeExpenseController context) {
        this.context = context;
        context.configureSearchParams(fleetOwnerChoiceBox, regNumberChoiceBox);
        context.configureSearchDateRange(monthChoiceBox, startDate, endDate, yearField);
    }
}
