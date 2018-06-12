package com.borqs.se.home3d;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class LocalService extends Service {

    @Override
    public void onCreate() {
        if (HomeUtils.DEBUG) {
            Log.d(HomeUtils.TAG, "###########################start localservice");
        }
        sendNotification();
    }

    private void sendNotification() {
//        Notification notification = new Notification();
//        notification.setLatestEventInfo(this, "3DHome", "Welcome to 3DHome !", pendingIntent);
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("3DHome")
                .setContentText("Welcome to 3DHome!")
                .setContentIntent(pendingIntent)
                .build();
        try {
            startForeground(12314, notification);
        } catch (Exception e) {

        }
    }

//    private boolean sendNotification_I() {
//        Notification notification = new Notification(0, "3DHome", System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, HomeActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(this, "3DHome", "Welcome to 3DHome !", pendingIntent);
//        try {
//            startForeground(12314, notification);
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
//    }
//
//    private void sendNotification_II() {
//        Notification notification = new Notification(R.drawable.app_icon, null, System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, HomeActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(this, "3DHome", "Welcome to 3DHome !", pendingIntent);
//        try {
//            startForeground(12314, notification);
//        } catch (Exception e) {
//
//        }
//    }

    @Override
    public void onDestroy() {
        if(HomeUtils.DEBUG) {
            Log.d(HomeUtils.TAG, "###########################stop localservice");
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
