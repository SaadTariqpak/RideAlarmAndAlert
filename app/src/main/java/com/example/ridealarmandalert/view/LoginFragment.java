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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.utils.CheckInternetConnection;
import com.example.ridealarmandalert.utils.Constants;
import com.example.ridealarmandalert.utils.FragUtil;
import com.example.ridealarmandalert.utils.MyProgressDialog;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class LoginFragment extends Fragment implements View.OnClickListener {

    View v;
    EditText edtUsername, edtPassword;
    MyProgressDialog myProgressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.login_fragment, container, false);

        try {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(R.string.app_name));
        } catch (Exception ignore) {

        }


        setHasOptionsMenu(true);
        v.findViewById(R.id.txt_signup).setOnClickListener(this);
        v.findViewById(R.id.cd_btn_login).setOnClickListener(this);
        edtUsername = v.findViewById(R.id.edt_username);
        edtPassword = v.findViewById(R.id.edt_pass);

        return v;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_signup) {

            new FragUtil(getActivity()).
                    changeFragmentWithBackstack(new SignupFragment(),
                            R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
                            R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
        } else if (id == R.id.cd_btn_login) {
            userLogin();

        }


    }


    private boolean validateForm() {
        boolean valid = true;


        String email = edtUsername.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            edtUsername.setError("Required.");
            valid = false;
        } else if (!email.matches(Constants.emailPattern)) {
            edtUsername.setError("Invalid Email.");
            valid = false;

        } else {
            edtUsername.setError(null);
        }

        String password = edtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Required.");
            valid = false;
        } else {
            edtPassword.setError(null);
        }


        return valid;
    }


    private void userLogin() {
        if (!validateForm()) {
            return;
        }

        if (!new CheckInternetConnection().isNetworkConnected(getContext())) {
            Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        myProgressDialog = new MyProgressDialog(getContext());
        myProgressDialog.show();

        getUserFromFb();

    }


    private void getUserFromFb() {
        final String email = edtUsername.getText().toString().toLowerCase().trim();
        final String pass = edtPassword.getText().toString().toLowerCase().trim();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");


        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                myProgressDialog.dismiss();
                if (dataSnapshot.exists()) {

                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        if (user.child("password").getValue().equals(pass)) {

                            SharedPreferencesManager manager = new SharedPreferencesManager(getActivity());
                            manager.setPreferences(SharedPreferencesManager.USERNAME, email);
                            manager.setPreferences(SharedPreferencesManager.USERUNIQUEID, user.getKey());

                            FragmentManager fm = ((AppCompatActivity) getActivity()).getSupportFragmentManager();
                            fm.popBackStack();

                            new FragUtil(getActivity()).
                                    changeFragmentWithoutBackstack(new MyMapFragment(),
                                            R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
//                                            R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);

                        } else {
                            Toast.makeText(getContext(), "Invalid password!", Toast.LENGTH_SHORT).show();
                        }


                    }
                } else {
                    Toast.makeText(getContext(), "User does not exist!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                myProgressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to login! " + databaseError.toString(), Toast.LENGTH_LONG).show();
            }
        });


    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }
}