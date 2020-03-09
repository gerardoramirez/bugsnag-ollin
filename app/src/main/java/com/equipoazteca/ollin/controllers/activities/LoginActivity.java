package com.equipoazteca.ollin.controllers.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsnag.android.BeforeNotify;
import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Error;
import com.equipoazteca.ollin.R;
import com.equipoazteca.ollin.controllers.application.EquipoAztecApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.EmptyStackException;
import java.util.HashMap;

import butterknife.BindView ;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int REQUEST_SIGNUP = 0;

    private EquipoAztecApplication equipoAztecApplication;

    @BindView (R.id.input_email)
    EditText emailText;
    @BindView (R.id.input_password)
    EditText passwordText;
    @BindView (R.id.btn_login)
    Button loginButton;
    @BindView (R.id.link_signup)
    TextView signupLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if(equipoAztecApplication != null) {
            if(isUserLoggedInToApp()) {
                Log.d(TAG, "current user is:  " + equipoAztecApplication.getFirebaseAuth().getCurrentUser());
                skipLoginActivity();
            }
        }
        // get the common context of the application ...
        equipoAztecApplication = (EquipoAztecApplication) getApplication();

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "process Login::onClick ...");
                Bugsnag.leaveBreadcrumb("User clicked Login");
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sending user to SignUp Activity ...");

                // Transfer user to Sign Up Activity ...
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "call to onStart ...");

        if(equipoAztecApplication != null ) {
            if(isUserLoggedInToApp()) {
                skipLoginActivity();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "call to onStop ...");

    }
    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
            Bugsnag.leaveBreadcrumb("Preference updated", BreadcrumbType.STATE, new HashMap<String, String>() {{
                put("from", "moka");
                put("to", "french press");
            }});

            // Throw an empty exception
            //throw new EmptyStackException();
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "process signIn()" + email);
        if (!validate()) {
            return;
        }

        showProgressDialog();
        setBugsnagUser(email, "");
        equipoAztecApplication.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:onComplete", task.getException());
                            onLoginFailed();
                        } else {
                            // On complete call either onLoginSuccess or onLoginFailed
                            onLoginSuccess();

                            Intent intent = new Intent(LoginActivity.this, EquipoAztecaMainActivity.class);
                            startActivity(intent);
                        }
                        hideProgressDialog();
                    }
                });
    }

    public void login() {
        Log.d(TAG, "process Login() ...");
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        if (!validate()) {
            Log.d(TAG, "validation did not pass ...");

            progressDialog.dismiss();
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        signIn(email,password);
        progressDialog.dismiss();
    }

    private void skipLoginActivity() {
        Intent intent = new Intent(LoginActivity.this, EquipoAztecaMainActivity.class);
        startActivity(intent);
    }

    private boolean isUserLoggedInToApp() {
        Log.d(TAG, "current user is:  " + equipoAztecApplication.getFirebaseAuth().getCurrentUser());

        return equipoAztecApplication.getFirebaseAuth().getCurrentUser() != null;
    }

    private void setBugsnagUser(String email, String name) {
        Bugsnag.setUser("123456",email, name);
    }
}
