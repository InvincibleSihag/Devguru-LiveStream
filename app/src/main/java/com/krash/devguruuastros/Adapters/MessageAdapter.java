package com.krash.devguruuastros.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.krash.devguruuastros.Models.LiveStreamMessage;
import com.krash.devguruuastros.R;
import com.krash.devguruuastros.databinding.MsgSentItemLayoutBinding;


import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.SentViewHolder> {
    Context context;
    ArrayList<LiveStreamMessage> messages;

//    LayoutInflater layoutInflater;

    public MessageAdapter(Context context, ArrayList<LiveStreamMessage> messages){
        this.context = context;
        this.messages = messages;
    }


    @Override
    public SentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_sent_item_layout, parent, false);
        return new MessageAdapter.SentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.SentViewHolder holder, int position) {
        holder.binding.senderMessage.setText(messages.get(position).getMessage());
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder{

         MsgSentItemLayoutBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MsgSentItemLayoutBinding.bind(itemView);
        }
    }

}
