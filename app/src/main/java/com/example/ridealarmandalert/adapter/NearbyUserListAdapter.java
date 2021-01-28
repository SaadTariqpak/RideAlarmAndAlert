package com.example.ridealarmandalert.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.models.ChildModel;
import com.example.ridealarmandalert.utils.FragUtil;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.example.ridealarmandalert.utils.SphericalUtil;
import com.example.ridealarmandalert.view.AlarmListFragment;
import com.example.ridealarmandalert.view.ViewProfileData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;

public class NearbyUserListAdapter extends RecyclerView.Adapter<NearbyUserListAdapter.MyViewHolder> {

    private ArrayList<ChildModel> childModelArrayList;
    private Context mContext;
    DatabaseReference databaseReference;
    String mEmail, userId;
    TextView txtNoData;
    Location location;

    int ItemViewId;

    public NearbyUserListAdapter(Context context, TextView txtNoData, Location location) {

        this.mContext = context;
        this.txtNoData = txtNoData;
        this.location = location;

        childModelArrayList = new ArrayList<>();

        SharedPreferences sharedPreferencesManager = new SharedPreferencesManager(context).getPreferencesManager();
        mEmail = sharedPreferencesManager.getString(SharedPreferencesManager.USERNAME, "");
        userId = sharedPreferencesManager.getString(SharedPreferencesManager.USERUNIQUEID, "");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        getUserFromFb();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.child_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final ChildModel childModel = childModelArrayList.get(position);
        holder.txtName.setText(childModel.getName());

        if (childModel.getImgPath() != null && childModel.getImgPath().length() > 0)
            Glide.with(mContext).load(childModel.getImgPath()).into(holder.imgChild);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ViewProfileData();
                Bundle bundle = new Bundle();
                bundle.putString("id", childModel.getId());
                fragment.setArguments(bundle);
                new FragUtil(mContext).
                        changeFragmentWithBackstack(fragment,
                                R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
                                R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
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
        int size = childModelArrayList.size();
        if (size == 0) {
            txtNoData.setVisibility(View.VISIBLE);
        } else {
            txtNoData.setVisibility(View.INVISIBLE);
        }
        return size;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView txtName;
        ImageView imgChild;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemViewId = itemView.getId();

            imgChild = itemView.findViewById(R.id.img_item);

            txtName = itemView.findViewById(R.id.txt_simple_det);

            setVisuals();
        }

        private void setVisuals() {
            Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
            Point size = new Point();

            display.getSize(size);

            int width = size.x;
            int height = size.y;

            //   int paramTextView = (int) (width * 0.15);
            imgChild.getLayoutParams().height = (int) (width * 0.2);
            imgChild.getLayoutParams().width = (int) (width * 0.2);


        }

    }

    private int dpToPx(int dp) {
        Resources r = ((Activity) mContext).getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void getUserFromFb() {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                childModelArrayList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChildModel childModel = new ChildModel();
                    if (!data.getKey().equals(userId) && data.hasChildren()) {
                        String[] arr = data.child("location").getValue().toString().split("/");
                        if (checkUserExistInBound(new LatLng(Double.parseDouble(arr[0]), Double.parseDouble(arr[1])))) {

                            if (data.hasChild("name"))
                                childModel.setName(String.valueOf(data.child("name").getValue()));
                            else {
                                childModel.setName("User xx");
                            }

                            if (data.hasChild("image_url")) {
                                childModel.setImgPath(String.valueOf(data.child("image_url").getValue()));
                            }


                            childModel.setId(data.getKey());
                            childModelArrayList.add(childModel);
                        }


                    }


                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });
    }


    private boolean checkUserExistInBound(LatLng mLatLng) {
        int mRADIUS = 100;//distance in meters

        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

        LatLng northSide = SphericalUtil.computeOffset(center, mRADIUS, 0);
        LatLng southSide = SphericalUtil.computeOffset(center, mRADIUS, 180);
        LatLng eastSide = SphericalUtil.computeOffset(center, mRADIUS, 90);
        LatLng westSide = SphericalUtil.computeOffset(center, mRADIUS, 270);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(northSide)
                .include(southSide)
                .include(eastSide)
                .include(westSide)
                .build();

        return bounds.contains(mLatLng);

    }

}
