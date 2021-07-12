package com.krash.devguruuastros.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.krash.devguruuastros.Models.Broadcasts;
import com.krash.devguruuastros.R;
import com.krash.devguruuastros.databinding.LiveStreamItemBinding;

import java.util.ArrayList;

public class LiveStreamsAdapter extends RecyclerView.Adapter<LiveStreamsAdapter.ViewHolder>{
    ArrayList<Broadcasts> broadcasts;
    Context context;
    DatabaseReference reference;

    public LiveStreamsAdapter(ArrayList<Broadcasts> broadcasts, Context context)
    {
        this.broadcasts = broadcasts;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.live_stream_item, parent, false);
        reference = FirebaseDatabase.getInstance().getReference("Astrologers");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LiveStreamsAdapter.ViewHolder holder, int position) {

        reference.child(broadcasts.get(position).getChannelName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                holder.binding.astrologerName.setText(String.valueOf(snapshot.child("name").getValue()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });


    }


    @Override
    public int getItemCount() {
        return broadcasts.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        LiveStreamItemBinding binding;
        public ViewHolder(View itemView) {
            super(itemView);
            binding = LiveStreamItemBinding.bind(itemView);
        }
    }
}
