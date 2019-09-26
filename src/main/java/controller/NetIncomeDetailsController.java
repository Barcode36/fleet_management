package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.Map;

import static main.java.controller.IncomeExpenseController.*;


public class NetIncomeDetailsController {
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> date, regNumber, details, income, expense;
    @FXML
    private void initialize() {
        setUpTable();
    }

    private void setUpTable() {
        for (TableColumn column : tableView.getColumns()) {
            if (column == details) {
                column.prefWidthProperty().bind(tableView.widthProperty().divide(3));
            } else{
                column.prefWidthProperty().bind(tableView.widthProperty().divide(6));

            }
        }

        //data
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));
        income.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(INCOME)));
        expense.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(EXPENSE)));
        details.setCellFactory(param -> new TableCell<Map<String, String>, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Map<String, String> data = tableView.getItems().get(index);
                    VBox vBox = new VBox(5.0);
                    vBox.getStyleClass().add("padding-5");
                    HBox hBox = new HBox(5.0);
                    Label label = createLabel("Driver:");
                    Label value = createLabelValue(data, DRIVER);
                    hBox.getChildren().addAll(label, value);
                    vBox.getChildren().add(hBox);

                    hBox = new HBox(5.0);
                    label = createLabel("Conductor:");
                    value = createLabelValue(data, CONDUCTOR);
                    hBox.getChildren().addAll(label, value);
                    vBox.getChildren().add(hBox);

                    setGraphic(vBox);
                } else{
                    setGraphic(null);
                }
            }

            private Label createLabel(String value) {
                Label label = new Label(value);
                label.setMinWidth(Control.USE_PREF_SIZE);
                label.getStyleClass().add("fw-500");
                return label;
            }

            private Label createLabelValue(Map<String, String> data, String key) {
                Label label = new Label(data.get(key));
                label.setTextAlignment(TextAlignment.CENTER);
                return label;
            }
        });
    }

    public void setData(ObservableList<Map<String, String>> data) {
        tableView.setItems(data);
    }
}
