package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.Reminder;
import main.java.util.Dao;
import main.java.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;

public class RemindersController {
    private ObservableList<Reminder> reminders = FXCollections.observableArrayList();
    @FXML
    private VBox container;
    @FXML
    private TableView<Reminder> tableView;
    @FXML
    private TableColumn<Reminder, String> regNumber, reminder, dueDate;
    @FXML
    private ChoiceBox<Reminder.Category> categoryChoiceBox;
    @FXML
    private Button settingsBtn;
    private MainController main;

    @FXML
    private void initialize() {
        ImageView imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("main/resources/images/settings-outline.png")));
        imageView.setFitHeight(15.0);
        imageView.setFitWidth(15.0);
        settingsBtn.setGraphic(imageView);
        settingsBtn.setOnAction(event -> {
            viewSettings();
        });

        categoryChoiceBox.setItems(FXCollections.observableArrayList(Reminder.Category.values()));
        categoryChoiceBox.getSelectionModel().select(Reminder.Category.ALL);
        categoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterResults(newValue);
        });
        setUpTable();
        getReminders();
    }

    private void getReminders() {
        //get settings
        Task<ObservableList<Reminder>> task = new Task<ObservableList<Reminder>>() {
            @Override
            protected ObservableList<Reminder> call() throws Exception {
                return Dao.getReminders();

            }
        };
        task.setOnSucceeded(event -> {
            reminders =task.getValue();
            tableView.setItems(task.getValue());
            main.setNumReminders(reminders.size());
        });
        new Thread(task).start();

    }

    private void setUpTable() {
        Label label = new Label("No reminders found!");
        label.getStyleClass().add("missing-content");
        tableView.setPlaceholder(label);

        regNumber.setCellValueFactory(param -> param.getValue().regNumberProperty());
        reminder.setCellValueFactory(param -> param.getValue().categoryProperty());
        dueDate.setCellFactory(param -> new TableCell<Reminder, String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex();
                if (index >= 0 && index < tableView.getItems().size()) {
                    LocalDate dueDate = DateUtil.parseDate(tableView.getItems().get(index).getDueDate());
                    if (dueDate != null) {
                        if (dueDate.isBefore(LocalDate.now()) || dueDate.isEqual(LocalDate.now())) {
                            setStyle("-fx-text-fill: #ff4444");
                        }
                    }
                    setText(DateUtil.formatDateLong(dueDate));
                } else {
                    setText(null);
                }
            }
        });
    }

    private void filterResults(Reminder.Category newValue) {
        if (newValue == Reminder.Category.ALL) {
            tableView.setItems(reminders);
        } else{
            ObservableList<Reminder> filtered = FXCollections.observableArrayList();
            for (Reminder reminder : reminders) {
                if (reminder.getCategory() == newValue) {
                    filtered.add(reminder);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void viewSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/settings.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            SettingsController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
            getReminders();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMain(MainController mainController) {
        this.main = mainController;
    }
}

