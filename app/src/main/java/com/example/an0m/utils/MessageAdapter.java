package com.example.an0m.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.an0m.R;
import com.example.an0m.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String currentUser;

    // Constructor
    public MessageAdapter(Context context, List<Message> messageList, String currentUser) {
        this.context = context;
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

    // Create new views
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == MessageViewType.SENT) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(itemView);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageSender.setText(message.getSenderName());
        holder.messageContent.setText(message.getContent());
        holder.messageTimestamp.setText(message.getFormattedTimestamp());
    }

    public static class MessageViewType {
        public static final int SENT = 1;
        public static final int RECEIVED = 2;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderName().equals(currentUser)) {
            return MessageViewType.SENT;
        } else {
            return MessageViewType.RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder hold references to the views in each item
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageSender;
        TextView messageContent;
        TextView messageTimestamp;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageSender = itemView.findViewById(R.id.message_sender);
            messageContent = itemView.findViewById(R.id.message_content);
            messageTimestamp = itemView.findViewById(R.id.message_timestamp);
        }
    }
}
