package com.example.ridealarmandalert.reciever;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.example.ridealarmandalert.MainActivity;
import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.service.AlarmService;

import java.util.Random;

import static com.example.ridealarmandalert.utils.Constants.GenralchanelId;

public class AlarmReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, final Intent intent) {

        this.context = context;
        //   NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //     Notification notification = intent.getParcelableExtra("notification");
        int notificationId = new Random().nextInt(60000);
        int requestCode = new Random().nextInt(3000);
        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(notificationId, getNotification(title, "" + title, requestCode));

//        Intent fgServiceIntent = new Intent(context, AlarmService.class);
//        fgServiceIntent.putExtra("id", id);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(fgServiceIntent);
//        } else {
//            context.startService(fgServiceIntent);
//        }


    }

    private Notification getNotification(String title, String content, int reqCode) {
        Intent intent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(reqCode, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GenralchanelId);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        // builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(content));
        //   builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }


}
