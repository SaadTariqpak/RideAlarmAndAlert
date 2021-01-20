package com.example.ridealarmandalert.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.utils.CheckInternetConnection;
import com.example.ridealarmandalert.utils.MyProgressDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignupFragment extends Fragment {

    View v;
    EditText edtName, edtEmail, edtPassword;
    MaterialCardView btnRegister;
    MyProgressDialog myProgressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.signup_fragment, container, false);
        setHasOptionsMenu(true);

        edtName = v.findViewById(R.id.edt_name);
        edtEmail = v.findViewById(R.id.edt_email);
        edtPassword = v.findViewById(R.id.edt_pass);
        btnRegister = v.findViewById(R.id.cd_btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return v;
    }


    private void registerUser() {
        if (!validateForm()) {
            return;
        }

        if (!new CheckInternetConnection().isNetworkConnected(getActivity())) {
            Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        myProgressDialog = new MyProgressDialog(getContext());
        myProgressDialog.show();

        storeUserDataOnFb();

    }


    private boolean validateForm() {
        boolean valid = true;


        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Required.");
            valid = false;
        } else {
            edtName.setError(null);
        }

        String email = edtEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Required.");
            valid = false;
        } else {
            edtEmail.setError(null);
        }

        String pass = edtPassword.getText().toString();
        if (TextUtils.isEmpty(pass)) {
            edtPassword.setError("Required.");
            valid = false;
        } else {
            edtPassword.setError(null);
        }


        return valid;
    }

    private void storeUserDataOnFb() {


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        final HashMap<String, String> hashMap = new HashMap<>();

        String email = edtEmail.getText().toString().trim().toLowerCase();

        hashMap.put("name", edtName.getText().toString().trim());
        hashMap.put("email", email);
        hashMap.put("password", edtPassword.getText().toString());

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myProgressDialog.dismiss();
                if (snapshot.exists()) {
                    Toast.makeText(getContext(), "Email already registered!", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.
                            push().
                            setValue(hashMap).
                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    try {


                                        edtName.setText("");
                                        edtEmail.setText("");
                                        edtPassword.setText("");

                                        getActivity().onBackPressed();
                                    } catch (Exception e) {

                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                myProgressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to register", Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }
}