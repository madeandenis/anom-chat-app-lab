package com.example.an0m.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.an0m.R;
import com.example.an0m.components.ErrorToast;
import com.example.an0m.models.Message;
import com.example.an0m.utils.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String username;

    private TextView groupNameView;
    private ImageButton sendButton;
    private EditText messageInput;
    private RecyclerView recyclerViewMessages;

    private MessageAdapter messageAdapter;
    private boolean isAdapterReady = false;

    private List<Message> messageList;
    private ListenerRegistration messagesListener;

    private String groupId;
    private String chatId;

    @Override
    protected void onStop() {
        super.onStop();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeViews();

        fetchUsername(
                () -> fetchGroupDetails(this::fetchMessages)
        );
    }

    private void initializeViews() {
        Intent intent = getIntent();
        groupId = intent.getStringExtra("GROUP_ID");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        groupNameView = findViewById(R.id.toolbar_group_name);
        sendButton = findViewById(R.id.button_send);
        messageInput = findViewById(R.id.edit_text_message);
        recyclerViewMessages = findViewById(R.id.recycler_view_messages);

        messageList = new ArrayList<>();
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchUsername(Runnable onSuccess) {

        String userId = getUserId();

        // Check if userId is null
        if (userId == null) {
            ErrorToast.show(this, "User ID is null");
            return;
        }

        DocumentReference sessionRef = db.collection("sessions").document(userId);

        sessionRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                ErrorToast.show(this, "Error fetching session data");
                return;
            }

            DocumentSnapshot sessionDoc = task.getResult();

            if (sessionDoc == null || !sessionDoc.exists()) {
                ErrorToast.show(this, "Invalid user session");
                return;
            }

            String username = sessionDoc.getString("username");

            if (username == null) {
                ErrorToast.show(this, "Username not found in session");
                return;
            }

            this.username = username;

            messageAdapter = new MessageAdapter(this, messageList, username);
            recyclerViewMessages.setAdapter(messageAdapter);
            isAdapterReady = true;

            onSuccess.run();
        });
    }


    private void fetchGroupDetails(Runnable onSuccess) {

        if (groupId == null || groupId.isEmpty()) {
            ErrorToast.show(this, "Group id is missing");
            return;
        }

        DocumentReference groupRef = db
                .collection("groups")
                .document(groupId);

        groupRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                ErrorToast.show(this, "Error fetching group details");
                return;
            }

            DocumentSnapshot groupDoc = task.getResult();

            if (groupDoc == null || !groupDoc.exists()) {
                ErrorToast.show(this, "Group not found");
                return;
            }

            String chatId = groupDoc.getString("chatId");
            String groupName = groupDoc.getString("name");

            if (groupName == null || groupName.isEmpty()) {
                ErrorToast.show(this, "Group name is missing");
                return;
            }
            if (chatId == null || chatId.isEmpty()) {
                ErrorToast.show(this, "Chat ID is missing");
                return;
            }
            // TODO -> update active users

            groupNameView.setText(groupName);
            this.chatId = chatId;

            startListeningForMessages();

            onSuccess.run();

        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void processMessages(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            ErrorToast.show(this, "No chat document found");
            return;
        }

        Object messagesObject = documentSnapshot.get("messages");
        if (messagesObject == null || !(messagesObject instanceof List<?>)) {
            ErrorToast.show(this, "Messages data is invalid");
            return;
        }

        List<?> messagesList = (List<?>) messagesObject;
        if (messagesList.isEmpty()) {
            ErrorToast.show(this, "No messages found");
            return;
        }

        List<HashMap<String, Object>> messagesData;
        try {
            messagesData = (List<HashMap<String, Object>>) messagesList;
        } catch (ClassCastException e) {
            ErrorToast.show(this, "Messages format is incorrect");
            return;
        }

        messageList.clear();
        for (HashMap<String, Object> messageMap : messagesData) {
            try {
                Message message = Message.fromHashMap(messageMap);
                messageList.add(message);
            } catch (Exception exception) {
                Log.e("GroupChat", "Error parsing message: " + exception.getMessage());
            }
        }

        messageAdapter.notifyDataSetChanged();
        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchMessages() {
        if (chatId == null || chatId.isEmpty()) {
            ErrorToast.show(this, "Chat ID is missing");
            return;
        }

        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("fetchMessages", "Error fetching messages: " + task.getException());
                ErrorToast.show(this, "Error fetching messages");
                return;
            }
            processMessages(task.getResult());
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void startListeningForMessages() {
        if (chatId == null || chatId.isEmpty()) {
            ErrorToast.show(this, "GroupChat: Chat ID is null or empty, cannot listen for messages");
            return;
        }

        messagesListener = db.collection("chats")
                .document(chatId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("GroupChat", "Error listening for messages: " + e.getMessage());
                        ErrorToast.show(this, "Error listening for messages");
                        return;
                    }
                    processMessages(documentSnapshot);
                });

    }

    private void sendMessage() {

        String messageContent = messageInput.getText().toString().trim();

        if (messageContent.isEmpty()) {
            ErrorToast.show(this, "Message content is empty");
            return;
        }

        if (chatId == null || chatId.isEmpty()) {
            ErrorToast.show(this, "Chat ID is missing");
            return;
        }

        if (username == null || username.isEmpty()) {
            ErrorToast.show(this, "Username is missing");
            return;
        }

        Message message = new Message(username, messageContent, System.currentTimeMillis());
        messageList.add(message);

        try{
            db.collection("chats").document(chatId).update("messages", FieldValue.arrayUnion(message.toHashMap()))
                .addOnFailureListener(e -> {
                    throw new RuntimeException(e);
                });
        } catch (Exception e){
            Log.e("MessageSendError", "Error sending message to Firestore", e);
            ErrorToast.show(this,  "Message could not be sent. Please try again later.");
            return;
        }

        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewMessages.scrollToPosition(messageList.size() - 1);

        messageInput.setText("");
    }

    private String getUserId(){
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            ErrorToast.show(this, "User is not authenticated");
            return null;
        }
        return user.getUid();
    }

}
