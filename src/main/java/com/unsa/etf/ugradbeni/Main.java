package com.unsa.etf.ugradbeni;

import com.unsa.etf.ugradbeni.controllers.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        StageHandler.openNewWindow(getClass().getResource("/views/LoginWindow.fxml"), "Remote Laboratory", new LoginController());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
