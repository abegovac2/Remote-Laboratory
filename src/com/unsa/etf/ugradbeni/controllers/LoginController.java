package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.alert.AlertMaker;
import com.unsa.etf.ugradbeni.models.User;
import com.unsa.etf.ugradbeni.models.MessagingClient;
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

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class LoginController {
    @FXML
    public TextField fldUsername;

    @FXML
    public Label welcome;

    @FXML
    public AnchorPane anchorPane;


    public LoginController() {
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
            AlertMaker.alertERROR("An error has occured", "Username has to be less than 10 characters");
        } else {
            Stage homePageStage = new Stage();
            homePageStage.setResizable(false);

            final Parent[] roots = {null};

            Task<Boolean> loadingTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    //check other users
                    MessagingClient userActions = User.checkForDuplicateUsernames(username);
                    if (userActions == null) return false;
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GroupWindow.fxml"));
                    User user = new User(username, userActions);

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
                    return true;
                }
            };

            loadingTask.setOnSucceeded(workerStateEvent -> {
                if (roots[0] == null) {
                    homePageStage.close();
                    fldUsername.setText("");
                    AlertMaker.alertERROR("An error has occured", "A user with the username \"" + username + "\" already exist!");
                } else {
                    closeWindow();
                    homePageStage.setScene(new Scene(roots[0], USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                    homePageStage.show();
                }
            });

            Parent secRoot = null;
            try {
                secRoot = FXMLLoader.load(getClass().getResource("/views/LoadingWindow.fxml"));
                secRoot.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            homePageStage.setScene(new Scene(secRoot, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            homePageStage.initModality(Modality.APPLICATION_MODAL);
            homePageStage.show();
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

    /*private MessagingClient checkForDuplicateUsernames(String username) {
        AtomicBoolean isTaken = new AtomicBoolean(false);
        MessagingClient user = null;
        String check = "", taken = "";
        try {
            HashMap<String, MqttOnRecive> mapOfFunctions = new HashMap<>();
            mapOfFunctions.put("taken", (String topic, MqttMessage mqttMessage) -> isTaken.set(true));
            user = new MessagingClient(UUID.randomUUID().toString(), mapOfFunctions);

            check = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.CHECK + "/" + username;
            taken = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.TAKEN + "/" + username;

            user.subscribeToTopic(taken, null, 0);
            user.sendMessage(check, "{}", 0);
            //waits 2sec to get a response
            Thread.sleep(2000);
            user.unsubscribeFromTopic(taken, null);

            if (isTaken.get()) {
                user.disconnect();
                user = null;
            } else {
                mapOfFunctions.remove("taken");
            }

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }*/

}
