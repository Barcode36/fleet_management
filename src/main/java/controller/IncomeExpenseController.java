package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.LedgerEntryType;
import main.java.model.FleetOwner;
import main.java.model.Permission;
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

public class IncomeExpenseController {
    public static final String RECORD_ID = "id";
    public static final String CATEGORY = "category";
    public static final String INCOME = "income";
    public static final String EXPENSE = "expense";
    public static final String DATE = "date";
    public static final String DRIVER = "driver";
    public static final String REG_NUMBER = "reg_number";
    public static final String CONDUCTOR = "conductor";
    public static final String CONDUCTOR_ID = "conductor_id";
    public static final String DRIVER_ID = "driver_id";
    public static final String TYPE = "type";
    public static final String EXPENSE_DETAIL = "expense_detail";
    public static final String INCOME_DETAIL = "income_detail";
    public static final String AMOUNT = "amount";
    private ObservableList<String> regNumbers = FXCollections.observableArrayList();
    private Map<String, ObservableList<String>> ownerRegNumberMap = new HashMap<>();
    @FXML
    private ChoiceBox<LedgerEntryType> ledgerEntryTypeChoiceBox;
    @FXML
    private VBox container;
    @FXML
    private DatePicker startDate, endDate;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button addRecord;

    @FXML
    private void initialize() {
//        setUpTable();

        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());

        ledgerEntryTypeChoiceBox.setItems(FXCollections.observableArrayList(LedgerEntryType.values()));
        ledgerEntryTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            onSearchRecords();
        });
        ledgerEntryTypeChoiceBox.getSelectionModel().select(LedgerEntryType.INCOME);

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != 0) {
                loadTab(newValue.intValue());
            }
        });
        addRecord.setDisable(!Main.userPermissions.get(Permission.RECORD_INCOME));
    }

    private void loadTab(int index) {
        String[] files = new String[]{"income-reports", "expense-reports", "net-income-reports",
                "driver-performance-reports", "conductor-performance-reports"};
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + files[index - 1] + ".fxml"));
            Node node = loader.load();

            switch (index - 1) {
                case 0:
                    IncomeReportsController controller = loader.getController();
                    controller.setContext(this);
                    break;
                case 1:
                    ExpenseReportsController expenseReportsController = loader.getController();
                    expenseReportsController.setContext(this);
                    break;
                case 2:
                    NetIncomeReportsController netIncomeReportsController = loader.getController();
                    netIncomeReportsController.setContext(this);
                    break;
                case 3:
                    DriverPerformanceController driverReportsCtrl = loader.getController();
                    driverReportsCtrl.setContext(this);
                    break;
                case 4:
                    ConductorPerformanceReports conductorReportsCtrl = loader.getController();
                    conductorReportsCtrl.setContext(this);
            }

            Tab tab = tabPane.getTabs().get(index);
            tab.setContent(node);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearchRecords() {
        LocalDate start = startDate.getValue() != null ? startDate.getValue() : LocalDate.now().minusMonths(1);
        startDate.setValue(start);

        LocalDate end = endDate.getValue() != null ? endDate.getValue() : LocalDate.now();
        endDate.setValue(end);

        Task<ObservableList<Map<String, String>>> task = new Task<ObservableList<Map<String, String>>>() {
            @Override
            protected ObservableList<Map<String, String>> call() throws Exception {
                return getLedgerEntries(start, end);
            }
        };
        task.setOnSucceeded(event -> {
            showDetails(task.getValue());
        });
        new Thread(task).start();
    }

    private void showDetails(ObservableList<Map<String, String>> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/income_expense_report.fxml"));
            Node node = loader.load();
            VBox.setVgrow(node, Priority.ALWAYS);
            IncomeExpenseReportDetailsController controller = loader.getController();
            controller.setParameters(data, ledgerEntryTypeChoiceBox.getValue(), true);
            controller.setContext(this);
            if (container.getChildren().size() > 3) {
                container.getChildren().remove(3);
            }
            container.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Map<String, String>> getLedgerEntries(LocalDate start, LocalDate end) throws SQLException {
        String sql = "select ledger_entry.*, conductors.national_id, drivers.national_id, " +
                "conductors.first_name, conductors.last_name, " +
                "drivers.first_name, drivers.last_name " +
                "from ledger_entry " +
                "left join employees drivers on drivers.national_id = ledger_entry.driver_id " +
                "left join employees conductors on conductors.national_id = ledger_entry.conductor_id " +
                "where date between '" + start + "' and '" + end + "' ";
        if (ledgerEntryTypeChoiceBox.getValue() == LedgerEntryType.EXPENSE) {
            sql += " and income = 0 and expense  != 0 ";
        } else{
            sql += " and expense = 0 and income != 0 ";
        }
        sql += " order by date";
        return Dao.getCashEntries(sql, ledgerEntryTypeChoiceBox.getValue());
    }

    @FXML
    private void onNewRecord() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/record-income-expense.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            onSearchRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void toggleVisibility(Node target, boolean visible) {
        target.setVisible(visible);
        target.setManaged(visible);
    }

    void configureSearchDateRange(ChoiceBox<Month> monthChoiceBox, DatePicker startDate, DatePicker endDate, TextField yearField) {
        monthChoiceBox.setItems(FXCollections.observableArrayList(Month.values()));
        monthChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                endDate.setValue(null);
                startDate.setValue(null);
            }
        });
        startDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                monthChoiceBox.getSelectionModel().clearSelection();
                yearField.clear();
            }
        });
        startDate.setConverter(DateUtil.getDatePickerConverter());
        endDate.setConverter(DateUtil.getDatePickerConverter());
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
    }

    List<LocalDate> getDates(DatePicker startDate, DatePicker endDate, ChoiceBox<Month> monthChoiceBox, TextField yearField) {
        List<LocalDate> dates = new ArrayList<>();
        if (monthChoiceBox.getValue() == null) {
            if (startDate.getValue() != null && endDate.getValue() != null) {
                dates.add(startDate.getValue());
                dates.add(endDate.getValue());
                return dates;
            }
        } else {
            if (NumberUtil.stringToInt(yearField.getText()) != -1) {
                dates.add(LocalDate.of(NumberUtil.stringToInt(yearField.getText()), monthChoiceBox.getValue().getValue(), 1)); //first day of month
                dates.add(dates.get(0).withDayOfMonth(dates.get(0).lengthOfMonth())); //last day of month
                return dates;
            }
        }
        return null;
    }

    void configureSearchParams(ChoiceBox<FleetOwner> fleetOwnerChoiceBox, ChoiceBox<String> regNumberChoiceBox) {
        getFleetOwners(fleetOwnerChoiceBox);
        getRegNumbers(regNumberChoiceBox);
        fleetOwnerChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > 0) {
                regNumberChoiceBox.setItems(ownerRegNumberMap.get(fleetOwnerChoiceBox.getItems().get(newValue.intValue()).getNationalId()));
            } else {
                regNumberChoiceBox.setItems(regNumbers);
            }
        });
    }

    private void getFleetOwners(ChoiceBox<FleetOwner> fleetOwnerChoiceBox) {
        Task<ObservableList<FleetOwner>> task = new Task<ObservableList<FleetOwner>>() {
            @Override
            protected ObservableList<FleetOwner> call() throws Exception {
                return Dao.getFleetOwners();
            }
        };
        task.setOnSucceeded(event -> {
            fleetOwnerChoiceBox.setItems(task.getValue());
            FleetOwner owner = new FleetOwner("All owners", "", "");
            fleetOwnerChoiceBox.getItems().add(0, owner);
            fleetOwnerChoiceBox.getSelectionModel().select(0);
        });
        new Thread(task).start();
    }

    private void getRegNumbers(ChoiceBox<String> regNumberChoiceBox) {
        Task<Map<String, ObservableList<String>>> task = new Task<Map<String, ObservableList<String>>>() {
            @Override
            protected Map<String, ObservableList<String>> call() throws Exception {
                Map<String, ObservableList<String>> map = new HashMap<>();
                ResultSet resultSet = DbUtil.executeQuery("select owner_id, reg_number from vehicles");
                if (resultSet != null) {
                    while (resultSet.next()) {
                        String owner = resultSet.getString("owner_id");
                        if (map.containsKey(owner)) {
                            ObservableList<String> regNumbers = map.get(owner);
                            regNumbers.add(resultSet.getString("reg_number"));
                        } else {
                            ObservableList<String> regNumbers = FXCollections.observableArrayList(resultSet.getString("reg_number"));
                            map.put(owner, regNumbers);
                        }
                    }
                }

                return map;
            }
        };
        task.setOnSucceeded(event -> {
            ownerRegNumberMap = task.getValue();
            regNumbers = FXCollections.observableArrayList();
            for (String key : task.getValue().keySet()) {
                regNumbers.addAll(task.getValue().get(key));
            }
            regNumberChoiceBox.setItems(regNumbers);
            regNumberChoiceBox.getItems().add(0, null);
        });
        new Thread(task).start();
    }

    void getDataSeries(ObservableList<Map<String, String>> searchResults, XYChart.Series<String, Double> series) {
        for (Map<String, String> item : searchResults) {
            XYChart.Data<String, Double> data = getDataFromSeries(series, item.get(DATE));
            if (data != null) {
                data.setYValue(data.getYValue() + CurrencyUtil.parseCurrency(item.get(AMOUNT)));
            } else {
                series.getData().add(new XYChart.Data<>(item.get(DATE), CurrencyUtil.parseCurrency(item.get(AMOUNT))));
            }
        }
        ;
    }
    private XYChart.Data<String, Double> getDataFromSeries(XYChart.Series<String, Double> series, String date) {
        for (XYChart.Data<String, Double> data : series.getData()) {
            if (data.getXValue().equals(date)) {
                return data;
            }
        }
        return null;
    }


}
