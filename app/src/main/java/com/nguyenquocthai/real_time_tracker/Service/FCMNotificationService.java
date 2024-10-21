package com.nguyenquocthai.real_time_tracker.Service;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Tạo một Intent để gửi thông tin đến MainActivity
        // When u using app
        Intent intent = new Intent("com.yourapp.FCM_MESSAGE");
        intent.putExtra("userID", remoteMessage.getData().get("userID"));
        intent.putExtra("name", remoteMessage.getData().get("name"));
        // Gửi Broadcast
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
