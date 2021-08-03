package com.krash.devguruuastros.media;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.krash.devguruuastros.Activities.AstrologerDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;

import java.io.IOException;

import io.grpc.internal.JsonParser;

public class IvrCalling {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private final String from;
    private final String to;
    Context context;
    Connection.Response response;

    public IvrCalling(Context context, String from, String to)
    {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
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
            Document document = response.parse();
            System.out.println(response.parse());
            String a = document.getElementsByTag("body").text();
            JSONObject json = new JSONObject(a);
            System.out.println(json.get("data"));
            databaseReference.child("ivrNumber").setValue(json.get("data")).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    System.out.println("Successful");
                    System.out.println(firebaseAuth.getUid());
                    databaseReference.child("ivrAvailable").setValue("true");
                }
            });

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + json.get("data")));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(intent);

            System.out.println(a);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
