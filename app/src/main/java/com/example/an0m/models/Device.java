package com.example.an0m.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Device {

    private String appVersion;
    private String deviceModel;
    private String deviceName;
    private Map<String, String> deviceOs;
    private boolean isActive;
    private boolean isCompromised;
    @ServerTimestamp
    private Object lastLogin;
    private String publicKey;

    // Private constructor to ensure the object can only be created through the Builder
    private Device(DeviceBuilder builder) {
        this.appVersion = builder.appVersion;
        this.deviceModel = builder.deviceModel;
        this.deviceName = builder.deviceName;
        this.deviceOs = builder.deviceOs;
        this.isActive = builder.isActive;
        this.isCompromised = builder.isCompromised;
        this.publicKey = builder.publicKey;
        this.lastLogin = builder.lastLogin;
    }

    // Getters
    public String getAppVersion() {
        return appVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Map<String, String> getDeviceOs() {
        return deviceOs;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isCompromised() {
        return isCompromised;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public String getPublicKey() {
        return publicKey;
    }


    // Builder class
    public static class DeviceBuilder {
        private String appVersion;
        private String deviceModel;
        private String deviceName;
        private Map<String, String> deviceOs;
        private boolean isActive;
        private boolean isCompromised;
        private Object lastLogin;
        private String publicKey;

        public DeviceBuilder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public DeviceBuilder setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
            return this;
        }

        public DeviceBuilder setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public DeviceBuilder setDeviceOs(Map<String, String> deviceOs) {
            this.deviceOs = deviceOs;
            return this;
        }

        public DeviceBuilder setActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public DeviceBuilder setCompromised(boolean isCompromised) {
            this.isCompromised = isCompromised;
            return this;
        }

        public DeviceBuilder setLastLogin(Object lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public DeviceBuilder setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Device build() {
            return new Device(this);
        }
    }

    public Map<String, Object> toHashMap() {
        Map<String, Object> map = new HashMap<>();

        try {
            Field[] fields = Device.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // Allows access to private fields
                String fieldName = field.getName();
                Object fieldValue = field.get(this); // Get the value of the field
                map.put(fieldName, fieldValue); // Put the field name and value into the map
            }
        } catch (IllegalAccessException e) {
            Log.d("Device Model - HashMap Conversion", Arrays.toString(e.getStackTrace()));
        }

        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return "Device{" +
                "appVersion='" + appVersion + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceOs=" + deviceOs +
                ", isActive=" + isActive +
                ", isCompromised=" + isCompromised +
                ", lastLogin=" + lastLogin +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
