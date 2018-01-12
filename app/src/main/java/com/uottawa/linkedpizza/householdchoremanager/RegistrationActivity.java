package com.uottawa.linkedpizza.householdchoremanager;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Thread;
import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;
/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity implements Runnable{
    // Request sing in code. Could be anything as you required.
    private static final int RequestSignInCode = 7;

    private static int id = 0;

    // Google API Client object.
    private GoogleApiClient googleApiClient;

    private GoogleSignInAccount googleSignInAccount;

    // Google Sign In button .
    com.google.android.gms.common.SignInButton signInButton;

    // Database object.
    private Database db;
    private UserAccount user;
    private LocalAccounts localAccounts;
    private boolean formState = true;
    private boolean loginState = false;
    private boolean checkExists = true;

    // UI references.
    private Switch parent;
    private ImageButton codeAbout;
    private EditText codeText;
    private Button registerButton;
    private TextView title;
    private TextView subtitle;
    private EditText mNickname;
    private Space space;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Retrieve state from intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            formState = extras.getBoolean("state", false);
        }

        // Get database instance.
        db = db.getInstance(new LoginActivity());
        localAccounts = localAccounts.getInstance();

        // Set up the login form.
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        parent = findViewById(R.id.parent);
        mNickname = findViewById(R.id.nickname);
        space = findViewById(R.id.space);

        // Set up UI Clickables.
        signInButton = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        registerButton = (Button) findViewById(R.id.registerButton);
        codeText = findViewById(R.id.code);
        codeAbout = findViewById(R.id.about);

        // Generate family code.
        if (!formState){
            String code = db.generateCode();
            codeText.setText(code);
            codeText.setKeyListener(null);
        }

        // Creating and Configuring Google Sign In object.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(RegistrationActivity.this)
                .enableAutoManage(RegistrationActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        registerButton.setVisibility(View.GONE);

        // Check state.
        updateUI();

        // Click listener for register submit button.
        registerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                // Finished registering new family.
                if (formState) {
                    onRegisterFamily();
                }
            }
        });

        // Adding Click listener to User Sign in Google button.
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validData()) {
                    System.out.println("VALID");

                    if (!formState) {
                        // Registering to a family.
                        db.setRegistrationActivity(RegistrationActivity.this);
                        userSignIn();
                    } else {
                        // Registering a new family.
                        userSignIn();
                    }
                }
            }
        });

        codeAbout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display dialog.
                System.out.println("ALERT");
                if (formState)
                    dialog();
                else
                    parenDialog();
            }
        });

        findViewById(R.id.title).requestFocus();
    }

    @Override
    public void onBackPressed()
    {
        onCancelRegisterFamily();
    }

    @Override
    public void run(){
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        if (formState)
            db.registerFamily();
        else
            db.registerToFamily(user);
    }

    // Sign In function Starts From Here.
    protected void userSignIn() {
        //ADDED
        // Passing Google Api Client into Intent.
        Auth.GoogleSignInApi.signOut(googleApiClient);
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent, RequestSignInCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestSignInCode) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()) {
                // Google auth complete; start Firebase auth.
                googleSignInAccount = googleSignInResult.getSignInAccount();
                onGoogleLogin();
            }
        }
    }

    // Google login successful.
    private void onGoogleLogin() {
        String email = googleSignInAccount.getEmail();

        // Check if email is already used.
        if (localAccounts.getLocalAccounts() != null) {
            for (UserAccount user : localAccounts.getLocalAccounts()) {
                if (email.compareTo(user.getEmail()) == 0) {
                    makeToast("Each account must have separate emails.");
                    return;
                }
            }
        }

        db = db.getInstance(null);
        db.setRegistrationActivity(this);

        // Log into firebase.
        db.registerLogin(googleSignInAccount);
    }

    // CALL BACK METHODS //

    protected void onFirebaseLogin(){
        // Check if account already exists.
        db.accountExists(googleSignInAccount.getEmail());
        checkExists = false;
    }

    // Called from database if account in found.
    protected void onAccountFound() {
        System.out.println("Account found!");

        if (!loginState){
            // Tell user that
            makeToast("Account with that email already exists.");
        }
        else {
            // Register account to family.
            db.setRegistrationActivity(this);
            db.registerUser(user);
        }
    }

    protected void onAccountNotFound() {
        // Create user account.
        user = new UserAccount(googleSignInAccount.getEmail());
        user.setFirstName(googleSignInAccount.getGivenName());
        user.setLastName(googleSignInAccount.getFamilyName());
        user.setNickname(mNickname.getText().toString());
        user.setParent(parent.isChecked());

        // set login id.
        user.setUserID(Integer.toString(id++));

        // Display message to user.
        Toast.makeText(RegistrationActivity.this, "Account " + mNickname.getText() + " created!", Toast.LENGTH_SHORT).show();

        if (formState) {
            // Check for code.
            if (!codeText.getText().toString().isEmpty()){
                // Register to a family.
                String code = codeText.getText().toString();
                if (code.length() == 5){
                    // Valid.
                    localAccounts.setCurrentUser(user);
                    db.searchCode(code);
                    onRegisterComplete();
                }
                else{
                    makeToast("Code must be 5 digits");
                }
            }
            else {
                // Form state: creating a new family.
                // Add account to list.
                localAccounts.addAccount(user);
                onFamilyMemberAuthorized();
            }
        }
        else {
            onRegisterFamily();
        }
    }

    protected void onFamilyMemberAuthorized(){
        registerButton.setVisibility(View.VISIBLE);

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.registration_layout), "Delete account " + localAccounts.getAccountAt(id-1).getNickname(), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                undoRegisterAccount();
            }
        });
        snackbar.show();

        // Make first account login.
        if (localAccounts.getCurrentUser() == null){
            localAccounts.setCurrentUser(user);
        }
    }

    protected void onFamilyMemberNotAuthorized(){
        // Account authorization failed.
        Toast.makeText(RegistrationActivity.this, "Error: account authorization failed.", Toast.LENGTH_SHORT).show();
    }

    // Called when database did not find family code.
    protected void onFamilyNotFound(){
        makeToast("Family code not found");
    }

    // Called when the user clicks submit when registering a family.
    protected void onRegisterFamily(){
        Thread databaseWorker = new Thread(this);
        databaseWorker.start();
        onRegisterComplete();
    }

    // Called when account logged in to register to a family.
    protected void onRegisterUser(){
        Thread databaseWorker = new Thread(this);
        databaseWorker.start();
        onRegisterComplete();
    }

    // Called when the user clicks cancel when registering a family.
    protected void onCancelRegisterFamily(){
        super.onBackPressed();

        if (formState) {
            // Clear local accounts.
            localAccounts.deleteAccounts();

            // Move to login activity.
            Intent loginScreen = new Intent(this, LoginActivity.class);
            loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginScreen);
            finish();
        }
        else {
            Intent settingScren = new Intent(this, SettingsActivity.class);
            settingScren.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(settingScren);
            finish();
        }
    }

    // Called when registration is completed.
    protected void onRegisterComplete() {
        // Move to main activity.
        if (formState){
            // TODO Serialize local accounts.
            Toast.makeText(getApplicationContext(), "Welcome! Family created!", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Now logged into " + localAccounts.getCurrentUser().getNickname(), Toast.LENGTH_LONG).show();
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
        else {
            Toast.makeText(RegistrationActivity.this, "Welcome " + googleSignInAccount.getGivenName() + "!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private void makeToast(String s){
        Toast.makeText(RegistrationActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    // Undo action.
    private void undoRegisterAccount(){
        if (id > 0) {
            makeToast("Account " + localAccounts.getAccountAt(--id).getNickname() + " has been deleted.");
            localAccounts.removeAccountAt(id);

            // Can't get user at index -1.
            if (id > 0) {
                final Snackbar snackbar = Snackbar.make(findViewById(R.id.registration_layout), "Delete account " + localAccounts.getAccountAt(id - 1).getNickname(), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        undoRegisterAccount();
                    }
                });
                snackbar.show();
            }
        }
        if (id == 0) {
            registerButton.setVisibility(View.GONE);
        }
    }

    private void updateUI(){
        if (!formState){
            // Display register to family form.
            title.setText("Add Account");
            subtitle.setText("Sign in with new member's gmail");
            space.setMinimumHeight(40);
            registerButton.setText("Submit");
            registerButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }
        else{
            // Display register new family form.
            title.setText("REGISTER NEW FAMILY");
            subtitle.setText("Sign in for each account, then submit to complete family registration :)");
            space.setMinimumHeight(26);
            //loginButton.setVisibility(View.VISIBLE);
            registerButton.setText("Submit");
            signInButton.setVisibility(View.VISIBLE);
            if (localAccounts.getFamilySize() == 0)
                registerButton.setVisibility(View.GONE);
        }
    }

    private boolean validData(){
        System.out.println("nickname length " + mNickname.getText().length());

        // Validate nickname.
        if (mNickname.getText().length() < 1){
            Toast.makeText(RegistrationActivity.this, "Nickname not set.", Toast.LENGTH_LONG).show();
            return false;
        }

        // Validate email.
        if (formState && codeText.getText().toString().isEmpty() && id == 0 && !parent.isChecked()){
            makeToast("First account must be a parent account");
            return false;
        }
        return true;
    }

    //reference Nevin
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    public void dialog(){
        new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle("Registering With a Family Code")
                .setMessage("A family code let's you register to an existing family!")
                .setNegativeButton("How to get a family code", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new AlertDialog.Builder(RegistrationActivity.this)
                                    .setTitle("Registering With a Family Code")
                                    .setMessage("A parent can go to (Settings->Add Family Member) to get the family code.")
                                    .setPositiveButton("Close", null).create().show();
                        }
                    })
                .setPositiveButton("Close", null)
                .create().show();
    }

    public void parenDialog(){
        new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle("Registering With a Family Code")
                .setMessage("A family code let's someone register to your family using another device!")
                .setNegativeButton("More", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(RegistrationActivity.this)
                                .setTitle("Registering With a Family Code")
                                .setMessage("Give this code to a family member so they can register to your family.")
                                .setPositiveButton("Close", null).create().show();
                    }
                })
                .setPositiveButton("Close", null)
                .create().show();
    }

}