package com.equipoazteca.ollin.controllers.application;


import android.app.Application;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EquipoAztecApplication extends Application {

    private static final String TAG = EquipoAztecApplication.class.getSimpleName();

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate() {
        Log.d(TAG, "process onCreate ...");
        super.onCreate();

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth != null) {
            // do your stuff
        } else {
        }

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


}
