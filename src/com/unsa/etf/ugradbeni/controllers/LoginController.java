package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.alert.AlertMaker;
import com.unsa.etf.ugradbeni.models.User;
import com.unsa.etf.ugradbeni.models.MessagingClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;


import java.io.IOException;
import java.util.regex.Pattern;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class LoginController {
    @FXML
    public TextField fldUsername;

    @FXML
    public Label welcome;

    @FXML
    public AnchorPane anchorPane;

    private User user;


    public LoginController() {
        try {
            user = new User();
        } catch (MqttException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @FXML
    public void initialize() {

        fldUsername.getStyleClass().add("ok");
        fldUsername.setStyle("-fx-border-color: red");

        fldUsername.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (fldUsername.getText().trim().isEmpty()) {
                fldUsername.setStyle("-fx-border-color: red");
            } else {
                fldUsername.setStyle("-fx-border-color: lightgreen");
            }
            fldUsername.getStyleClass().add("ok");
        });
    }

    @FXML
    public void signinAction(ActionEvent actionEvent) {
        String username = fldUsername.getText().trim();
        if (username.isEmpty()) AlertMaker.alertERROR("An error has occured", "You must enter a username!");
        else if (username.length() > 10) {
            fldUsername.setText("");
            AlertMaker.alertERROR("An error has occured", "Username has to be less than 10 characters!");
        } else if (!checkNumberLetter(username)) {
            fldUsername.setText("");
            AlertMaker.alertERROR("An error has occured", "Username can consist only of numbers and letters!");
        } else {
            Stage roomSelectPage = new Stage();
            roomSelectPage.setResizable(false);

            final Parent[] roots = {null};

            Task<Boolean> loadingTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    //check other users
                    boolean isTaken = user.checkForDuplicateUsernames(username);
                    if (isTaken) return false;
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GroupWindow.fxml"));

                    try {
                        user.setupUserMqtt();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    GroupController groupController = new GroupController(user);
                    loader.setController(groupController);
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

                    roomSelectPage.setOnCloseRequest((event) -> {
                        user.disconnectUser();
                        Platform.exit();
                    });

                    return true;
                }
            };

            loadingTask.setOnSucceeded(workerStateEvent -> {
                if (roots[0] == null) {
                    roomSelectPage.close();
                    fldUsername.setText("");
                    AlertMaker.alertERROR("An error has occured", "A user with the username \"" + username + "\" already exist!");
                } else {
                    closeWindow();
                    roomSelectPage.setScene(new Scene(roots[0], USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                    roomSelectPage.show();
                }
            });

            Parent secRoot = null;
            try {
                secRoot = FXMLLoader.load(getClass().getResource("/views/LoadingWindow.fxml"));
                secRoot.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            roomSelectPage.setScene(new Scene(secRoot, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            roomSelectPage.initModality(Modality.APPLICATION_MODAL);
            roomSelectPage.show();
            Thread thread = new Thread(loadingTask);
            thread.start();
        }
    }

    @FXML
    public void closeAction(ActionEvent actionEvent) {
        closeWindow();
    }

    public void closeWindow() {
        ((Stage) fldUsername.getScene().getWindow()).close();
    }

    private Boolean checkNumberLetter(String name) {
        String regex = "^[a-zA-Z0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(name).matches();
    }

}
