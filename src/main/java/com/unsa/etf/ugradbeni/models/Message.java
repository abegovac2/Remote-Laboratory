package com.unsa.etf.ugradbeni.models;

public class Message {
    private final int id;
    private String message;
    private final int roomId;

    public Message(int id, String message, int roomId) {
        this.id = id;
        this.message = message;
        this.roomId = roomId;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{" +
                "\"Id\": " + id + "," +
                "\"Message\": \"" + message + "\" ," +
                "\"RoomId\": " + roomId +
                "}";
    }

    public int getRoomId() {
        return roomId;
    }
}