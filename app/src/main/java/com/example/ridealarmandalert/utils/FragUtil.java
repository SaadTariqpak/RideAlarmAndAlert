package com.example.ridealarmandalert.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragUtil {
    private
    Context context;

    public FragUtil(Context context) {
        this.context = context;
    }

    public void changeFragmentWithoutBackstack(Fragment fg, int id, int anim1, int anim2) {
        try {


            FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            if (anim1 != -1 && anim2 != -1)
                fragmentTransaction.setCustomAnimations(
                        anim1, anim2

                );

            fragmentTransaction.replace(id, fg);
            fragmentTransaction.commit();
        } catch (Exception ignored) {
        }
    }

    public void changeFragmentWithBackstack(Fragment fg, int id, int anim1, int anim2, int anim3, int anim4) {
        try {


            FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    anim1, anim2, anim3, anim4

            );
            fragmentTransaction.replace(id, fg);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } catch (Exception ignored) {
        }
    }

}
