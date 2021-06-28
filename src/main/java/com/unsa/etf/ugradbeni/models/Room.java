package com.unsa.etf.ugradbeni.models;

import java.util.Objects;

public class Room {
    private final int id;
    private final String roomName;

    public Room(int id, String roomName) {
        this.roomName = roomName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id == room.id && Objects.equals(roomName, room.roomName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomName);
    }

    @Override
    public String toString() {
        return "{" +
                "\"RoomId\": " + id + " ," +
                "\"RoomName\": \"" + roomName + "\"" +
                "}";
    }
}
