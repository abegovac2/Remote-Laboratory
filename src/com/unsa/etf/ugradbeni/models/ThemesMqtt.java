package com.unsa.etf.ugradbeni.models;

public enum ThemesMqtt {
    BASE("project225883/us/etf"),
    MESSAGE("/message"),
    ALL_MESSAGES_FROM_ROOMS("/message/+"),
    SEND_REFRESH("/refresh/send"),
    RECIVE_REFRESH("/refresh/recive"),
    ROOM_REFRESH_SEND("/room/send/refresh"),
    ROOM_REFRESH_RECIVE("/room/recive/refresh"),
    ROOM_ADD_NEW("/room/add/new"),
    USER_SEND("/user/send"),
    USER_RECIVE("/user/recive"),
    USER("/user"),
    CHECK("/check"),
    TAKEN("/taken"),
    USER_CONNECTED("/user/newUser/connected"),
    USER_DISCONNECTED("/user/newUser/disconnected"),
    USER_REFRESH_SEND("/user/refresh/send"),
    USER_REFRESH_RECIVE("/user/refresh/recive"),

    //trebaju jos za mbed
    TO_PORT("/mbed/port"),
    SEND_INFO("/mbed/info/send"),
    RECIVE_INFO("/mbed/info/recive")
    ;


    private String value;

    ThemesMqtt(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
