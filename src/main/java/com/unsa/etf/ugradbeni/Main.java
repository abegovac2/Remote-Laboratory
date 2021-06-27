package com.unsa.etf.ugradbeni;

import com.unsa.etf.ugradbeni.controllers.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        StageHandler.openNewWindow(getClass().getResource("/views/LoginWindow.fxml"), "Remote Laboratory", new LoginController());
      //  StageHandler.openNewWindow(getClass().getResource("/views/GroupWindow.fxml"), "Remote Laboratory", new GroupController());
      //  StageHandler.openNewWindow(getClass().getResource("/views/ChatWindow.fxml"), "Remote Laboratory", new ChatController("",null,null));

//        Parent root = FXMLLoader.load(getClass().getResource("/views/ChatWindow.fxml"));
//        primaryStage.setTitle("Remote Laboratory");
//        primaryStage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
//        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
