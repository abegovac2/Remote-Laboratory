package com.unsa.etf.ugradbeni.models.mqtt_components;

import com.unsa.etf.ugradbeni.controllers.ChatController;
import com.unsa.etf.ugradbeni.models.Message;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ChatUserMqtt extends MessagingClient{

    private VBox ChatList;
    //private String roomName;

    public ChatUserMqtt(String clientId, VBox ChatList/*, String roomName*/) throws MqttException {
        super(clientId);
        //this.roomName = roomName;
        this.ChatList = ChatList;
    }

    @Override
    protected void messageArrivedAction(String topic, MqttMessage mqttMessage) {
        Message msg = Message.parseJSON(new String(mqttMessage.getPayload()));

        //adds users
        Label newMessage = new Label();
        newMessage.setText(msg.getMessage());
        ChatList.getChildren().add(newMessage);
    }
}
