package com.equipoazteca.ollin.controllers.application;


import android.app.Application;
import androidx.annotation.NonNull;

import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import com.bugsnag.android.BeforeNotify;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Error;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;


public class EquipoAztecApplication extends Application {

    private static final String TAG = EquipoAztecApplication.class.getSimpleName();

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate() {
        Log.d(TAG, "process onCreate ...");
        super.onCreate();
        FirebaseApp.initializeApp(this);

        Bugsnag.init(this);

        Bugsnag.setUser("123456", "noauthentication@bug.com", "Bug Bug");

        mAuth = FirebaseAuth.getInstance();

        //authentication session
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged::signed_in:" + user.getUid());
                    Log.d(TAG, "onAuthStateChanged::signed_in:" + user.getEmail());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged::signed_out");
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        Bugsnag.beforeNotify(new BeforeNotify() {
            @Override
            public boolean run(@NonNull Error error) {
                Log.d(TAG, "bugsnag update...");

                // Attach customer information to every error report
                error.addToTab("BootsOnGround", "company", "Equipo Azteca");
                error.addToTab("BootsOnGround", "anonymous_user", true);
                error.addToTab("BootsOnGround", "language", getCurrentLanguage());
                return true;
            }
        });
        //
        // Test the integration
        //Bugsnag.notify(new RuntimeException("Test error"));
        Bugsnag.leaveBreadcrumb("App loaded");


    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(mAuthListener != null) {
            mAuth.signOut();
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    public void logoutUser() {
        mAuth.signOut();
    }

    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    private String getCurrentLanguage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return LocaleList.getDefault().get(0).getLanguage();
        } else{
            return Locale.getDefault().getLanguage();
        }
    }
}
