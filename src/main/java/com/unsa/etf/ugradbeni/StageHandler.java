package com.unsa.etf.ugradbeni;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class StageHandler {

    public static final String ICON_IMAGE_LOC = "/pictures/etf_logo.png";

    public static Stage openNewWindow(URL loc, String title, Object controller) {
        Stage parentStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(loc);
            loader.setController(controller);
            Parent root = loader.load();
            parentStage.setTitle(title);
            parentStage.setResizable(false);
            parentStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            setStageIcon(parentStage);
            parentStage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return parentStage;
    }

    private static  void setStageIcon(Stage stage) {
        stage.getIcons().add(new Image(ICON_IMAGE_LOC));
    }

}
