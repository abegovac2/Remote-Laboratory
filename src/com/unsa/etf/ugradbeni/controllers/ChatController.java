package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.alert.AlertMaker;
import com.unsa.etf.ugradbeni.bubble.BubbledLabel;
import com.unsa.etf.ugradbeni.models.*;
import com.unsa.etf.ugradbeni.models.MessagingClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class ChatController {
    @FXML
    public VBox ChatList;
    @FXML
    public VBox UserList;
    @FXML
    public VBox RoomList;
    @FXML
    public Button SendMessage;
    @FXML
    public TextField MessageField;
    @FXML
    public Label usernameLbl;

    @FXML
    public Button btnUsersRefresh;
    @FXML
    public Button btnRoomRefresh;

    private MessagingClient chatUser;
    private SimpleStringProperty messageContent = new SimpleStringProperty("");
    private String currentChatTopic;

    private User user;
    private List<Message> last10;
    private final Room currentRoom;


    @FXML
    public void initialize() {
        usernameLbl.setText(user.getUserName());
        MessageField.textProperty().bindBidirectional(messageContent);

        try {
            chatUserSetup();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (int i = last10.size() - 1; i >= 0; --i) {
            Button bl = new Button();
            bl.setText(last10.get(i).getMessage());
            ChatList.getChildren().add(bl);
        }

        for (String user1 : user.getConnectedUsers()) {
            Button bl = new Button();
            bl.setText(user1);
            UserList.getChildren().add(bl);
        }

        for (Room room : user.getActiveRooms()) {
            if (room.equals(currentRoom)) continue;
            Button bl = new Button();
            bl.setText(room.getRoomName());
            bl.setOnAction((ActionEvent event) -> openNewChat(room));
            RoomList.getChildren().add(bl);
        }
    }

    public ChatController(List<Message> lastTenMsg, User user, Room room) {
        this.last10 = user.getLast10();
        this.user = user;
        this.currentRoom = room;
        currentChatTopic = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + this.currentRoom.getRoomName();
    }


    @FXML
    public void sendAction(ActionEvent actionEvent) {
        String message = messageContent.get().trim();

        if (message.length() > 255) {
            AlertMaker.alertINFORMATION("Notice", "Message can't have more than 255 character.");
            return;
        }
        if (message.isEmpty()) {
            AlertMaker.alertERROR("", "Message field is empty!");
            return;
        }
        messageContent.set("");
        try {
            Message sendMessage = new Message(-1, "[" + user.getUserName() + "]: " + message, currentRoom.getId());
            chatUser.sendMessage(currentChatTopic, sendMessage.toString(), 0);
        } catch (JSONException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void chatUserSetup() throws MqttException {
        HashMap<String, MqttOnRecive> functions = new HashMap<>();
        chatUser = new MessagingClient(user.getUserName(), functions);
        chatUser.subscribeToTopic(currentChatTopic, null, 0);
        functions.put("message", (String theme, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {
                        Button bl = new Button();

                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));
                        String text = msg.getString("Message");
                        bl.setText(text);
                        Platform.runLater(() -> ChatList.getChildren().add(bl));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

    public void openNewChat(Room room) {
        Stage newChatStage = new Stage();
        newChatStage.setResizable(false);

        final Parent[] roots = {null};

        Task<Boolean> loadingTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                //check other users
                List<Message> lastTenMsg = null;
                try {
                    lastTenMsg = user.getMessagesForRoom(room);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChatWindow.fxml"));
                ChatController chat = new ChatController(lastTenMsg, user, room);
                loader.setController(chat);
                try {
                    chatUser.disconnect();
                    roots[0] = loader.load();
                } catch (IOException | MqttException e) {
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
        ((Stage) ChatList.getScene().getWindow()).close();
    }

    @FXML
    public void roomRefresh(ActionEvent event) {
        user.refreshActiveRooms();
        new Thread(() -> {
            Platform.runLater(() -> {
                RoomList.getChildren().clear();
                RoomList.getChildren().add(btnRoomRefresh);
            });
            for (Room room : user.getActiveRooms()) {
                if (room.equals(currentRoom)) continue;
                Button bl = new Button();
                bl.setText(room.getRoomName());
                bl.setOnAction((ActionEvent event1) -> openNewChat(room));
                Platform.runLater(() -> RoomList.getChildren().add(bl));
            }
        }).start();

    }

    @FXML
    public void userRefresh(ActionEvent event) {
        user.refreshConnectedUsers();
        new Thread(() -> {
            Platform.runLater(() -> {
                UserList.getChildren().clear();
                UserList.getChildren().add(btnUsersRefresh);
            });
            for (String user1 : user.getConnectedUsers()) {
                Button bl = new Button();
                bl.setText(user1);
                Platform.runLater(() -> UserList.getChildren().add(bl));
            }
        }).start();
    }


}
