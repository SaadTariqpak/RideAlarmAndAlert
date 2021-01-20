package com.example.ridealarmandalert.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.utils.ImagePicker;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ViewProfileData extends Fragment {
    View v;

    private ImageView imgProf;
    private EditText editTextName, edtEmergencyPhone, edtDisease;
    private Button btnUpdate, btnSelectImage;
    private static final int PICK_IMAGE = 1;
    private static final int CLICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    Bitmap btmp;
    DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.update_profile_data, container, false);

        init();
        return v;
    }


    private void init() {
        userID = getArguments().getString("id");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        imgProf = v.findViewById(R.id.img_prof);

        editTextName = v.findViewById(R.id.edt_name);
        edtEmergencyPhone = v.findViewById(R.id.edt_emergency_phone_no);
        edtDisease = v.findViewById(R.id.edt_disease);

        btnSelectImage = v.findViewById(R.id.btn_image_change);
        btnUpdate = v.findViewById(R.id.btn_update_info);

        btnUpdate.setVisibility(View.INVISIBLE);
        btnSelectImage.setVisibility(View.INVISIBLE);
        TextView tvHead = (TextView) v.findViewById(R.id.txt_head);
        tvHead.setText("User Information");
        editTextName.setEnabled(false);
        edtEmergencyPhone.setEnabled(false);
        edtDisease.setEnabled(false);


        getUserFromFb();

    }


    private void getUserFromFb() {
        databaseReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // for (DataSnapshot data : dataSnapshot.getChildren()) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("image_url")) {

                        Glide.with(Objects
                                .requireNonNull(getActivity()))
                                .load(dataSnapshot.child("image_url").getValue().toString())

                                .into(imgProf);

                    }

                    if (dataSnapshot.hasChild("name")) {

                        editTextName.setText(String.valueOf(dataSnapshot.child("name").getValue()));
                    } else {
                        editTextName.setText("User xx");

                    }
                    if (dataSnapshot.hasChild("emergency_phone")) {

                        edtEmergencyPhone.setText(String.valueOf(dataSnapshot.child("emergency_phone").getValue()));
                    } else {
                        edtEmergencyPhone.setText("03xx-xxxxxxxx");

                    }
                    if (dataSnapshot.hasChild("disease")) {

                        edtDisease.setText(String.valueOf(dataSnapshot.child("disease").getValue()));
                    } else {
                        edtDisease.setText("");

                    }


                }
                // }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });
    }

}
