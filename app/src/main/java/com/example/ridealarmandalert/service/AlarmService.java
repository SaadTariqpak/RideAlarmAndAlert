package com.example.ridealarmandalert.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ridealarmandalert.MainActivity;
import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.db.DBHelper;

import static com.example.ridealarmandalert.utils.Constants.FgServicechannelId;
import static com.example.ridealarmandalert.utils.Constants.GenralchanelId;
import static com.example.ridealarmandalert.utils.Constants.locationReqCount;


public class AlarmService extends Service {


    private final String TAG = "BackgroundService";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1337, getForegroundServiceNotification());

        }
//        DBHelper dbHelper = new DBHelper(this);
//        try {
//            if (intent != null && intent.hasExtra("id")) {
//                String id = intent.getStringExtra("id");
//
//                if (id != null) {
//                    dbHelper.deleteAlarm(id);
//                }
//            }
//        } catch (Exception e) {
//
//        }
        // return START_STICKY;
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {


        super.onTaskRemoved(rootIntent);
    }


    //Foreground Service notification
    private Notification getForegroundServiceNotification() {


        return new NotificationCompat.Builder(this, FgServicechannelId)
                .setContentTitle("")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }



}