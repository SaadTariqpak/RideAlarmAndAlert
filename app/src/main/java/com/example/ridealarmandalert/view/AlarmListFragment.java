package com.example.ridealarmandalert.view;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.util.StringUtil;

import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.adapter.AlarmListAdapter;
import com.example.ridealarmandalert.db.DBHelper;
import com.example.ridealarmandalert.reciever.AlarmReceiver;
import com.example.ridealarmandalert.utils.MyProgressDialog;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;


public class AlarmListFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    View v, mViewBg;

    private RecyclerView recyclerViewTableList;
    private AlarmListAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView txtNoData;
    FloatingActionButton floatingActionButton;

    int day, month, year, hour, minute;
    int myday, myMonth, myYear, myHour, myMinute;

    private BottomSheetBehavior sheetBehaviorAddAlarm;
    private LinearLayout bottomSheetAddAlarm;
    EditText edtAlarmTitle;
    Button btnSubmitAlarm, btnSelectDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.alarm_list, container, false);

        try {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Alarms List");
        } catch (Exception ignore) {

        }

        mViewBg = v.findViewById(R.id.bg);

        recyclerViewTableList = v.findViewById(R.id.recyclerview_list);
        floatingActionButton = v.findViewById(R.id.fab_add_alarm);

        mLayoutManager = new LinearLayoutManager(getContext());
        txtNoData = v.findViewById(R.id.txt_no_data);

        recyclerViewTableList.setLayoutManager(mLayoutManager);
        recyclerViewTableList.setItemAnimator(new DefaultItemAnimator());


        adapter = new AlarmListAdapter(getActivity(), txtNoData);

        recyclerViewTableList.setAdapter(adapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehaviorAddAlarm.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });

        setBottomSheet();
        return v;
    }
//    String uniqueID = UUID.randomUUID().toString();

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myYear = year;
        myday = day;
        myMonth = month;
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), AlarmListFragment.this, hour, minute, DateFormat.is24HourFormat(getContext()));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        myHour = hourOfDay;
        myMinute = minute;

//        textView.setText("Year: " + myYear + "\n" +
//                "Month: " + myMonth + "\n" +
//                "Day: " + myday + "\n" +
//                "Hour: " + myHour + "\n" +
//                "Minute: " + myMinute);
    }


    private void setAlarm() {
        long mTime = getMyDate().getTimeInMillis();
        MyProgressDialog mAlarmNotification = new MyProgressDialog(getActivity());
        DBHelper dbHelper = new DBHelper(getActivity());
        long id = dbHelper.insertAlarm(edtAlarmTitle.getText().toString(), 0, mTime, mAlarmNotification);
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("title", edtAlarmTitle.getText().toString());
        intent.putExtra("id", id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), Integer.valueOf(String.valueOf(id)), intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, mTime, pendingIntent);

        adapter.loadAlarms();
    }

    private Calendar getMyDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, myHour);
        calendar.set(Calendar.MINUTE, myMinute);
        calendar.set(Calendar.MONTH, myMonth);
        calendar.set(Calendar.DAY_OF_MONTH, myday);
        calendar.set(Calendar.YEAR, myYear);

        return calendar;
    }

    private boolean validateAlarm() {
        Date aDate = getMyDate().getTime();
        Date cDate = Calendar.getInstance().getTime();

        if (aDate.after(cDate)) {
            return true;
        } else {
            Toast.makeText(getActivity(), "Alarm time is invalid!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validateAlarmTitle() {
        boolean valid = true;
        String title = edtAlarmTitle.getText().toString().trim();
        if (!TextUtils.isEmpty(title)) {

            try {
                double d = Double.parseDouble(title);
                edtAlarmTitle.setError("Invalid input.");
                valid = false;
            } catch (NumberFormatException nfe) {
                edtAlarmTitle.setError(null);

            }


        } else {
            valid = false;
            edtAlarmTitle.setError("Required.");
        }

        return valid;
    }

    private void setBottomSheet() {

        bottomSheetAddAlarm = v.findViewById(R.id.bottom_sheet_set_alarm);
        sheetBehaviorAddAlarm = BottomSheetBehavior.from(bottomSheetAddAlarm);

        edtAlarmTitle = v.findViewById(R.id.edt_alarm_title);
        btnSelectDate = v.findViewById(R.id.btn_select_date);
        btnSubmitAlarm = v.findViewById(R.id.btn_submit_alarm);
        btnSubmitAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtAlarmTitle.setError(null);
                if (validateAlarmTitle() && validateAlarm()) {
                    setAlarm();
                    sheetBehaviorAddAlarm.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), AlarmListFragment.this, year, month, day);
                datePickerDialog.show();
            }
        });

        sheetBehaviorAddAlarm.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        spinnerEnDenFb.setSelection(0);

//                        edtLocationNameRCFb.setEnabled(false);

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        edtAlarmTitle.setText("");
                        mViewBg.setVisibility(View.GONE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                mViewBg.setVisibility(View.VISIBLE);
                mViewBg.setAlpha(v);
            }
        });


    }
}
