package com.krash.devguruuastros.Models;

public class LiveStreamMessage {
    String message;
    String userFirebaseId;
    String userAgoraId;
    String name;
    public LiveStreamMessage(String message, String name, String userAgoraId, String userFirebaseId)
    {
        this.message = message;
        this.userAgoraId = userAgoraId;
        this.userFirebaseId = userFirebaseId;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public String getUserAgoraId() {
        return userAgoraId;
    }

    public String getUserFirebaseId() {
        return userFirebaseId;
    }

    public String getName() {
        return name;
    }
}
