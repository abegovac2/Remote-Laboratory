package com.unsa.etf.ugradbeni.models;

import org.json.JSONObject;

public class Message {
    private int id;
    private String message;
    private int roomId;

    public Message(int id, String message, int roomId) {
        this.id = id;
        this.message = message;
        this.roomId = roomId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }


    public static Message parseJSON(String jsonStr){
        JSONObject obj = new JSONObject(jsonStr);
        String message = obj.getString("Message");
        int roomId = obj.getInt("RoomId");
        return new Message(-1, message, roomId);
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": " + id + "," +
                "\"Message\": \"" + message + "\" ," +
                "\"RoomId\": " + roomId +
                "}";
    }
}