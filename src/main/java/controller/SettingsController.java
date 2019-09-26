package main.java.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.Main;
import main.java.model.Permission;
import main.java.model.ScheduledAction;
import main.java.model.Setting;
import main.java.model.TimeUnit;
import main.java.util.AlertUtil;
import main.java.util.Dao;
import main.java.util.DbUtil;
import main.java.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {
    @FXML
    private ChoiceBox<TimeUnit> insuranceTimeUnitChoiceBox, servicingTimeUnitChoiceBox, speedGovTimeUnitChoiceBox, inspectionTimeUnitChoiceBox, licenseTimeUnitChoiceBox;
    @FXML
    private TextField insuranceTextField, servicingTextField, speedGovTextField, inspectionTextField, licenseTextField;
    private Stage stage;

    @FXML
    private Accordion accordion;
    @FXML
    private Button saveButton;
    @FXML
    private void initialize() {
        for (ChoiceBox choiceBox : new ChoiceBox[]{insuranceTimeUnitChoiceBox, servicingTimeUnitChoiceBox, speedGovTimeUnitChoiceBox, inspectionTimeUnitChoiceBox, licenseTimeUnitChoiceBox}) {
            choiceBox.setItems(FXCollections.observableArrayList(TimeUnit.values()));
        }

        for (TitledPane pane : accordion.getPanes()) {
            pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    VBox vBox = (VBox) pane.getContent();
                    HBox hBox = (HBox) vBox.getChildren().get(0);
                    TextField textField = (TextField) hBox.getChildren().get(1);
                    Platform.runLater(textField::requestFocus);
                }
            });
        }
        saveButton.setDisable(!Main.userPermissions.get(Permission.MODIFY_SETTINGS));
        getSettings();
    }

    private void getSettings() {
        Task<ObservableList<Setting>> task = new Task<ObservableList<Setting>>() {
            @Override
            protected ObservableList<Setting> call() throws Exception {
                return Dao.getSettings();
            }
        };
        task.setOnSucceeded(event -> {
            setSettings(task.getValue());
        });
        new Thread(task).start();
    }

    private void setSettings(ObservableList<Setting> settings) {
        for (Setting setting : settings) {
            switch (setting.getAction()) {
                case INSURANCE:
                    insuranceTimeUnitChoiceBox.getSelectionModel().select(setting.getTimeUnit());
                    insuranceTextField.setText(Integer.toString(setting.getDuration()));
                    break;
                case SERVICING:
                    servicingTimeUnitChoiceBox.getSelectionModel().select(setting.getTimeUnit());
                    servicingTextField.setText(Integer.toString(setting.getDuration()));
                    break;
                case INSPECTION:
                    inspectionTimeUnitChoiceBox.getSelectionModel().select(setting.getTimeUnit());
                    inspectionTextField.setText(Integer.toString(setting.getDuration()));
                    break;
                case SPEED_GOV_RENEWAL:
                    speedGovTimeUnitChoiceBox.getSelectionModel().select(setting.getTimeUnit());
                    speedGovTextField.setText(Integer.toString(setting.getDuration()));
                    break;
                case NTSA_LICENSE_RENEWAL:
                    licenseTimeUnitChoiceBox.getSelectionModel().select(setting.getTimeUnit());
                    licenseTextField.setText(Integer.toString(setting.getDuration()));
            }
        }
    }

    @FXML
    private void onSaveSettings() {
        if (DbUtil.saveSettings(createSettings())) {
            DbUtil.saveActivityLog("Updated reminders settings");
            AlertUtil.showAlert("Settings", "Settings successfully saved!", Alert.AlertType.INFORMATION);
            stage.close();
        } else {
            AlertUtil.showGenericError();
        }
    }

    private List<Setting> createSettings() {
        List<Setting> settings = new ArrayList<>();
        if (insuranceTimeUnitChoiceBox.getValue() != null && NumberUtil.stringToInt(insuranceTextField.getText()) != -1) {
            settings.add(new Setting(ScheduledAction.INSURANCE, NumberUtil.stringToInt(insuranceTextField.getText()), insuranceTimeUnitChoiceBox.getValue()));

        }

        if (inspectionTimeUnitChoiceBox.getValue() != null && NumberUtil.stringToInt(inspectionTextField.getText()) != -1) {
            settings.add(new Setting(ScheduledAction.INSPECTION, NumberUtil.stringToInt(inspectionTextField.getText()), inspectionTimeUnitChoiceBox.getValue()));

        }
        if (licenseTimeUnitChoiceBox.getValue() != null && NumberUtil.stringToInt(licenseTextField.getText()) != -1) {
            settings.add(new Setting(ScheduledAction.NTSA_LICENSE_RENEWAL, NumberUtil.stringToInt(licenseTextField.getText()), licenseTimeUnitChoiceBox.getValue()));

        }
        if (servicingTimeUnitChoiceBox.getValue() != null && NumberUtil.stringToInt(servicingTextField.getText()) != -1) {
            settings.add(new Setting(ScheduledAction.SERVICING, NumberUtil.stringToInt(servicingTextField.getText()), servicingTimeUnitChoiceBox.getValue()));

        }
        if (speedGovTimeUnitChoiceBox.getValue() != null && NumberUtil.stringToInt(speedGovTextField.getText()) != -1) {
            settings.add(new Setting(ScheduledAction.SPEED_GOV_RENEWAL, NumberUtil.stringToInt(speedGovTextField.getText()), speedGovTimeUnitChoiceBox.getValue()));

        }

        return settings;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

