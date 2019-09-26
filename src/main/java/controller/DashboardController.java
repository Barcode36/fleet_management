package main.java.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.LedgerEntryType;
import main.java.util.DateUtil;
import main.java.util.DbUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {
    @FXML
    private VBox container;
    @FXML
    private Label incomeChartTitle, expenseChartTitle;
    @FXML
    private LineChart<String, Double> expenseLineChart, incomeLineChart;
    @FXML
    private CategoryAxis expenseChartCategoryAxis, incomeChartCategoryAxis, incomeRankCategoryAxis;
    @FXML
    private NumberAxis expenseChartNumberAxis, incomeChartNumberAxis, incomeRankNumberAxis;
    @FXML
    private BarChart<String, Double> incomeRankBarChart;

    @FXML
    private FlowPane flowPane;

    @FXML
    private void initialize() {
        getVehiclesList();
        generateExpenseChart();
        generateIncomeChart();
        generateIncomeRankingsChart();

        LocalDate currentDate = LocalDate.now();
        incomeChartTitle.setText((currentDate.getMonth().name() + " " + currentDate.getYear() + " income ").toUpperCase());
        expenseChartTitle.setText((currentDate.getMonth().name() + " " + currentDate.getYear() + " expense ").toUpperCase());

    }

    private void generateIncomeRankingsChart() {
        Task<Map<String, Double>> task = new Task<Map<String, Double>>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                return getIncomeRankings();
            }
        };
        task.setOnSucceeded(event -> {
            incomeRankBarChart.getData().clear();
            incomeRankCategoryAxis.setLabel("Reg. Number");
            incomeRankNumberAxis.setLabel("Income");
            for (String regNumber : task.getValue().keySet()) {
                incomeRankBarChart.getData().add(getSeries(regNumber, task.getValue().get(regNumber)));
            }
        });
        new Thread(task).start();
    }

    private XYChart.Series<String, Double> getSeries(String regNumber, Double amount) {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        XYChart.Data<String, Double> data = new XYChart.Data<>("", amount);
        series.getData().add(data);
        series.setName(regNumber);
        return series;
    }

    private Map<String, Double> getIncomeRankings() throws SQLException {
        Map<String, Double> data = new HashMap<>();
        String sql = "select sum(income) as amount, reg_number from ledger_entry " +
                "where date between '" + LocalDate.now().minusMonths(3) + "' and '" + LocalDate.now() + "' " +
                "group by reg_number " +
                "order by amount desc limit 5";
        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                data.put(resultSet.getString("reg_number"), resultSet.getDouble("amount"));
            }
        }
        return data;
    }

    private void generateIncomeChart() {

        Task<XYChart.Series<String, Double>> task = new Task<XYChart.Series<String, Double>>() {
            @Override
            protected XYChart.Series<String, Double> call() throws Exception {
                return getCurrentMonthData(LedgerEntryType.INCOME);
            }
        };
        task.setOnSucceeded(event -> {
            incomeLineChart.getData().clear();
            incomeChartCategoryAxis.setLabel("Date");
            incomeChartNumberAxis.setLabel("Income");
            incomeLineChart.getData().add(task.getValue());
        });
        new Thread(task).start();
    }

    private XYChart.Series<String, Double> getCurrentMonthData(LedgerEntryType entryType) throws SQLException {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(start.lengthOfMonth());
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        String sql;
        if (entryType == LedgerEntryType.INCOME) {
            sql = "select sum(income) as amount, date from ledger_entry " +
                    " where date between '" + start + "' and '" + end + "' " +
                    " group by date order by date asc";
        } else {
            sql = "select sum(expense) as amount, date from ledger_entry " +
                    " where date between '" + start + "' and '" + end + "' " +
                    " group by date order by date asc";
        }

        ResultSet resultSet = DbUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                series.getData().add(new XYChart.Data<>(DateUtil.formatDateLong(resultSet.getObject("date", LocalDate.class)), resultSet.getDouble("amount")));
            }
        }
        return series;
    }

    private void generateExpenseChart() {
        Task<XYChart.Series<String, Double>> task = new Task<XYChart.Series<String, Double>>() {
            @Override
            protected XYChart.Series<String, Double> call() throws Exception {
                return getCurrentMonthData(LedgerEntryType.EXPENSE);
            }
        };
        task.setOnSucceeded(event -> {
            expenseLineChart.getData().clear();
            expenseChartCategoryAxis.setLabel("Date");
            expenseChartNumberAxis.setLabel("Expense");
            expenseLineChart.getData().add(task.getValue());
        });
        new Thread(task).start();
    }

    private void getVehiclesList() {
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                String sql = "select reg_number from vehicles order by reg_number";
                ResultSet resultSet = DbUtil.executeQuery(sql);
                if (resultSet != null) {
                    while (resultSet.next()) {
                        list.add(resultSet.getString("reg_number"));
                    }
                }
                return list;
            }
        };
        task.setOnSucceeded(event -> {
            drawCanvas(task.getValue());
        });
        new Thread(task).start();
    }

    private void drawCanvas(List<String> regNumbersList) {
        if (regNumbersList.isEmpty()) {
            Label label = new Label("No registered vehicles!");
            label.getStyleClass().add("missing-content");
            flowPane.setAlignment(Pos.CENTER);
            flowPane.getChildren().add(label);
        } else {
            for (String regNumber : regNumbersList) {
                Label label = new Label(regNumber);
                VBox vBox = new VBox(label);
                vBox.setAlignment(Pos.CENTER);
                vBox.getStyleClass().add("dashboard-item");
                vBox.setOnMouseClicked(event -> {
                    openClickedItem(regNumber);
                });
                flowPane.getChildren().add(vBox);
            }
        }
    }

    private void openClickedItem(String regNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/control-panel.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());

            ControlPanelController controller = loader.getController();
            controller.setRegNumber(regNumber);

            stage.showAndWait();
            generateExpenseChart();
            generateIncomeChart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewCurrentAssignments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/manage_assignments.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(container.getScene().getWindow());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAssignEmployees() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/assign_vehicles.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            AssignVehiclesController controller = loader.getController();
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
