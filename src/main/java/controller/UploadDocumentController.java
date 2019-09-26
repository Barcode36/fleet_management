package main.java.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.util.AlertUtil;
import main.java.util.DbUtil;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class UploadDocumentController {
    @FXML
    private Label fileName, dialogTitle;
    @FXML
    private TextField documentNameField;

    private Stage stage;
    private File file;

    private String ownerId;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            dialogTitle.requestFocus();
        });

    }
    VBox vBox;
    ScrollPane scrollPane;
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    @FXML
    private void onUploadFile() {
        String documentName = documentNameField.getText();
        if (documentName == null || documentName.isEmpty()) {
            documentName = FilenameUtils.removeExtension(file.getName());
        }
        //get dimensions
        int width = 0, height=0;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (DbUtil.uploadFile(documentName, ownerId, file, width, height)){
           AlertUtil.showAlert("Document Upload", "Document successfully uploaded!", Alert.AlertType.INFORMATION);
        } else{
            AlertUtil.showGenericError();
        }
        stage.close();
    }

    public void setOwner(String ownerId) {
        this.ownerId = ownerId;
        dialogTitle.setText("Document Upload for " + ownerId);
    }

    private void setFile(File file, String fileName) {
        this.file = file;
        this.fileName.setText(file.getName());
        if (fileName == null || fileName.isEmpty()) {
            documentNameField.setText(FilenameUtils.removeExtension(file.getName()));
        } else{
            documentNameField.setText(fileName);
        }
    }

    void showUploadDocumentDialog(String fileName, String ownerId, Region container) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pictures", "*.jpeg", "*.jpg");
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(container.getScene().getWindow());
        if (file != null) {
            if ((double)file.length() / (1024 * 1024) > 5.0) {
                AlertUtil.showAlert("", "File size should be at most 5MB", Alert.AlertType.ERROR);
            } else{
                uploadFile(file, fileName, ownerId, container);
            }
        }
    }

    private void uploadFile(File file, String fileName, String ownerId, Region container) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main/resources/view/upload-document.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initOwner(container.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            UploadDocumentController controller = loader.getController();
            controller.setStage(stage);
            controller.setOwner(ownerId);
            controller.setFile(file, fileName);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

