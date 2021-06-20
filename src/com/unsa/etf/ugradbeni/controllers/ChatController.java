package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.alert.AlertMaker;
import com.unsa.etf.ugradbeni.bubble.BubbledLabel;
import com.unsa.etf.ugradbeni.models.Message;
import com.unsa.etf.ugradbeni.models.MqttOnRecive;
import com.unsa.etf.ugradbeni.models.Room;
import com.unsa.etf.ugradbeni.models.ThemesMqtt;
import com.unsa.etf.ugradbeni.models.mqtt_components.MessagingClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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

    private String username;
    private final Room currentRoom;
    private MessagingClient chatUser, userActions;
    private SimpleStringProperty messageContent = new SimpleStringProperty("");
    private String currentChatTopic = "";


    @FXML
    public void initialize() {
        usernameLbl.setText(username);
        MessageField.textProperty().bindBidirectional(messageContent);

        try {
            chatUserSetup();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public ChatController(String username, MessagingClient userActions, Room currentRoom) {
        this.username = username;
        this.userActions = userActions;
        this.currentRoom = new Room(1, "soba");
        currentChatTopic = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + this.currentRoom.getRoomName();
    }


    @FXML
    public void sendAction(ActionEvent actionEvent) {
        String message = messageContent.get().trim();

        if (message.length() > 255){
            AlertMaker.alertINFORMATION("Notice","Message can't have more than 255 character.");
        }
        if (message.isEmpty()){
            AlertMaker.alertERROR("","Message field is empty!");
        }
        messageContent.set("");
        try {
            Message sendMessage = new Message(-1, "[" + username + "]: " + message, currentRoom.getId());
            chatUser.sendMessage(currentChatTopic, sendMessage.toString(), 0);
        } catch (JSONException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void chatUserSetup() throws MqttException {
        HashMap<String, MqttOnRecive> functions = new HashMap<>();
        chatUser = new MessagingClient(username, functions);
        chatUser.subscribeToTopic(currentChatTopic, null, 0);
        functions.put("message", (String theme, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {
                        // Button newMessage = new Button();
                        BubbledLabel bl = new BubbledLabel();

                        //
                        //ovdje treba umjesto button-a stavit nesto drugo, tj.ljepse
                        //


                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));
                        String text = msg.getString("Message");
                        //newMessage.setText(text);
                        bl.setText(text);
                        Platform.runLater(() -> ChatList.getChildren().add(bl));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

}
