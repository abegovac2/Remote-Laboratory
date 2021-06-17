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

    public static Room parseJSON(String jsonStr) {
        JSONObject obj = new JSONObject(jsonStr);
        var roomId = obj.getInt("RoomId");
        var roomName = obj.getString("RoomName");
        return new Room(roomId, roomName);
    }

    @Override
    public String toString() {
        return "{" +
                "\"RoomId\": " + id + " ," +
                "\"RoomName\": \"" + roomName + "\"" +
                "}";
    }
}
