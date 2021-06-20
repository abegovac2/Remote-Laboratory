package com.unsa.etf.ugradbeni.models.mqtt_components;

import com.unsa.etf.ugradbeni.models.MqttOnRecive;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Map;

public class MessagingClient implements MqttCallback {

    private Map<String, MqttOnRecive> onReciveMap;

    public void setOnReciveMap(Map<String, MqttOnRecive> onReciveMap) {
        this.onReciveMap = onReciveMap;
    }

    public Map<String, MqttOnRecive> getOnReciveMap() {
        return onReciveMap;
    }

    private static final String brokerURL = "tcp://broker.hivemq.com:1883";
    private final MqttClient mqttClient;
    private final String clientId;

    public String getClientId() {
        return clientId;
    }

    public MessagingClient(String clientId, Map<String, MqttOnRecive> onReciveMap) throws MqttException {
        this.clientId = clientId;
        this.onReciveMap = onReciveMap;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        mqttClient = new MqttClient(
                brokerURL,
                clientId
        );

        mqttClient.setCallback(this);
        mqttClient.connect(options);
    }

    public void subscribeToTopic(String topic, String message, int qos) throws MqttException {
        mqttClient.subscribe(topic, qos);
        if (message != null) mqttClient.publish(topic, message.getBytes(), 2, false);
    }

    public void unsubscribeFromTopic(String topic, String message) throws MqttException {
        if (message != null) mqttClient.publish(topic, message.getBytes(), 2, false);
        mqttClient.unsubscribe(topic);
    }

    public void sendMessage(String topic, String message, int qos) throws MqttException {
        mqttClient.publish(topic, message.getBytes(), qos, false);
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        try {
            disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        synchronized (this.getClass()) {

            String[] splitTopic = topic.split("/");
            String key = splitTopic[splitTopic.length-2];

            MqttOnRecive function = onReciveMap.get(key);

            if(function != null) {
                try {
                    function.execute(topic, mqttMessage);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //not needed for anything
    }
}
