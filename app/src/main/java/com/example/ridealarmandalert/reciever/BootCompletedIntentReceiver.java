package com.example.ridealarmandalert.reciever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ridealarmandalert.db.DBHelper;
import com.example.ridealarmandalert.models.AlarmModel;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ////// reset your alarms here
            setAlarms();
        }

    }


    private void setAlarms() {

        DBHelper dbHelper = new DBHelper(context);
        List<AlarmModel> list = dbHelper.getAllAlarm();
        for (AlarmModel alarmModel : list) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("title", alarmModel.getTitle());
            intent.putExtra("id", alarmModel.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.valueOf(String.valueOf(alarmModel.getId())), intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            assert alarmManager != null;
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmModel.getTime(), pendingIntent);
        }
    }


}