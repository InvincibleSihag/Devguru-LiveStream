package com.krash.devguruuastros.media;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;

import java.io.IOException;

public class IvrCalling {
    private final String from;
    private final String to;
    Connection.Response response;

    public IvrCalling(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public String getNumber() {

        new Thread(){
            @Override
            public void run() {
                super.run();
                request();
            }
        }.start();
        return from;
    }

    public void request()
    {
        try {
            String apiKey = "Bearer 71953|6RNFuIEgLuFYSaoUbfUc03rwuBEqJgAMKq5PIwtX";
            response = Jsoup.connect("https://panelv2.cloudshope.com/api/outbond_call" + "?from_number=" + from + "&mobile_number=" + to)
                    .timeout(10 * 1000)
                    .method(Method.GET)
                    .header("Authorization", apiKey)
                    .data("max_seconds", "300")
                    .execute();
            System.out.println(response.parse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
