package com.unsa.etf.ugradbeni.models;

import org.json.JSONObject;

public class Room {
    private int id;
    private String roomName;

    public Room(int id, String roomName) {
        this.roomName = roomName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return "{" +
                "\"RoomId\": " + id + " ," +
                "\"RoomName\": \"" + roomName + "\"" +
                "}";
    }
}
