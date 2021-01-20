package com.example.ridealarmandalert.adapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.db.DBHelper;
import com.example.ridealarmandalert.models.AlarmModel;
import com.example.ridealarmandalert.models.ChildModel;
import com.example.ridealarmandalert.reciever.AlarmReceiver;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.example.ridealarmandalert.utils.SphericalUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.MyViewHolder> {

    private ArrayList<AlarmModel> alarmModelArrayList;
    private Context mContext;
    DatabaseReference databaseReference;
    String mEmail;
    TextView txtNoData;
    DBHelper dbHelper;
    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm  dd-MMMM-yyyy");

    int ItemViewId;

    public AlarmListAdapter(Context context, TextView txtNoData) {

        this.mContext = context;
        this.txtNoData = txtNoData;

        alarmModelArrayList = new ArrayList<>();

        mEmail = new SharedPreferencesManager(context).getPreferencesManager().getString(SharedPreferencesManager.USERNAME, "");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        dbHelper = new DBHelper(context);
        loadAlarms();
    }

    void dellAlarm(String id, int pos) {

        dbHelper.deleteAlarm(id);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);


        alarmManager.cancel(PendingIntent.getBroadcast(mContext, Integer.parseInt(id),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA));
        Toast.makeText(mContext, "Alarm Cancelled", Toast.LENGTH_SHORT).show();
        alarmModelArrayList.remove(pos);
        notifyDataSetChanged();
    }

    public void loadAlarms() {
        alarmModelArrayList.clear();
        alarmModelArrayList.addAll(dbHelper.getAllAlarm());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.alarm_list_child, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final AlarmModel alarmModel = alarmModelArrayList.get(position);
        holder.txtTitle.setText(alarmModel.getTitle());
        holder.txtTime.setText(formatter.format(alarmModel.getTime()));

        holder.imageViewCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dellAlarm(alarmModel.getId(), position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Fragment fragment = new UpdateChildData();
//                Bundle bundle = new Bundle();
//                bundle.putString("id", childModel.getId());
//                fragment.setArguments(bundle);
//
//                new FragUtil(mContext).
//                        changeFragmentWithBackstack(fragment,
//                                R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
//                                R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);

            }
        });
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                databaseReference.child("child").child(childModel.getId()).removeValue();
//
//                return false;
//            }
//        });

    }


    @Override
    public int getItemCount() {
        int size = alarmModelArrayList.size();
        if (size == 0) {
            txtNoData.setVisibility(View.VISIBLE);
        } else {
            txtNoData.setVisibility(View.INVISIBLE);
        }
        return size;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView txtTime, txtTitle;
        ImageView imageViewCancelAlarm;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemViewId = itemView.getId();

            txtTime = itemView.findViewById(R.id.txt_time);
            txtTitle = itemView.findViewById(R.id.txt_title);
            imageViewCancelAlarm = itemView.findViewById(R.id.img_cancel_alarm);

            setVisuals();
        }

        private void setVisuals() {
            Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
            Point size = new Point();

            display.getSize(size);

            int width = size.x;
            int height = size.y;

            //   int paramTextView = (int) (width * 0.15);
//            imgChild.getLayoutParams().height = (int) (width * 0.2);
//            imgChild.getLayoutParams().width = (int) (width * 0.2);


        }

    }

    private int dpToPx(int dp) {
        Resources r = ((Activity) mContext).getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
