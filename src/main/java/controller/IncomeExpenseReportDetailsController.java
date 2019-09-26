package main.java.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.java.model.LedgerEntryType;
import main.java.model.Permission;
import main.java.util.AlertUtil;
import main.java.util.DbUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static main.java.controller.IncomeExpenseController.*;

public class IncomeExpenseReportDetailsController {
    @FXML
    private VBox container;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> amount, date, regNumber, options, details, driver, conductor, category;
    private LedgerEntryType entryType;
    private IncomeExpenseController context;

    @FXML
    private void initialize() {
        setUpTable();

    }

    private void setUpTable() {
        Label label = new Label("No records found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);
        amount.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(AMOUNT)));
        date.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(DATE)));
        regNumber.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(REG_NUMBER)));
        driver.setCellFactory(param -> new TableCellData(driver));

        category.setCellFactory(param -> new TableCellData(category));
        conductor.setCellFactory(param -> new TableCellData(conductor));
        details.setCellFactory(param -> new TableCellData(details));
        options.setCellFactory(param -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    Button delete = new Button("Delete");

                    delete.getStyleClass().add("btn-danger-outline");
                    delete.setDisable(!Main.userPermissions.get(Permission.DELETE_INCOME));
                    delete.setOnAction(event -> {
                        deleteEntry(tableView.getItems().get(index));
                    });

                    Button edit = new Button("Edit");
                    edit.getStyleClass().add("btn-info-outline");
                    edit.setDisable(!Main.userPermissions.get(Permission.EDIT_INCOME));
                    edit.setOnAction(event -> {
                        editEntry(tableView.getItems().get(index));
                    });
                    VBox vBox = new VBox(5.0, edit, delete);
                    vBox.setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void editEntry(Map<String, String> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource
                    ("main/resources/view/record-income-expense.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            RecordIncomeExpenseController controller = loader.getController();
            controller.setDataRecord(data, entryType);
            stage.showAndWait();
            context.onSearchRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteEntry(Map<String, String> entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete record?", ButtonType.YES, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "delete from ledger_entry where entry_id = " + entry.get(RECORD_ID);
            if (DbUtil.executeStatement(sql)) {
                AlertUtil.showAlert("", "Record has been successfully deleted!", Alert.AlertType.INFORMATION);
                if (entryType == LedgerEntryType.INCOME) {
                    DbUtil.saveActivityLog("Deleted income record { Date: " + entry.get(DATE) + ", Vehicle : " + entry.get(REG_NUMBER) + ", Amount: " + entry.get(AMOUNT) + " }");
                } else{
                    DbUtil.saveActivityLog("Deleted expense record { Date: " + entry.get(DATE) + ", Vehicle : " + entry.get(REG_NUMBER) + ", Amount: " + entry.get(AMOUNT) + " }");
                }

                tableView.getItems().remove(entry);
            } else {
                AlertUtil.showGenericError();
            }
        }
    }


    void setParameters(ObservableList<Map<String, String>> data, LedgerEntryType entryType, boolean editable) {
        this.entryType = entryType;
        if (!editable) {
            tableView.getColumns().remove(options);
        }
        tableView.setItems(data);
    }

    public void setContext(IncomeExpenseController context) {
        this.context = context;
    }

    private class TableCellData extends TableCell<Map<String, String>, String> {
        private TableColumn<Map<String, String>, String> column;

        TableCellData(TableColumn<Map<String, String>, String> column) {
            this.column = column;
        }
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            if (index >= 0 && index < tableView.getItems().size()) {
                Map<String, String> entry = tableView.getItems().get(index);
                if (column == category) {
                    setText(entry.get(CATEGORY));
                } else if (column == conductor) {
                    setText(entry.get(CONDUCTOR));
                } else if (column == driver) {
                    setText(entry.get(DRIVER));
                } else if (column == details) {
                    if (entryType == LedgerEntryType.INCOME) {
                        setText(entry.get(INCOME_DETAIL));
                    } else{
                        setText(entry.get(EXPENSE_DETAIL));
                    }
                }
                setPrefHeight(40);
                setWrapText(true);
            } else{
                setText(null);
            }
        }
    }
}

