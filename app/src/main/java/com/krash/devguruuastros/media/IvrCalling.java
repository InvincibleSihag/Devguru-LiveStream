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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    DatabaseReference databaseReference, callReference;
    String uniqueId;
    private final String from;
    private final String to;
    private final int max_seconds = 500;
    Context context;
    Connection.Response response;
    String astroId;
    String callPrice;
    String userBalance;
    String astroBalance;
    public IvrCalling(Context context, String from, String to, String astroId)
    {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
        callReference = FirebaseDatabase.getInstance().getReference().child("Calls");
        this.from = from;
        this.to = to;
        this.astroId = astroId;
    }

    public String getNumber() {

        new Thread(){
            @Override
            public void run() {
                super.run();

                firebaseDatabase.getReference().child("Astrologers").child(astroId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callPrice = String.valueOf(snapshot.child("callPrice").getValue());
                        astroBalance = String.valueOf(snapshot.child("balance").getValue());
                        firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                userBalance = String.valueOf(snapshot.child("balance").getValue());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                while (callPrice == null || astroBalance == null || userBalance == null)
                {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                request();
            }
        }.start();
        return from;
    }

    public void request()
    {
        try {
            uniqueId = callReference.push().getKey();
            String apiKey = "Bearer 83951|URwRAZdLgAkaO6VabroyyoHo6zXokyS4IoGyzd5p";
            response = Jsoup.connect("https://panelv2.cloudshope.com/api/outbond_call" + "?from_number=" + from
                    + "&mobile_number=" + to + "&max_seconds=" + String.valueOf(max_seconds) + "&unique_id=" + uniqueId
                    + "&dlurl=http://devguruuastro.com/api.php?unique_id=" + firebaseAuth.getUid()
                    + "(-)" + userBalance
                    + "(-)" + astroId
                    + "(-)" + callPrice
                    + "(-)" + astroBalance)
                    .timeout(10 * 10000)
                    .method(Method.GET)
                    .header("Authorization", apiKey)
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
//            callReference.child(uniqueId).child("from").setValue(from);
//            callReference.child(uniqueId).child("to").setValue(to);
//            callReference.child(uniqueId).child("max_seconds").setValue(max_seconds);
//            callReference.child(uniqueId).child("response").setValue(response.parse().text());

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + json.get("data")));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(intent);

            System.out.println(a);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
//            callReference.child(uniqueId).child("from").setValue(from);
//            callReference.child(uniqueId).child("to").setValue(to);
//            callReference.child(uniqueId).child("max_seconds").setValue(max_seconds);
//            callReference.child(uniqueId).child("response").setValue("Timeout in requesting the API");
        }

    }
}
