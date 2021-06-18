package com.unsa.etf.ugradbeni;

import com.unsa.etf.ugradbeni.controllers.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        StageHandler.openNewWindow(getClass().getResource("/views/ChatWindow.fxml"),"Remote Laboratory",new ChatController());

//        Parent root = FXMLLoader.load(getClass().getResource("/views/ChatWindow.fxml"));
//        primaryStage.setTitle("Remote Laboratory");
//        primaryStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
//        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
