package com.uottawa.linkedpizza.householdchoremanager;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.common.api.Status;

/**
 * Created by iDarkDuck on 11/12/17.
 * Modified by Nick on 11/25/17.
 */

public class LoginActivity extends AppCompatActivity{

    // TAG is for show some tag logs in LOG screen.
    private static final String TAG = "LoginActivity";

    // Request sing in code. Could be anything as you required.
    protected static final int RequestSignInCode = 7;

    // Class objects.
    private LocalAccounts localAccounts;
    private Database db;

    // Google API Client object.
    static private GoogleApiClient googleApiClient;

    // Sing out button.
    Button SignOutButton;

    // Google Sign In button .
    com.google.android.gms.common.SignInButton signInButton;

    // Register button.
    protected Button registerButton;

    // TextView to Show Login User Email and Name.
    TextView LoginUserName, LoginUserEmail, loginStatusText;

    //TextView title
    TextView logintitle1,logintitle2,logintitle3;
    ImageView logintitle4;
    ProgressBar progressBar;

    //@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TAG is for show some tag logs in LOG screen.
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        SignOutButton= (Button) findViewById(R.id.sign_out);

        LoginUserName = (TextView) findViewById(R.id.textViewName);

        LoginUserEmail = (TextView) findViewById(R.id.textViewEmail);

        loginStatusText = (TextView) findViewById(R.id.loginLoadingText);

        signInButton = (com.google.android.gms.common.SignInButton)findViewById(R.id.sign_in_button);
        registerButton = (Button) findViewById(R.id.register_button);

        // TO BE REMOVED.
        Button clear = (Button) findViewById(R.id.clear);
        clear.setVisibility(View.GONE);
        db = db.getInstance(this);

        //added for page title
        logintitle1= (TextView) findViewById(R.id.about_title);
        logintitle2= (TextView) findViewById(R.id.about_title2);
        logintitle3= (TextView) findViewById(R.id.about_title3);
        logintitle4= (ImageView) findViewById(R.id.login_app_icon);
        progressBar= (ProgressBar) findViewById(R.id.progressBar);

        // Hiding the TextView on activity start up time.
        LoginUserEmail.setVisibility(View.GONE);
        LoginUserName.setVisibility(View.GONE);


        // Creating and Configuring Google Sign In object.
        GoogleSignInOptions gso  = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(LoginActivity.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Adding Click listener to User Sign in Google button.
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSignIn();
            }
        });
        // Adding Click listener to register button.
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRegister();
            }
        });
        // Adding Click listener to CLEAR button.
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.clear();
            }
        });
        updateUI();
    }
    // Sign In function Starts From Here.
    protected void userSignIn(){
        // Hiding Login in button.
        signInButton.setVisibility(View.GONE);

        //ADDED
        logintitle1.setVisibility((View.GONE));
        logintitle2.setVisibility((View.GONE));
        logintitle3.setVisibility((View.GONE));
        logintitle4.setVisibility((View.GONE));
        progressBar.setVisibility((View.VISIBLE));
        registerButton.setVisibility(View.GONE);

        loginStatusText.setVisibility(View.VISIBLE);
        // Passing Google Api Client into Intent.
        Auth.GoogleSignInApi.signOut(googleApiClient);
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent, RequestSignInCode);
    }

    // Sign In function Starts From Here.
    protected void userRegister(){
        // Move to register activity.
        Intent register = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(register);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestSignInCode){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()){
                // Google auth complete; start Firebase auth.
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                db = db.getInstance(this);

                // Set google instances.
                db.setGoogleApiClient(googleApiClient);
                db.setGoogleSignIn(googleSignInAccount);

                db.login(googleSignInAccount);
            }
            else{
                makeToast("Google authentication failed.");
                userRegister();
            }
        }
    }

    protected void userSignOut() {
        System.out.println(googleApiClient);

        if (googleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(googleApiClient);
        }

        // After logout setting up login button visibility to visible.
        if (signInButton != null) {
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    // Called when the user logs in.
    protected void onLoginComplete(){
        // Welcome user and move to main activity.
        localAccounts = localAccounts.getInstance();

        Toast.makeText(LoginActivity.this, "Welcome " + localAccounts.getCurrentUser().getFirstName() + "!", Toast.LENGTH_LONG).show();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
    }

    protected void onOffline(){
        // Welcome user and move to main activity.
        localAccounts = localAccounts.getInstance();

        Toast.makeText(LoginActivity.this, "Entering offline mode.", Toast.LENGTH_LONG).show();
        Toast.makeText(LoginActivity.this, "Welcome!", Toast.LENGTH_LONG).show();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
    }

    private void updateUI(){
        registerButton.setVisibility(View.VISIBLE);
    }

    private void makeToast(String s){
        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}