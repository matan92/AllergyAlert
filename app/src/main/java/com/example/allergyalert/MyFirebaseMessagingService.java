package com.example.allergyalert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.allergyalert.MainActivity;
import com.example.allergyalert.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "";
    public static int NOTIFICATION_ID=1;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        generateNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
    }

    private void generateNotification(String body, String title) {
        Intent intent= new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder noBuilder= new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(NOTIFICATION_ID>1073741824)
            NOTIFICATION_ID=0;

        notificationManager.notify(NOTIFICATION_ID++,noBuilder.build());



    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d(TAG, "Refreshed token: " + s);
    }
}
