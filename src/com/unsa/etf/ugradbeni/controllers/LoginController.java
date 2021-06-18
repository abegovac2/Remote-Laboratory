package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.alert.AlertMaker;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ResourceBundle;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class LoginController {
    @FXML
    public TextField usernamefld;

    public String getUsername() {
        return username;
    }

    public static String username;


    @FXML
    public Label welcome;

    @FXML
    public AnchorPane anchorPane;





    public LoginController(){

    }


    @FXML
    public void initialize(){

        usernamefld.getStyleClass().add("ok");
        usernamefld.setStyle("-fx-border-color: red");


        usernamefld.textProperty().addListener((observableValue, oldValue, newValue) ->{
            if(usernamefld.getText().trim().isEmpty()){
                usernamefld.setStyle("-fx-border-color: red");
                usernamefld.getStyleClass().add("ok");
            }
            else{
                usernamefld.setStyle("-fx-border-color: lightgreen");
                usernamefld.getStyleClass().add("ok");
            }
        } );


    }


    @FXML
    public void signinAction(ActionEvent actionEvent) throws IOException {
        if(usernamefld.getText().trim().isEmpty())
            AlertMaker.alertERROR("Error occured", "Field for username is empty!");

          else {
            username = usernamefld.getText();

            closeWindow();


            Stage homePageStage = new Stage();
            homePageStage.setResizable(false);
            ChatController ctrl = new ChatController();

            final Parent[] roots={null};


            Task<Boolean> loadingTask =new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChatWindow.fxml"));
                    loader.setController(ctrl);
                    try {
                        roots[0] = loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };

            loadingTask.setOnSucceeded(workerStateEvent ->{
                homePageStage.setScene(new Scene(roots[0],USE_COMPUTED_SIZE,USE_COMPUTED_SIZE));
                homePageStage.show();
            });


            Parent secRoot = null;
            try{
                secRoot=FXMLLoader.load(getClass().getResource("/views/LoadingWindow.fxml"));
                secRoot.setVisible(true);
            }catch(IOException e){
                e.printStackTrace();
            }

            homePageStage.setScene(new Scene(secRoot,USE_COMPUTED_SIZE,USE_COMPUTED_SIZE));
//            homePageStage.setTitle(StageEnums.HOME_PAGE.toString());
//            homePageStage.getIcons().add(new Image("/images/bug_image.png"));
            homePageStage.show();
            Thread thread = new Thread(loadingTask);
            thread.start();


        }
    }


    @FXML
    public void closeAction(ActionEvent actionEvent){ closeWindow();}

    public void closeWindow(){
        ((Stage) usernamefld.getScene().getWindow()).close();
    }





}
