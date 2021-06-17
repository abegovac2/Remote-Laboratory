package com.unsa.etf.ugradbeni.models.mqtt_components;

import com.unsa.etf.ugradbeni.models.Message;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class MessageRefreshMqtt extends MessagingClient{

    private VBox ChatList;

    public MessageRefreshMqtt(String clientId, VBox ChatList) throws MqttException {
        super(clientId);
        this.ChatList = ChatList;
    }

    @Override
    protected void messageArrivedAction(String topic, MqttMessage mqttMessage) {
        JSONArray array = new JSONArray(new String(mqttMessage.getPayload()));
        for(int i = 0; i < array.length(); ++i){
            String msg = array.getJSONObject(i).getString("Message");

            Label newMessage = new Label();
            newMessage.setText(msg);
            ChatList.getChildren().add(newMessage);
        }
    }
}
