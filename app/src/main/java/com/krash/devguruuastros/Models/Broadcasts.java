package com.krash.devguruuastros.Models;

import com.google.firebase.database.DatabaseReference;

public class Broadcasts {
    private String channelName;
    private String token;
    private String live;
    private String userId;
    private String astrologerName;

    public Broadcasts(String channelName, String token, String userId, String live)
    {
        this.channelName = channelName;
        this.token = token;
        this.userId = userId;
        this.live = live;
    }

    public String getLive() {
        return live;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

}
