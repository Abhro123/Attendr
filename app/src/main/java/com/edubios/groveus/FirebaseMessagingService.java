package com.edubios.groveus;

import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import com.edubios.groveus.activity.MyNotificationManager;
import com.edubios.groveus.activity.NotificationLayout;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    private MyNotificationManager myNotificationManager;
    //private static final String TAG = "fcmmessageexample";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
       //Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        notifyUser(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());

    }
    public void notifyUser(String from, String notification)
    {
        myNotificationManager = new MyNotificationManager(getApplicationContext());
        myNotificationManager.showNotification(from, notification, new Intent(getApplication(), NotificationLayout.class));
    }

}
