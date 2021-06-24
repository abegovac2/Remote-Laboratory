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
import javafx.geometry.HPos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class ChatController {
//    @FXML
//    public VBox ChatList;
    @FXML
    public VBox UserList;
    @FXML
    public VBox RoomList;
    @FXML
    public Button SendMessage;
    @FXML
    public TextArea MessageField;
    @FXML
    public Label usernameLbl;

    @FXML
    public ScrollPane scrollChat;

    private String username;
    private final Room currentRoom;
    private MessagingClient chatUser, userActions;
    private SimpleStringProperty messageContent = new SimpleStringProperty("");
    private String currentChatTopic = "";

    private int i = 0;

    private GridPane chat = new GridPane();
    @FXML
    public void initialize() {
        usernameLbl.setText(username);
        MessageField.textProperty().bindBidirectional(messageContent);

        chat.setStyle("-fx-background-color: #2A2E37");
        chat.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        chat.setVgap(5.);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(200);
        chat.getColumnConstraints().add(c1);

//        for (int i = 0; i < 20; i++) {
//            BubbledLabel chatMessage = new BubbledLabel("hi  " + i);
//            chatMessage.getStyleClass().add("chat-bubble");
//            GridPane.setHalignment(chatMessage,   HPos.LEFT );
//            chat.addRow(i, chatMessage);
//        }
    scrollChat.setContent(chat);
//        ScrollPane scroll = new ScrollPane(chat);
        scrollChat.setFitToWidth(true);


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
        }else
        if (message.isEmpty()){
            AlertMaker.alertERROR("","Message field is empty!");
        }else {
            messageContent.set("");
            try {

                Message sendMessage = new Message(-1, "[" + username + "]: " + message, currentRoom.getId());
                chatUser.sendMessage(currentChatTopic, sendMessage.toString(), 0);
            } catch (JSONException | MqttException e) {
                e.printStackTrace();
            }
        }
    }




    private void chatUserSetup() throws MqttException {
        HashMap<String, MqttOnRecive> functions = new HashMap<>();
        chatUser = new MessagingClient(username, functions);
        chatUser.subscribeToTopic(currentChatTopic, null, 0);
        functions.put("message", (String theme, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {

                        BubbledLabel bl = new BubbledLabel();
                        bl.getStyleClass().add("chat-bubble");

                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));

                        System.out.println(msg);

                        String text = msg.getString("Message");
                        bl.setText(text);
                        GridPane.setHalignment(bl,   HPos.LEFT );
//
                        Image img = new Image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR277JJUkU44zs2ln_Bw37bt5V_gY-XWpF3HQ&usqp=CAU");
                        Circle cir2 = new Circle(40,30,20);
                        cir2.setStroke(Color.SEAGREEN);
                        cir2.setFill(Color.SNOW);
                        cir2.setEffect(new DropShadow(+4d, 0d, +2d, Color.DARKSEAGREEN));
                        cir2.setFill(new ImagePattern(img));

                        HBox x = new HBox();
                        x.setSpacing(3);
                        x.getChildren().add(cir2);
                        x.getChildren().add(bl);

                       Platform.runLater(() -> chat.addRow(i++, x));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start());
    }

}
