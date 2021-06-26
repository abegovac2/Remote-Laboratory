package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.models.Room;
import com.unsa.etf.ugradbeni.models.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class GroupController {
    @FXML
    public Label user;

    @FXML
    public FlowPane pane;

    @FXML
    public TextField date;

    private DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private com.unsa.etf.ugradbeni.models.User User;


    @FXML
    public void initialize() {
        date.setText(LocalDate.now().format(myFormatObj));
        user.setText(User.getUserName());

        List<Room> rooms = new ArrayList<>(User.getActiveRooms());

        for (Room room : rooms) {
            Button group = new Button(room.getRoomName());
            group.setMinWidth(200);
            group.setMinHeight(200);
            group.setOnAction((ActionEvent event) -> openNewChat(room));
            pane.getChildren().add(group);
        }

    }

    public GroupController(User user) {
        this.User = user;
    }

    public void openNewChat(Room room) {
        Stage newChatStage = new Stage();
        newChatStage.setResizable(false);

        final Parent[] roots = {null};

        Task<Boolean> loadingTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    User.getMessagesForRoom(room);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChatWindow.fxml"));
                ChatController chat = new ChatController(User, room);
                loader.setController(chat);

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


                newChatStage.setOnCloseRequest((event) -> {
                    User.disconnectUser();
                    Platform.exit();
                });

                return true;
            }
        };

        loadingTask.setOnSucceeded(workerStateEvent -> {
            closeWindow();
            newChatStage.setScene(new Scene(roots[0], USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
            newChatStage.show();
        });

        Parent secRoot = null;
        try {
            secRoot = FXMLLoader.load(getClass().getResource("/views/LoadingWindow.fxml"));
            secRoot.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newChatStage.setScene(new Scene(secRoot, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        newChatStage.initModality(Modality.APPLICATION_MODAL);
        newChatStage.show();
        Thread thread = new Thread(loadingTask);
        thread.start();

    }

    public void closeWindow() {
        ((Stage) user.getScene().getWindow()).close();
    }


}
