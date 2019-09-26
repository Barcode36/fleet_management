package main;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.controller.LoginController;
import main.java.controller.MainController;
import main.java.controller.RemindersController;
import main.java.model.Permission;
import main.java.model.User;
import main.java.util.DbUtil;

import java.io.IOException;
import java.util.Map;

public class Main extends Application {
    public static User currentUser;
    public static Map<Permission, Boolean> userPermissions;
    @FXML
    private AnchorPane mainPane;
    public static Stage stage;
    private MainController mainController;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void setUserPermissions(Map<Permission, Boolean> userPermissions) {
        Main.userPermissions = userPermissions;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        if (loginSuccessful()) {
            logEvent("Logged in");
            showContent();
        }
    }

    private void logEvent(String message) {
        Task task = new Task() {
            @Override
            protected Object call() {
                DbUtil.saveActivityLog(message);
                return null;
            }
        };
        new Thread(task).start();
    }

    private void showContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/main.fxml"));
            VBox vBox = loader.load();

            SplitPane splitPane = (SplitPane) vBox.getChildren().get(1);
            mainPane = (AnchorPane) splitPane.getItems().get(1);

            //stage
            stage = new Stage();
            stage.setMaximized(true);
            stage.setScene(new Scene(vBox));
            MainController controller = loader.getController();
            controller.setMain(this);
            stage.show();

            loadModule("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModule(String resourceFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/" + resourceFileName + ".fxml"));
            Node node = loader.load();

            mainPane.getChildren().clear();
            mainPane.getChildren().add(node);

            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node,0.0);

            if (resourceFileName.equals("reminders")) {
                RemindersController controller = loader.getController();
                controller.setMain(mainController);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loginSuccessful() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/login.fxml"));

            Stage stage = new Stage();
            stage.setTitle("System Login");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);

            LoginController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
            return controller.loginSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void logOut() {
        logEvent("Logged in");
        stage.close();
        if (loginSuccessful()) {
            logEvent("Logged in");
            showContent();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
