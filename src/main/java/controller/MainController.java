package main.java.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.Main;
import main.java.model.Permission;
import main.java.model.Reminder;
import main.java.util.Dao;
import main.java.util.DateUtil;

import java.time.LocalTime;

public class MainController {
    private static final String APPLICATION_VERSION = "Version 1.0";
    @FXML
    private StackPane numRemindersPane;
    @FXML
    private Label loginTime, version, numRemindersLabel;
    @FXML
    private MenuButton currentUserBtn;
    @FXML
    private VBox container;
    @FXML
    private HBox dashboard;
    @FXML
    private HBox reminders;
    private Main main;
    private HBox clickedItem;

    @FXML
    private void initialize() {
        for (Node node : container.getChildren()) {
            node.setOnMouseClicked(event -> handleClicked((HBox) event.getSource()));
        }
        clickedItem = dashboard;
        setSelected(true);
        getNumReminders();
        loginTime.setText("Logged in at " + DateUtil.formatTime(LocalTime.now()));
        version.setText(APPLICATION_VERSION);
    }

    private void getNumReminders() {
        Task<ObservableList<Reminder>> task = new Task<ObservableList<Reminder>>() {
            @Override
            protected ObservableList<Reminder> call() {
                return Dao.getReminders();
            }
        };
        task.setOnSucceeded(event -> {
            setNumReminders(task.getValue().size());
        });
        new Thread(task).start();
    }

    private void handleClicked(HBox hBox) {
        setSelected(false);
        main.loadModule(hBox.getId());
        clickedItem = hBox;
        setSelected(true);
    }

    private void setSelected(boolean selected) {
        if (clickedItem != null) {
            if (selected) {
                clickedItem.getStyleClass().remove("bg-primary");
                clickedItem.getStyleClass().add("bg-accent");
            } else {
                clickedItem.getStyleClass().remove("bg-accent");
                clickedItem.getStyleClass().add("bg-primary");
            }
        }
    }

    @FXML
    private void onLogOut() {
        main.logOut();
    }

    public void setMain(Main main) {
        this.main = main;
        main.setMainController(this);
        currentUserBtn.setText(Main.currentUser.getFirstName() + " " + Main.currentUser.getLastName());
        reminders.setVisible(Main.userPermissions.get(Permission.VIEW_REMINDERS));
        reminders.setManaged(Main.userPermissions.get(Permission.VIEW_REMINDERS));
    }

    public Main getMain() {
        return main;
    }

    void setNumReminders(int numReminders) {
        if (numReminders != 0) {
            numRemindersPane.setVisible(true);
            numRemindersLabel.setText(Integer.toString(numReminders));
        } else {
            numRemindersPane.setVisible(false);
        }
    }
}
