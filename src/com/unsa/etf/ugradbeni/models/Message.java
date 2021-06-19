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

    @Override
    public String toString() {
        return "{" +
                "\"Id\": " + id + "," +
                "\"Message\": \"" + message + "\" ," +
                "\"RoomId\": " + roomId +
                "}";
    }
}