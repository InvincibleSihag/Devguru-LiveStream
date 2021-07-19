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
import java.util.Objects;

public class LiveStreaming extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private RtcEngine mRtcEngine;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    DatabaseReference astrologerReference;
    String astrologerUid;
    DatabaseReference liveStreamReference;
    MessageAdapter adapter;
    ArrayList<LiveStreamMessage> messages;
    RecyclerView recyclerView;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the onJoinChannelSuccess callback.
        // This callback occurs when the local user successfully joins the channel.
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
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
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
    }

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

   

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        astrologerUid = firebaseAuth.getUid();
        liveStreamReference = FirebaseDatabase.getInstance().getReference("LiveStreams");
        astrologerReference = FirebaseDatabase.getInstance().getReference("LiveStreams").child(firebaseAuth.getUid());
        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
        recyclerView = findViewById(R.id.recyclerView);
        messages = new ArrayList<>();
        adapter = new MessageAdapter(getApplicationContext(), messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
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
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_FIT, 1234567890);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
        System.out.println("Local Video Setup Done");
    }
    private void joinChannel() {
        String token = BuildToken();
        System.out.println(token);
        // Join a channel with a token.
        mRtcEngine.joinChannel(token, astrologerUid, "",1234567890);
        astrologerReference.child("token").setValue(token);
        astrologerReference.child("astrologerUserIDStream").setValue(1234567890);
        astrologerReference.child("Streaming").setValue(true);
        System.out.println("Channel Joined");
    }


    // Listen for the onUserJoined callback.
    // This callback occurs when the remote user successfully joins the channel.
    // You can call the setupRemoteVideo method in this callback to set up the remote video view.


    private void setupRemoteVideo(int uid) {

        // Create a SurfaceView object.
        RelativeLayout mRemoteContainer = findViewById(R.id.remote_video_view_container);
        SurfaceView mRemoteView;
        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteView.setZOrderMediaOverlay(true);
        mRemoteContainer.addView(mRemoteView);
        // Set the remote video view.
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_FIT, uid));

    }


    private void leaveChannel() {
        // Leave the current channel.
        mRtcEngine.leaveChannel();
        astrologerReference.child("Streaming").setValue(false);
    }
    private String BuildToken()
    {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + 3600);
        String result = token.buildTokenWithUid("e3cc7928788f4d2591885321baecb35c", "0b870feaa4b64e6b8f8c367a5cdc662a",
                astrologerUid, 1234567890, Role.Role_Publisher, timestamp); // astrologerUid -> channel name
        System.out.println("Generated Token = "+String.valueOf(result));
        return result;
    }
    public void onCallClicked(View view)
    {
        leaveChannel();
        Intent intent = new Intent(getApplicationContext(), AstrologerMainActivity.class);
        liveStreamReference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Messages").removeValue();
        startActivity(intent);
    }
    public void getMessages(){
        liveStreamReference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Messages").addValueEventListener(new ValueEventListener() {
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
                if (messages.size() > 3)
                {
                    recyclerView.smoothScrollToPosition(messages.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
