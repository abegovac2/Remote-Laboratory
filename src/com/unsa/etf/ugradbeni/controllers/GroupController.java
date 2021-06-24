package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.bubble.BubbledLabel;
import com.unsa.etf.ugradbeni.models.MqttOnRecive;
import com.unsa.etf.ugradbeni.models.Room;
import com.unsa.etf.ugradbeni.models.ThemesMqtt;
import com.unsa.etf.ugradbeni.models.mqtt_components.MessagingClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupController {
    @FXML
    public Label user;

    @FXML
    public FlowPane pane;

    @FXML
    public TextField date;

    private String username;
    private DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private final Room currentRoom;
    private MessagingClient chatUser, userActions;
    private SimpleStringProperty messageContent = new SimpleStringProperty("");
    private String currentChatTopic = "";


    @FXML
    public void initialize() throws MqttException {
        date.setText(LocalDate.now().format(myFormatObj));
        user.setText(username);

        currentChatTopic = "" + ThemesMqtt.BASE + ThemesMqtt.ROOM_REFRESH_RECIVE;

        HashMap<String, MqttOnRecive> functions = new HashMap<>();
        chatUser = new MessagingClient(username, functions);
        chatUser.subscribeToTopic(currentChatTopic, null, 0);

        functions.put("receive", (String theme, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {



                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));
//                        String text = msg.getString("ListOfRooms");
                        System.out.println("evo -> " + msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start());


        //ovako dodajemo sobe
        for(int i=1;i<=10;i++){
            Button grupa = new Button("soba "+i);
            grupa.setMinWidth(200);
            grupa.setMinHeight(200);
            pane.getChildren().add(grupa);
        }

    }

    public GroupController(String username){
        this.username= username;
        this.currentRoom = new Room(1, "soba");
    }


}
