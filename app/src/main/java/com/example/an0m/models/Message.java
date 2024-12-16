package com.example.an0m.models;

import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Message {
    private String senderName;
    private String content;
    private long timestamp;

    public Message(String senderName, String content, long timestamp) {
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return  sdf.format(date);
    }

    public static Message fromHashMap(Map<String, Object> map) {
        String senderName = (String) map.get("senderName");
        String content = (String) map.get("content");
        Object timestampObj = map.get("timestamp");

        long timestamp;
        if (timestampObj instanceof Number) {
            timestamp = ((Number) timestampObj).longValue();
        } else {
            timestamp = System.currentTimeMillis();
        }

        return new Message(senderName, content, timestamp);
    }


    public Map<String, Object> toHashMap() {
        Map<String, Object> map = new HashMap<>();

        try {
            Field[] fields = Message.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // Allows access to private fields
                String fieldName = field.getName();
                Object fieldValue = field.get(this); // Get the value of the field
                map.put(fieldName, fieldValue); // Put the field name and value into the map
            }
        } catch (IllegalAccessException e) {
            Log.d("Message Model - HashMap Conversion", Arrays.toString(e.getStackTrace()));
        }

        return map;
    }
}