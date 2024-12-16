package com.example.an0m.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.an0m.R;

public class ErrorToast {

    public static void show(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context); //  XML -> corresponding View objects
        View toastLayout = inflater.inflate(R.layout.alert_toast, null);  // null -> do not attach the layout yet to any parent

        TextView toastMessage = toastLayout.findViewById(R.id.toast_message);
        toastMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        toast.show();
    }

}
