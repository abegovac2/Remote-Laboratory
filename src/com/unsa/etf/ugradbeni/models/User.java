package com.unsa.etf.ugradbeni.models;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class User {
    private String userName;
    private MessagingClient userClient;
    private MessagingClient chatClient;
    private Set<String> connectedUsers;
    private Set<Room> activeRooms;
    private List<Message> last10;

    public User() throws MqttException {
        this.userClient = new MessagingClient(UUID.randomUUID().toString(), new HashMap<>());
        connectedUsers = new HashSet<>();
        activeRooms = new HashSet<>();
    }

    private void setupUserNameCheck() throws MqttException {
        String taken = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.TAKEN + "/" + userName;
        String check = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.CHECK + "/" + userName;

        userClient.getOnReciveMap().put("check", (String topic, MqttMessage mqttMessage) -> {
            try {
                userClient.sendMessage(taken, "{}", 0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });

        //has to be immediately subscribed to, so we avoid a short period of time when the user is still connecting
        userClient.subscribeToTopic(check, null, 0);
    }

    public void setupConnectDisconnect(VBox userList) throws MqttException {
        String connected = "" + ThemesMqtt.BASE + ThemesMqtt.USER_CONNECTED;
        String disconnected = "" + ThemesMqtt.BASE + ThemesMqtt.USER_DISCONNECTED;

        userClient.getOnReciveMap().put("connected", (String topic, MqttMessage mqttMessage) ->
                //every time a new user is connected he is added to a list of currently active users
                new Thread(() -> {
                    String[] topicParts = topic.split("/");
                    String userName = topicParts[topicParts.length - 1];
                    if (userList != null) {
                        Button newUser = new Button(userName);
                        Platform.runLater(() -> userList.getChildren().add(newUser));
                    }
                    connectedUsers.add(userName);
                }).start()
        );

        userClient.getOnReciveMap().put("disconnected", (String topic, MqttMessage mqttMessage) ->
                //every time a user is disconnected he is omitted form the list of active users
                new Thread(() -> {
                    String[] topicParts = topic.split("/");
                    String userName = topicParts[topicParts.length - 1];
                    if (userList != null) {
                        Platform.runLater(() ->
                                userList.getChildren().removeIf((Node node) ->
                                        node instanceof Button && ((Button) node).getText().equals(userName)));
                    }
                    connectedUsers.remove(topicParts[topicParts.length - 1]);
                }).start()
        );

        if (userList == null) {
            userClient.sendMessage(connected + "/" + userName, "{}", 0);
            userClient.subscribeToTopic(connected + "/+", null, 0);
            userClient.subscribeToTopic(disconnected + "/+", null, 0);
        }
    }

    public void disconnectUser() {
        String disconnected = "" + ThemesMqtt.BASE + ThemesMqtt.USER_DISCONNECTED + "/" + userName;
        try {
            userClient.sendMessage(disconnected, "{}", 0);
            userClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setupRefreshUsers() throws MqttException {
        String recive = "" + ThemesMqtt.BASE + ThemesMqtt.USER_RECIVE;
        String send = "" + ThemesMqtt.BASE + ThemesMqtt.USER_SEND;

        userClient.getOnReciveMap().put("user", (String topic, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {
                        //to stop infinite loops
                        if (topic.endsWith("recive")) return;
                        String message = "{ \"UserName\": \"" + userName + "\"}";
                        userClient.sendMessage(recive, message, 0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }).start()
        );

        userClient.subscribeToTopic(send, null, 0);
    }

    public void setupUserMqtt() throws MqttException {
        HashMap<String, MqttOnRecive> mapOfFunctions = new HashMap<>();
        userClient.setOnReciveMap(mapOfFunctions);

        setupUserNameCheck();
        setupConnectDisconnect(null);
        setupRefreshUsers();

        refreshConnectedUsers();
        refreshActiveRooms();

    }

    public Set<String> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(Set<String> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public Set<Room> getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(Set<Room> activeRooms) {
        this.activeRooms = activeRooms;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public MessagingClient getUserClient() {
        return userClient;
    }

    public void setUserClient(MessagingClient userClient) {
        this.userClient = userClient;
    }

    public MessagingClient getChatClient() {
        return chatClient;
    }

    public void setChatClient(MessagingClient chatClient) {
        this.chatClient = chatClient;
    }

    //call this function within another thread
    public void refreshConnectedUsers() {
        try {
            String recive = "" + ThemesMqtt.BASE + ThemesMqtt.USER_RECIVE;
            String send = "" + ThemesMqtt.BASE + ThemesMqtt.USER_SEND;

            MqttOnRecive function = userClient.getOnReciveMap().get("user");
            userClient.unsubscribeFromTopic(send, null);

            userClient.getOnReciveMap().put("user", (String topic, MqttMessage mqttMessage) ->
                    new Thread(() -> {
                        try {
                            JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
                            String connectedUser = obj.getString("UserName");
                            connectedUsers.add(connectedUser);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).start()
            );

            userClient.subscribeToTopic(recive, null, 0);
            userClient.sendMessage(send, "{}", 0);

            //stops the tread so it can recive all refresh actions
            Thread.sleep(5000);

            userClient.unsubscribeFromTopic(recive, null);
            userClient.getOnReciveMap().put("user", function);
            userClient.subscribeToTopic(send, null, 0);

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
        connectedUsers.add(userName);
    }

    //call this function within another thread
    public void refreshActiveRooms() {
        try {
            activeRooms = new HashSet<>();
            String send = "" + ThemesMqtt.BASE + ThemesMqtt.ROOM_REFRESH_SEND;
            String recive = "" + ThemesMqtt.BASE + ThemesMqtt.ROOM_REFRESH_RECIVE;

            userClient.getOnReciveMap().put("recive", (String topic, MqttMessage mqttMessage) ->
                    new Thread(() -> {
                        JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
                        JSONArray array = obj.getJSONArray("ListOfRooms");

                        for (int i = 0; i < array.length(); ++i) {
                            obj = array.getJSONObject(i);
                            String roomName = obj.getString("RoomName");
                            int roomId = obj.getInt("RoomId");
                            Room room = new Room(roomId, roomName);
                            activeRooms.add(room);
                        }
                    }).start()
            );

            userClient.subscribeToTopic(recive, null, 0);
            userClient.sendMessage(send, "{}", 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            userClient.unsubscribeFromTopic(recive, null);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getLast10() {
        return last10;
    }

    public void setLast10(List<Message> last10) {
        this.last10 = last10;
    }

    //call this function within another thread
    public List<Message> getMessagesForRoom(Room room) throws MqttException {
        last10 = new ArrayList<>();

        String send = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + room.getRoomName() + ThemesMqtt.SEND_REFRESH;
        String recive = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + room.getRoomName() + ThemesMqtt.RECIVE_REFRESH;

        userClient.getOnReciveMap().put("refresh", (String topic, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    JSONObject obj = new JSONObject(new String(mqttMessage.getPayload()));
                    JSONArray array = obj.getJSONArray("ListOfMessages");

                    for (int i = 0; i < array.length(); ++i) {
                        obj = array.getJSONObject(i);
                        int id = obj.getInt("Id");
                        String message = obj.getString("Message");
                        int roomId = obj.getInt("RoomId");
                        Message messageObj = new Message(id, message, roomId);
                        last10.add(messageObj);
                    }
                }).start()
        );

        userClient.subscribeToTopic(recive, null, 0);
        userClient.sendMessage(send, room.toString(), 0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userClient.unsubscribeFromTopic(recive, null);

        return last10;
    }

    public boolean checkForDuplicateUsernames(String username) {
        AtomicBoolean isTaken = new AtomicBoolean(false);
//        MessagingClient user = null;
        String check, taken;
        try {
            Map<String, MqttOnRecive> mapOfFunctions = userClient.getOnReciveMap();
            mapOfFunctions.put("taken", (String topic, MqttMessage mqttMessage) -> isTaken.set(true));
//            user = new MessagingClient(UUID.randomUUID().toString(), mapOfFunctions);

            check = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.CHECK + "/" + username;
            taken = "" + ThemesMqtt.BASE + ThemesMqtt.USER + ThemesMqtt.TAKEN + "/" + username;

//            user.subscribeToTopic(taken, null, 0);
            userClient.subscribeToTopic(taken, null, 0);
//            user.sendMessage(check, "{}", 0);
            userClient.sendMessage(check, "{}", 0);
            //waits 2sec to get a response
            Thread.sleep(2000);
//            user.unsubscribeFromTopic(taken, null);
            userClient.unsubscribeFromTopic(taken, null);

            mapOfFunctions.remove("taken");
            if(!isTaken.get()) userName = username;
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
        return isTaken.get();
    }
}
