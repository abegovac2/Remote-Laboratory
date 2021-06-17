package com.unsa.etf.ugradbeni.models.mqtt_components;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class UsersManagerMqtt extends MessagingClient{

    public UsersManagerMqtt(String clientId) throws MqttException {
        super(clientId);
    }

    @Override
    protected void messageArrivedAction(String topic, MqttMessage mqttMessage) {
        JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
        String instruction = obj.getString("Instruciton");
        if(instruction != null){
            String userName ="{ \"UserName\": \"" + getClientId() + "\"}";
            try {
                sendMessage(topic, userName, 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else {
            String userName = obj.getString("UserName");
            //nekako zapamtit korisnike
        }
    }
}
