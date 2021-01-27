package com.example.ridealarmandalert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDex;

import com.example.ridealarmandalert.utils.Constants;
import com.example.ridealarmandalert.utils.FragUtil;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.example.ridealarmandalert.view.LoginFragment;
import com.example.ridealarmandalert.view.MyMapFragment;
import com.example.ridealarmandalert.view.UpdateProfileData;
import com.example.ridealarmandalert.view.ViewProfileData;

import static com.example.ridealarmandalert.utils.Constants.GenralchanelId;

public class MainActivity extends AppCompatActivity {

    SharedPreferencesManager sharedPreferencesManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferencesManager = new SharedPreferencesManager(this);


        String mUsername = sharedPreferencesManager.getPreferencesManager().getString(SharedPreferencesManager.USERNAME, "");
        if (mUsername.equals("")) {

            new FragUtil(MainActivity.this).changeFragmentWithoutBackstack(new LoginFragment(), R.id.main_container, -1, -1);
        } else {
            new FragUtil(MainActivity.this).changeFragmentWithoutBackstack(new MyMapFragment(), R.id.main_container, -1, -1);

        }


        createNotificationChannels();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;

    }


    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.item_logout) {
            sharedPreferencesManager.getPreferencesManager().edit().remove(SharedPreferencesManager.USERNAME).remove(SharedPreferencesManager.USERUNIQUEID).apply();

            new FragUtil(MainActivity.this).changeFragmentWithoutBackstack(new LoginFragment(), R.id.main_container, -1, -1);

        } else if (item.getItemId() == R.id.item_edit_profile) {
            Constants.locationReqCount = 0;
            new FragUtil(MainActivity.this).
                    changeFragmentWithBackstack(new UpdateProfileData(),
                            R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
                            R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void createNotificationChannels() {

        NotificationChannel channelGeneral;
        NotificationManager notificationManager = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelGeneral = new NotificationChannel(GenralchanelId, GenralchanelId, NotificationManager.IMPORTANCE_HIGH);
            channelGeneral.enableLights(true);
            channelGeneral.enableVibration(true);
            channelGeneral.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channelGeneral);

        }


    }

}