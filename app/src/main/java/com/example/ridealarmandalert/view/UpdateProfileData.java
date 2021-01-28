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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class UpdateProfileData extends Fragment {
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
    private SharedPreferencesManager sharedPreferencesManager;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.update_profile_data, container, false);
        try {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("User Profile");
        } catch (Exception ignore) {

        }

        init();
        return v;
    }

    private boolean validateForm() {
        boolean isValid = true;

        String name = editTextName.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            try {
                double d = Double.parseDouble(name);
                editTextName.setError("Invalid input.");
                isValid = false;
            } catch (NumberFormatException nfe) {
                editTextName.setError(null);
            }

        } else {
            isValid = false;
            editTextName.setError("Required.");
        }

        String phone = edtEmergencyPhone.getText().toString();
        if (!TextUtils.isEmpty(phone) && phone.length() >= 11) {
            edtEmergencyPhone.setError(null);

        } else {
            isValid = false;
            edtEmergencyPhone.setError("Required.");
        }
        String disease = edtDisease.getText().toString();
        if (!TextUtils.isEmpty(disease)) {
            try {
                double d = Double.parseDouble(disease);
                edtDisease.setError("Invalid input.");
                isValid = false;
            } catch (NumberFormatException nfe) {
                edtDisease.setError(null);
            }
        } else {
            isValid = false;
            edtDisease.setError("Required.");
        }

        return isValid;
    }

    private void init() {
        progressDialog = new ProgressDialog(getContext());

        sharedPreferencesManager = new SharedPreferencesManager(getActivity());
        userID = sharedPreferencesManager.getPreferencesManager().getString(SharedPreferencesManager.USERUNIQUEID, "");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        imgProf = v.findViewById(R.id.img_prof);

        editTextName = v.findViewById(R.id.edt_name);
        edtEmergencyPhone = v.findViewById(R.id.edt_emergency_phone_no);
        edtDisease = v.findViewById(R.id.edt_disease);

        btnSelectImage = v.findViewById(R.id.btn_image_change);
        btnUpdate = v.findViewById(R.id.btn_update_info);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {

                    if (btmp == null) {
                        uploadData(null, null);
                    } else {
                        uploadImage();
                    }
                }
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (checkPermission()) {
                    showPictureDialog();

                } else {
                    requestPermission();
                }


            }
        });

        getUserFromFb();

    }

    private boolean checkPermission() {

        int result = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }


    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {

            if (data != null && data.getData() != null) {

                btmp = ImagePicker.getImageFromResult(getContext(), resultCode, data);//your compressed bitmap here
                imgProf.setImageBitmap(btmp);
                // uploadImage(btmp);
            } else {

                Toast.makeText(getContext(), "Image must be selected!", Toast.LENGTH_SHORT).show();
            }


        } else if (requestCode == CLICK_IMAGE) {

            if (data != null && (Bitmap) data.getExtras().get("data") != null) {
                // filePath = data.getData();

                btmp = (Bitmap) data.getExtras().get("data");
                imgProf.setImageBitmap(btmp);

            } else {

                Toast.makeText(getContext(), "Image must be captured!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPictureDialog();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(getContext(), "Permission Denied, You cannot use local drive .", Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }

    private void uploadData(Uri uri, String imageName) {

        if (!progressDialog.isShowing()) {
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
        }

        final DatabaseReference childDataBaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);


        childDataBaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HashMap<String, String> hashMap = new HashMap<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        hashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                    }


                    if (uri != null)
                        hashMap.put("image_url", String.valueOf(uri));

                    if (imageName != null)
                        hashMap.put("image_name", imageName);
                    hashMap.put("name", editTextName.getText().toString());
                    hashMap.put("emergency_phone", edtEmergencyPhone.getText().toString());
                    hashMap.put("disease", edtDisease.getText().toString());


                    childDataBaseReference.setValue(hashMap);
                }

                progressDialog.dismiss();
                Toast.makeText(getContext(), "Data Uploaded", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }

    //function to upload image to firebase
    private void uploadImage() {

        if (btmp != null) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            btmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());


            ref.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref.getDownloadUrl()

                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {

//                                    if (btmp != null && !btmp.isRecycled()) {
//                                        btmp.recycle();
//                                        btmp = null;
//                                    }

                                    uploadData(uri, ref.getName());


                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Failed to post link", Toast.LENGTH_SHORT).show();
                                }
                            });


                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NotNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });

        }
    }

    //Function to shoe dialogue to select image
    private void showPictureDialog() {

        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        choosePhotoFromGallary();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;
                }
            }
        });

        AlertDialog alert = pictureDialog.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();


        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });


    }

    //Fucntion to choose photo from gallery
    private void choosePhotoFromGallary() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);

    }

    //Fucntion to take photo from gallery
    private void takePhotoFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CLICK_IMAGE);
    }
}
