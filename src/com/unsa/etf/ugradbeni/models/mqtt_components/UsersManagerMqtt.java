package com.unsa.etf.ugradbeni.models.mqtt_components;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.UUID;

public class UsersManagerMqtt extends MessagingClient {

    public boolean isUsed() {
        return isUsed;
    }

    private boolean isUsed = false;

    public UsersManagerMqtt(String clientId) throws MqttException {
        super(clientId);
    }
//sendMyName/refresh
//sendMyName/send

    // 1 2 3
    /*
     * {
     * "UserName" : "meho"
     * }
     *
     * */
    @Override
    protected void messageArrivedAction(String topic, MqttMessage mqttMessage) {
        synchronized (this.getClass()) {
            if (!topic.endsWith(getClientId())) {
                String message = "{ \"IsUsed\": true }";
                try {
                    sendMessage(topic, message, 0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } else if (topic.endsWith("recive")) {
                JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
                this.isUsed = obj.getBoolean("IsUsed");
            }
        }
    }

    //ovo treba izvrsit se kad se sign in klikne
    private void test() {
        String imeKojeJeUnioKoirnisk = "";
        imeKojeJeUnioKoirnisk = imeKojeJeUnioKoirnisk.trim();
        String base = "project225883/us/etf/";
        /*if (imeKojeJeUnioKoirnisk.length() > 10) {
            //vrati da opet upise
        }
        if (imeKojeJeUnioKoirnisk.isEmpty()) {
            //vrati da opet upise
        }*/
        try {
            UsersManagerMqtt user = new UsersManagerMqtt(UUID.randomUUID().toString());
            user.subscribeToTopic(base + "users/" + imeKojeJeUnioKoirnisk + "result", null, 0);
            user.sendMessage(base + "users/" + imeKojeJeUnioKoirnisk, "{}", 0);
            //waits 1sec to get a response
            Thread.sleep(1000);
            if (user.isUsed()) {
                //ovdje udje kad postiji lik sa tim imenom

            } else {
                //logika otvara se novi chat

            }

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
