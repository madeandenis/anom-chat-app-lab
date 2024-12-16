package com.example.an0m.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.an0m.models.Device;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfoProvider {

    public static Device.DeviceBuilder getDeviceInfo(Context context,Device.DeviceBuilder deviceBuilder) {
        try {
            if(deviceBuilder == null) deviceBuilder = new Device.DeviceBuilder();

            String appVersion = getAppVersion(context);
            String deviceModel = Build.MODEL;
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

            Map<String, String> deviceOs = new HashMap<>();
            deviceOs.put("OS", "Android");
            deviceOs.put("OS Version", Build.VERSION.RELEASE);
            deviceOs.put("API Level", String.valueOf(Build.VERSION.SDK_INT));

            deviceBuilder.setAppVersion(appVersion)
                    .setDeviceModel(deviceModel)
                    .setDeviceName(deviceName)
                    .setDeviceOs(deviceOs);

        } catch (Exception e) {
            Log.e("DeviceInfoProvider", "Error extracting device data", e);
        }

        return deviceBuilder;
    }

    private static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e("DeviceInfoProvider", "Error retrieving app version", e);
            return "Unknown";
        }
    }

}
