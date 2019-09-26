package main.java.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class LineChartController {
    @FXML
    private NumberAxis numberAxis;
    @FXML
    private CategoryAxis categoryAxis;
    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private void initialize() {

    }

    void setParameters(String categoryAxisLabel, String numberAxisLabel) {
        categoryAxis.setLabel(categoryAxisLabel);
        numberAxis.setLabel(numberAxisLabel);

    }

    public void setData(ObservableList<XYChart.Series<String,Double>> data) {
        lineChart.getData().clear();
        lineChart.setData(data);
    }
}

