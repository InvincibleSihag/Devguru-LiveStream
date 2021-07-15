package com.krash.devguruuastros.Models;

public class LiveStreamMessage {
    String message;
    String userFirebaseId;
    String userAgoraId;
    public LiveStreamMessage(String message, String userAgoraId, String userFirebaseId)
    {
        this.message = message;
        this.userAgoraId = userAgoraId;
        this.userFirebaseId = userFirebaseId;
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
}
