package com.unsa.etf.ugradbeni.models.mqtt_components;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class RoomManagerMqtt extends MessagingClient{

    //ovdje treba parametar za view u kojem se bira soba
    public RoomManagerMqtt(String clientId) throws MqttException {
        super(clientId);
    }

    @Override
    protected void messageArrivedAction(String topic, MqttMessage mqttMessage) {
        JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
        String instruction = obj.getString("Instruciton");
        //dodaj logiku za citanje svih dobivenih soba

    }
}
