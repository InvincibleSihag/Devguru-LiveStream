package com.krash.devguruuastros.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.krash.devguruuastros.Adapters.MessageAdapter;
import com.krash.devguruuastros.Models.LiveStreamMessage;
import com.krash.devguruuastros.R;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import com.krash.devguruuastros.media.RtcTokenBuilder;
import com.krash.devguruuastros.media.RtcTokenBuilder.Role;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

public class UserWatchStream extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private RtcEngine mRtcEngine;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    DatabaseReference liveStreamReference;
    String channelName;
    MessageAdapter adapter;
    ArrayList<LiveStreamMessage> messages;
    TextInputEditText inputEditText;
    MaterialButton sendButton;
    Hashtable<String, String> dictionary;
    String agoraUid="";
    String userName = "";
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the onJoinChannelSuccess callback.
        // This callback occurs when the local user successfully joins the channel.
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            agoraUid = String.valueOf(uid);
            sendButton.setVisibility(View.VISIBLE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }
        @Override
        // Listen for the onUserJoined callback.
        // This callback occurs when the remote user successfully joins the channel.
        // You can call the setupRemoteVideo method in this callback to set up the remote video view.
        public void onUserJoined(final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","Remote user joined, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // Listen for the onUserOffline callback.
        // This callback occurs when the remote user leaves the channel or drops offline.
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","User offline, uid: " + (uid & 0xFFFFFFFFL));

                }
            });
        }
    };

    // Initialize the RtcEngine object.
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e("TAG", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        // Set the client role as BORADCASTER or AUDIENCE according to the scenario.
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
    }

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_watch_stream);
        messages = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        Intent intent = getIntent();
        channelName = intent.getStringExtra("channelName");
        System.out.println("channel Name = "+ channelName);
        firebaseAuth = FirebaseAuth.getInstance();
        liveStreamReference = FirebaseDatabase.getInstance().getReference("LiveStreams");
        getUserName();
        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
        adapter = new MessageAdapter(getApplicationContext(), messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        dictionary = new Hashtable<>();
        inputEditText = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setVisibility(View.GONE);
        sendButton.setOnClickListener(v -> {

            if(!String.valueOf(inputEditText.getText()).equals("")){
                dictionary.clear();
                dictionary.put("firebaseId", firebaseAuth.getUid());
                dictionary.put("agoraId", agoraUid);
                dictionary.put("message", String.valueOf(inputEditText.getText()));
                dictionary.put("name", userName);
                liveStreamReference.child(channelName).child("Messages").push().setValue(dictionary);
                inputEditText.setText("");
            }
        });
        getMessages();
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupLocalVideo();
        joinChannel();
    }


    // Get the Permissions
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }
    private void setupLocalVideo() {

        // Enable the video module.
        mRtcEngine.enableVideo();

        // Create a SurfaceView object.
        FrameLayout mLocalContainer = findViewById(R.id.local_video_view_container);
        SurfaceView mLocalView;

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        // Set the local video view.
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_FIT, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
        System.out.println("Local Video Setup Done");
    }

    // Listen for the onUserJoined callback.
    // This callback occurs when the remote user successfully joins the channel.
    // You can call the setupRemoteVideo method in this callback to set up the remote video view.

    private void setupRemoteVideo(int uid) {
        System.out.println("Remote Video Started");
        // Create a SurfaceView object.
        FrameLayout mRemoteContainer = findViewById(R.id.local_video_view_container);
        SurfaceView mRemoteView;
        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteView.setZOrderMediaOverlay(true);
        mRemoteContainer.addView(mRemoteView);
        // Set the remote video view.
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_FIT, uid));
    }

    private void joinChannel() {
        String token = BuildToken();
        // Join a channel with a token.
        mRtcEngine.joinChannel(token, channelName, "",0);
        System.out.println("Channel Joined");
    }

    private void leaveChannel() {
        // Leave the current channel.
        mRtcEngine.leaveChannel();
    }
    private String BuildToken()
    {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + 3600);
        String result = token.buildTokenWithUid("e3cc7928788f4d2591885321baecb35c", "0b870feaa4b64e6b8f8c367a5cdc662a",
                channelName, 0, Role.Role_Attendee, timestamp); // astrologerUid -> channel name
        System.out.println("Generated Token = "+String.valueOf(result));
        return result;
    }
    public void onCallClicked(View view)
    {
        leaveChannel();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("comesfrom", "userWatchStream");
        startActivity(intent);
    }

    public void getUserName()
    {
        FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(firebaseAuth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userName = String.valueOf(snapshot.child("name").getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    public void getMessages(){
        liveStreamReference.child(channelName).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    messages.add(new LiveStreamMessage(
                       String.valueOf(snapshot1.child("message").getValue()),
                            String.valueOf(snapshot1.child("name").getValue()),
                            String.valueOf(snapshot1.child("agoraId").getValue()),
                            String.valueOf(snapshot1.child("firebaseId").getValue())
                    ));
                }
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messages.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
