package com.unsa.etf.ugradbeni.models.mqtt_components;

import org.eclipse.paho.client.mqttv3.*;

public abstract class MessagingClient implements MqttCallback {
    private static final String brokerURL = "tcp://broker.hivemq.com:1883";
    private final MqttClient mqttClient;
    private final String clientId;

    public String getClientId() {
        return clientId;
    }

    public MessagingClient(String clientId) throws MqttException {
        this.clientId = clientId;
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

            messageArrivedAction(topic, mqttMessage);

        }
    }

    //all classes derived from this one does the same thing, except when dealing with arrived messages
    protected abstract void messageArrivedAction(String topic, MqttMessage mqttMessage);

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //not needed for anything
    }
}
