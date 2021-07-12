package com.krash.devguruuastros.Activities;

import android.os.Bundle;
import android.renderscript.ScriptGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.krash.devguruuastros.Adapters.LiveStreamsAdapter;
import com.krash.devguruuastros.Models.Broadcasts;
import com.krash.devguruuastros.databinding.ActivityUserLiveStreamingWatchBinding;

import java.util.ArrayList;

public class UserLiveStreamingWatch extends AppCompatActivity {
    ActivityUserLiveStreamingWatchBinding binding;
    DatabaseReference reference;
    ArrayList<Broadcasts> broadcasts;
    LiveStreamsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserLiveStreamingWatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference("LiveStreams");
        broadcasts = new ArrayList<>();
        adapter = new LiveStreamsAdapter(broadcasts, getApplicationContext());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerView.setAdapter(adapter);
        getLiveStreams();

    }
    private void getLiveStreams()
    {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                broadcasts.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    if (String.valueOf(snapshot1.child("Streaming").getValue()).equals("true"))
                    {
                        broadcasts.add(new Broadcasts(String.valueOf(snapshot1.getKey()),
                                String.valueOf(snapshot1.child("token").getValue()),
                                String.valueOf(snapshot1.child("astrologerUserIDStream").getValue()),
                                String.valueOf(snapshot1.child("Streaming").getValue())
                        ));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
