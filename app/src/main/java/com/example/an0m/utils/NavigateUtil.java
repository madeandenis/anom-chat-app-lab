package com.example.an0m.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.an0m.activites.AccountSetupActivity;
import com.example.an0m.activites.GroupChatActivity;

public class NavigateUtil {

    public static void navigateToGroupChat(Context context) {
        if (context instanceof Activity) {
            Intent intent = new Intent(context, GroupChatActivity.class);
            intent.putExtra("GROUP_ID", "Io087dz8bN2TujZjDUD2");
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }

    public static void navigateToAccountSetup(Context context) {
        if (context instanceof Activity) {
            Intent intent = new Intent(context, AccountSetupActivity.class);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
