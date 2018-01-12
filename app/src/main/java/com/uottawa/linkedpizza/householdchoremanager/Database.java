package com.uottawa.linkedpizza.householdchoremanager;

import android.app.Activity;
import android.content.Context;
import android.icu.lang.UScript;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.database.*;

import java.io.Serializable;
import java.lang.AutoCloseable;
import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import java.lang.Thread;

/**
 * Created by Nick-JR on 11/22/2017.
 */

public final class Database<E> implements Runnable{

    // TAG is for show some tag logs in LOG screen.
    private static final String TAG = "LoginActivity";

    // Request sing in code. Could be anything as you required.
    private static final int RequestSignInCode = 7;

    // Singleton instance.
    private static volatile Database instance = null;

    private int accountsRead = 0;

    // Activity instances.
    private LoginActivity loginActivity = null;
    private RegistrationActivity registrationActivity = null;
    private MainActivity mainActivity = null;

    // Firebase Auth Object.
    private FirebaseAuth firebaseAuth;

    // Firebase Data Object.
    private DatabaseReference database;

    // Google API Client Object.
    private GoogleApiClient googleApiClient;

    // Google Sign In Object.
    private GoogleSignInAccount googleSignInAccount;

    // User objects.
    //private UserAccount user = null;
    private LocalAccounts localAccounts;

    // Constructor.
    private Database(LoginActivity loginActivity){
        this.loginActivity = loginActivity;
        localAccounts = localAccounts.getInstance();

        // Initialize firebase objects.
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    // Logs user into database auth.
    protected void login(final GoogleSignInAccount googleSignInAccount){
        // Authenticate firebase.
        firebaseLoginAuth(googleSignInAccount);
    }

    protected void registerLogin(GoogleSignInAccount googleSignInAccount){
        // Authenticate firebase.
        firebaseRegisterAuth(googleSignInAccount);
    }

    protected void registerUser(final UserAccount newUser){
        // Set user.
        if (newUser == null){
            makeToast("Couldn't register user: null user");
            return;
        }

        // Create user id, then write to database.
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserAccount user = new UserAccount(newUser);

                localAccounts.addAccount(user);
                // Update current user.
                if (localAccounts.getCurrentUser() == null) {
                    localAccounts.setCurrentUser(user);
                }

                writeUserToDatabase(user);
                registrationActivity.onRegisterComplete();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    protected void registerFamily(){
        // Set user.
        if (localAccounts == null){
            makeToast("Family not set: null users");
            return;
        }

        // Write family to database.
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            System.out.println("WRITING TO DATABASE." + localAccounts.getAccountAt(i));
            writeUserToDatabase(localAccounts.getAccountAt(i));
        }

        // Update family IDs.
        localAccounts.updateFamilyIDs();
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            database.child("users").child(localAccounts.getAccountAt(i).getUserID())
                    .child("family IDs").setValue(localAccounts.getAccountAt(i).getFamilyIDs());
        }
        //completeRegistration();
    }

    protected void registerToFamily(UserAccount userAccount){
        // Set user.
        if (localAccounts == null){
            makeToast("Family not set: null users");
            return;
        }

        // Write user to database.
        String key = writeUserToDatabase(userAccount);

        // Update family IDs.
        localAccounts.updateFamilyIDs();
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            database.child("users").child(localAccounts.getAccountAt(i).getUserID())
                    .child("family IDs").setValue(localAccounts.getAccountAt(i).getFamilyIDs());
        }
    }

    // Searches the database for an account with provided email.
    protected void accountExists(final String email){
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, HashMap> allUsersMap = (HashMap) snapshot.getValue();
                Map<String, String> userMap = new HashMap<>();

                System.out.println("email: " + email);

                if (allUsersMap != null) {
                    for (Map.Entry<String, HashMap> entry : allUsersMap.entrySet()) {
                        userMap = entry.getValue();
                        if (userMap != null) {
                            if (userMap.get("email") != null) {
                                if (userMap.get("email").equals(email)) {
                                    // Account exists.
                                    registrationActivity.onAccountFound();
                                    return;
                                }
                            }
                        }
                    }
                }
                // Account with this email not found.
                registrationActivity.onAccountNotFound();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // Reference https://stackoverflow.com/questions/33847225/generating-a-random-pin-of-5-digits
    // Generates a random 5 digit code for registering to a family.
    protected String generateCode(){
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String code = String.format("%05d", num);
        System.out.println(code);

        database.child("users").child(localAccounts.getCurrentUser().getUserID()).child("code").setValue(code);
        return code;
    }

    protected void searchCode(final String code){
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, HashMap> allUsersMap = (HashMap) snapshot.getValue();
                Map<String, String> userMap = new HashMap<>();

                if (allUsersMap != null) {
                    for (Map.Entry<String, HashMap> entry : allUsersMap.entrySet()) {
                        userMap = entry.getValue();
                        if (userMap.get("code") != null) {
                            if (userMap.get("code").equals(code)) {
                                // Family found!
                                // Get user id.
                                String id = userMap.get("user id");

                                System.out.println("user id: " + id);

                                ArrayList<String> familyIDs = (ArrayList<String>) snapshot.child(id).child("family IDs").getValue();
                                ArrayList<String> taskIDs = (ArrayList<String>) snapshot.child(id).child("task IDs").getValue();
                                Map<String, String> shoppingIDs = (HashMap) snapshot.child(id).child("shopping IDs").getValue();

                                if (familyIDs == null)
                                    familyIDs = new ArrayList<>();
                                familyIDs.add(id);

                                if (taskIDs != null)
                                    localAccounts.getCurrentUser().setTaskIDs(taskIDs);

                                if (shoppingIDs != null) {
                                    for (Map.Entry entry1 : shoppingIDs.entrySet()) {
                                        if (entry1.getValue() != null)
                                            localAccounts.getCurrentUser().addShoppingKey(entry1.getValue().toString());
                                    }
                                }

                                System.out.println(familyIDs);
                                localAccounts.getCurrentUser().setFamilyIDs(familyIDs);

                                /*
                                // Remove account as it will be re-added.
                                for (int i = 0; i < localAccounts.getFamilySize(); i++){
                                    if (localAccounts.getAccountAt(i).getEmail().equals(user.getEmail()))
                                        localAccounts.removeAccountAt(i);
                                }
                                */

                                writeUserToFamily();

                                // Update all account info.
                                return;
                            }
                        }
                    }
                }
                // Family not found.
                registrationActivity.onFamilyNotFound();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void firebaseLoginAuth(final GoogleSignInAccount googleSignInAccount) {

        final String email = googleSignInAccount.getEmail();
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){
                            // Check if account exists and save user data.
                            database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    // Retrieve all users in a hashmap.
                                    Map<String, HashMap> allUsersMap = (HashMap) snapshot.getValue();
                                    Map<String, String> userMap = new HashMap<>();

                                    // Database is empty.
                                    if (allUsersMap == null){
                                        loginActivity.userRegister();
                                        return;
                                    }

                                    for (Map.Entry<String, HashMap> entry : allUsersMap.entrySet()){
                                        System.out.println(entry);
                                        userMap = entry.getValue();
                                        System.out.println(userMap);

                                        if (userMap.containsValue(email)) {
                                            // Account found!
                                            UserAccount userAccount = new UserAccount(email);
                                            userAccount.setFirstName(userMap.get("first name"));
                                            userAccount.setLastName(userMap.get("last name"));
                                            userAccount.setUserID(userMap.get("user id"));

                                            if (userMap.get("parent") == "true")
                                                userAccount.setParent(true);
                                            else
                                                userAccount.setParent(false);

                                            // Set current logged in user.
                                            //localAccounts.addAccount(user);
                                            localAccounts.setCurrentUser(userAccount);
                                            loginActivity.onLoginComplete();

                                            //Thread login = new Thread(Database.this);
                                            updateLocalAccounts();
                                            // loggedIn(userAccount);
                                            return;
                                        }
                                    }
                                    Toast.makeText(loginActivity, "Welcome! Please register :)", Toast.LENGTH_SHORT).show();
                                    loginActivity.userRegister();
                                    return;
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value
                                    Log.w(TAG, "Failed to read value.", error.toException());
                                }
                            });
                        }else {
                            // Enter offline mode.
                            localAccounts.offlineMode();
                            // Authenticate from stored accounts.
                            int index = localAccounts.isLocalAccount(googleSignInAccount.getEmail());
                            if (index != -1) {
                                localAccounts.setCurrentUser(localAccounts.getAccountAt(index));
                                loginActivity.onOffline();
                            }
                            else {
                                return;
                            }
                        }
                    }
                });
    }

    private void firebaseRegisterAuth(GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(registrationActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){
                            registrationActivity.onFirebaseLogin();
                            return;
                        }else {
                            Toast.makeText(registrationActivity,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void run(){
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            // Set .
            updateLocalAccounts();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Updates the local accounts.
    private void updateLocalAccounts(){
        System.out.println("current user: " + localAccounts.getCurrentUser());
        System.out.println("current user: " + localAccounts.getCurrentUser().getEmail());
        System.out.println("current user: " + localAccounts.getCurrentUser().getUserID());

        // Update user data from the database.
        database.child("users").child(localAccounts.getCurrentUser().getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, String> userMap = (HashMap) snapshot.getValue();


                System.out.println(userMap.get("email"));

                // Parse user data.
                UserAccount userAccount = new UserAccount(userMap.get("email"));
                userAccount.setFirstName(userMap.get("first name"));
                userAccount.setLastName(userMap.get("last name"));
                userAccount.setNickname(userMap.get("nickname"));
                userAccount.setUserID(userMap.get("user id"));
                userAccount.setLevel(Integer.parseInt(userMap.get("level")));
                userAccount.setPoints(Integer.parseInt(userMap.get("points")));

                if (userMap.get("parent") != null) {
                    if (userMap.get("parent").equals("true"))
                        userAccount.setParent(true);
                    else
                        userAccount.setParent(false);
                }

                // Update family.
                ArrayList<String> familyIDs = null;
                if (userMap.get("family IDs") != null) {
                    familyIDs = (ArrayList<String>) snapshot.child("family IDs").getValue();

                    for (String id : familyIDs) {
                        System.out.println(id);
                        if (id != null) {
                            // Update local accounts.
                            userAccount.addFamilyID(id);
                            updateLocalAccounts(id);
                        }
                    }
                }

                // Update tasks.
               ArrayList<String> taskIDs = null;
                if (userMap.get("task IDs") != null){
                    taskIDs = (ArrayList<String>) snapshot.child("task IDs").getValue();

                    // Get tasks.
                    for (String id : taskIDs) {
                        if (id != null) {
                            readTask(id);
                        }
                    }
                }

                /*
                // Update completed tasks.
                ArrayList<String> completedIDs = null;
                if (userMap.get("completed task IDs") != null){
                    completedIDs = (ArrayList<String>) snapshot.child("completed task IDs").getValue();

                    // Get tasks.
                    for (String id : completedIDs) {
                        //userAccount.addCompletedTaskID(id);
                        if (id != null) {
                            //readCompletedTask(id);
                        }
                    }
                }
                */

                // Update shopping lists
                if (userMap.get("shopping IDs") != null) {
                    Map<String, String> shoppingKeys = (HashMap<String, String>) snapshot.child("shopping IDs").getValue();
                    for (Map.Entry<String, String> entry : shoppingKeys.entrySet()) {
                        readShoppingList(entry.getValue());
                    }
                }

                // Set current user.
                localAccounts.setCurrentUser(userAccount);
                // Store data locally.
                localAccounts.serializeAccounts();

                if (mainActivity != null) {
                    mainActivity.updateTaskView();
                }

                System.out.println("FAMILY IDS: " + familyIDs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // Recursive method for updating local accounts.
    private void updateLocalAccounts(String id){
        database.child("users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> familyMap = (HashMap) dataSnapshot.getValue();

                if (familyMap == null)
                    return;
                System.out.println(familyMap);

                // Parse account data.
                UserAccount userAccount = new UserAccount(familyMap.get("email"));
                userAccount.setFirstName(familyMap.get("first name"));
                userAccount.setLastName(familyMap.get("last name"));
                userAccount.setNickname(familyMap.get("nickname"));
                userAccount.setUserID(familyMap.get("user id"));

                if (familyMap.get("level") != null)
                    userAccount.setLevel(Integer.parseInt(familyMap.get("level")));
                if (familyMap.get("points") != null)
                    userAccount.setPoints(Integer.parseInt(familyMap.get("points")));

                if (familyMap.get("parent") != null && familyMap.get("parent").equals("true"))
                    userAccount.setParent(true);
                else
                    userAccount.setParent(false);

                ArrayList<String> familyIDs = (ArrayList) dataSnapshot.child("family IDs").getValue();
                if (familyIDs != null) {
                    userAccount.setFamilyIDs(familyIDs);
                }
                localAccounts.addAccount(userAccount);
                localAccounts.updateFamilyIDs();
                accountsRead++;
                mainActivity.updatePeopleView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readTask(final String taskID){
        database.child("tasks").child(taskID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> taskMap = (HashMap) dataSnapshot.getValue();

                if (taskMap == null)
                    return;

                // Parse task data.
                com.uottawa.linkedpizza.householdchoremanager.Task task = new com.uottawa.linkedpizza.householdchoremanager.Task(taskMap.get("name"));
                task.setStatus(taskMap.get("status"));
                task.setNote(taskMap.get("note"));
                task.setID(taskID);

                if (taskMap.get("reward") != null)
                    task.setReward(taskMap.get("reward"));

                if (taskMap.get("status") != null)
                    task.setStatus(taskMap.get("status"));

                // Set deadline.
                Map<String, Long> deadlineMap = (HashMap) dataSnapshot.child("deadline").getValue();
                if (deadlineMap != null) {
                    Date deadline = new Date(deadlineMap.get("year").intValue(),
                            deadlineMap.get("month").intValue(),
                            deadlineMap.get("day").intValue());
                    task.setDeadline(deadline);
                }

                // Set time.
                Map<String, Long> timeMap = (HashMap) dataSnapshot.child("time").getValue();
                if (timeMap != null) {
                    Time time = new Time(timeMap.get("hour").intValue(),
                                        timeMap.get("minute").intValue());
                    task.setDueTime(time);
                }

                // Set items.
                ArrayList<String> itemIDs = (ArrayList<String>) dataSnapshot.child("item IDs").getValue();
                if (itemIDs != null) {
                    for (String id : itemIDs) {
                        readItem(id, task);
                    }
                }

                // Update local accounts.
                for (int i = 0; i < localAccounts.getFamilySize(); i++) {
                    if (localAccounts.getAccountAt(i).getUserID().equals(taskMap.get("child id"))) {
                        // Set assigned child.
                        task.setAssignedUser(localAccounts.getAccountAt(i));
                    } else if (localAccounts.getAccountAt(i).getUserID().equals(taskMap.get("creator id"))) {
                        // Set creator.
                        task.setCreator(localAccounts.getAccountAt(i));
                    }
                }
                for (int i = 0; i < localAccounts.getFamilySize(); i++){
                    // Add task to parents.
                    localAccounts.getAccountAt(i).addTask(task);
                    localAccounts.getAccountAt(i).addTaskID(taskID);
                }
                mainActivity.updateTaskView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Reads a shopping list, then updates local accounts.
    private void readShoppingList(final String shoppingID){

        database.child("shopping list").child(shoppingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> shoppingMap = (HashMap) dataSnapshot.getValue();
                if (shoppingMap != null) {
                    if (shoppingMap.get("name") != null) {
                        Shopping shopping = new Shopping(shoppingMap.get("name"));
                        shopping.setId(shoppingID);

                        // Add items.
                        ArrayList<String> itemIDs = (ArrayList) dataSnapshot.child("item keys").getValue();
                        for (String id : itemIDs) {
                            readItem(id, shopping);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Reads an item to a task, then updates local accounts.
    private void readItem(final String itemID, final com.uottawa.linkedpizza.householdchoremanager.Task task){

        database.child("items").child(itemID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> itemMap = (HashMap) dataSnapshot.getValue();
                Item item = new Item();
                if (itemMap != null) {
                    item.setName(itemMap.get("name"));

                    if (itemMap.get("bought").equals("true"))
                        item.setHasBeenShopped(true);
                    else
                        item.setHasBeenShopped(false);

                    if (itemMap.get("shopping").equals("true"))
                        item.setIsShopping(true);
                    else
                        item.setIsShopping(false);

                    item.setId(itemID);
                    task.addRequiredItem(item);
                    task.addItemID(itemID);

                    for (int i = 0; i < localAccounts.getFamilySize(); i++) {
                        // Add task to parents.
                        localAccounts.getAccountAt(i).addTask(task);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Reads an item to a shopping list, then updates the list.
    private void readItem(final String itemID, final Shopping shopping){

        database.child("items").child(itemID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> itemMap = (HashMap) dataSnapshot.getValue();
                Item item = new Item();
                if (itemMap != null) {
                    item.setName(itemMap.get("name"));

                    if (itemMap.get("bought").equals("true"))
                        item.setHasBeenShopped(true);
                    else
                        item.setHasBeenShopped(false);

                    if (itemMap.get("shopping").equals("true"))
                        item.setIsShopping(true);
                    else
                        item.setIsShopping(false);

                    item.setId(itemID);

                    if (item != null) {
                        shopping.addItem(item);
                        localAccounts.getCurrentUser().addShoppingList(shopping);
                        mainActivity.updateShoppingView();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    // Logs user out of database auth.
    protected void logout(){
        System.out.println("LOGGING OUT OF FIREBASE.");
        firebaseAuth.getInstance().signOut();
        loginActivity.userSignOut();
        localAccounts.deleteAccounts();
    }

    // Removes account information from database.
    protected void removeAccount(){

    }

    protected void updateEmail(String email){
        FirebaseUser user = getUser();

        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                            sendVerificationEmail();
                        }
                    }
                });
    }

    // Writes a task to the database and returns its key.
    protected String writeTask(com.uottawa.linkedpizza.householdchoremanager.Task task){
        // Write task to database.
        DatabaseReference pushRef = database.child("tasks").push();

        // Put task data in a map.
        Map<String, String> taskMap = new HashMap<String, String>();
        if (task.getTaskName() != null)
            taskMap.put("name", task.getTaskName());
        if (task.getNote() != null)
            taskMap.put("note", task.getNote());
        if (task.getStatus() != null)
            taskMap.put("status", task.getStatus());
        if (task.getReward() != null)
            taskMap.put("reward", task.getReward());
        if (task.getAssignedUser() != null)
            taskMap.put("child id", task.getAssignedUser().getUserID());
        if (task.getCreator() != null)
            taskMap.put("creator id", task.getCreator().getUserID());
        pushRef.setValue(taskMap);

        // Get task id.
        String key = pushRef.getKey();
        task.setID(key);
        taskMap.put("id", task.getID());
        pushRef.setValue(taskMap);

        // Store items.
        DatabaseReference itemsRef = database.child("items").push();
        Item[] items = task.getRequiredItems();

        if (items != null) {
            ArrayList<String> itemKeys = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                Map<String, String> itemMap = new HashMap<String, String>();
                // Write items to database.
                itemMap.put("name", items[i].getName());
                if (items[i].getIsShopping())
                    itemMap.put("shopping", "true");
                else
                    itemMap.put("shopping", "false");

                if (items[i].getHasBeenShopped())
                    itemMap.put("bought", "true");
                else
                    itemMap.put("bought", "false");
                itemsRef.setValue(itemMap);
                itemKeys.add(itemsRef.getKey());
                itemsRef = database.child("items").push();
            }
            // Add key to task.
            task.setItemIDs(itemKeys);
            // Update task in database.
            database.child("tasks").child(key).child("item IDs").setValue(itemKeys);
        }

        // Update local accounts.
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            // Add id to parent accounts.
            System.out.println("USER TASK ID: " + localAccounts.getAccountAt(i).getTaskIDs());
            localAccounts.getAccountAt(i).addTaskID(key);
            System.out.println("update tsk" + task.getID());
            localAccounts.getAccountAt(i).addTask(task);
        }

        // Update database.
        ArrayList<String> taskIds = localAccounts.getCurrentUser().getTaskIDs();
        for (int i = 0; i < localAccounts.getFamilySize(); i++) {
            // Update account in database.
            database.child("users").child(localAccounts.getAccountAt(i).getUserID()).child("task IDs").setValue(taskIds);
        }

        // Set dates.
        database.child("tasks").child(key).child("deadline").setValue(task.getDeadline());
        database.child("tasks").child(key).child("deadline").setValue(task.getDeadline());
        database.child("tasks").child(key).child("repeat duration").setValue(task.getRepeatDuration());
        database.child("tasks").child(key).child("time").setValue(task.getDueTime());

        return key;
    }

    // Overwrites a task in the database.
    protected void editTask(com.uottawa.linkedpizza.householdchoremanager.Task task){
        updateTask(task);
    }

    private void updateTask(com.uottawa.linkedpizza.householdchoremanager.Task task){
        if (task == null)
            return;

        // Delete task.
        deleteTask(task);
        writeTask(task);
    }

    // Called when the child sets a task as completed.
    protected void setTaskStatus(com.uottawa.linkedpizza.householdchoremanager.Task task){
        // Set status.
        database.child("tasks").child(task.getID()).child("status").setValue(task.getStatus());
    }

    // Sets a task to completed in the database/
    protected void taskCompleted(com.uottawa.linkedpizza.householdchoremanager.Task task){
        // Update task.
        setTaskStatus(task);

        // Update users.
        for (UserAccount user : localAccounts.getLocalAccounts()) {
            updateUserDatabase(user);
        }
        // todo update child's points
    }

    protected void deleteTask(com.uottawa.linkedpizza.householdchoremanager.Task task){
        database.child("tasks").child(task.getID()).removeValue();

        // Delete items.
        ArrayList<String> itemIDs = task.getItemIDs();
        if (itemIDs != null) {
            for (String id : itemIDs) {
                database.child("items").child(id).removeValue();
            }
        }

        ArrayList<String> taskIDs = localAccounts.getCurrentUser().getTaskIDs();
        // Remove task reference from family.
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            // Remove from parents.
            localAccounts.getAccountAt(i).removeTask(task);
            // Update database.
            database.child("users").child(localAccounts.getAccountAt(i).getUserID()).child("task IDs").setValue(taskIDs);
            //database.child("users").child(localAccounts.getAccountAt(i).getUserID()).child("task IDs").child(task.get)
        }
    }


    protected void completeTask(com.uottawa.linkedpizza.householdchoremanager.Task task){
        database.child("tasks").child(task.getID()).removeValue();

        ArrayList<String> taskIDs = localAccounts.getCurrentUser().getTaskIDs();
        // Remove task reference from family.
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            // Remove from parents.
            localAccounts.getAccountAt(i).taskCompleted(task);
            // Update database.
            database.child("users").child(localAccounts.getAccountAt(i).getUserID()).child("task IDs").setValue(taskIDs);
            //database.child("users").child(localAccounts.getAccountAt(i).getUserID()).child("task IDs").child(task.get)
        }
    }

    // Writes an instance of shopping class to the database.
    protected void writeShoppingList(Shopping shopping){
        DatabaseReference pushRef = database.child("shopping list").push();

        // Put shopping list data in a map.
        Map<String, String> shoppingMap = new HashMap<String, String>();
        if (shopping.getListName() != null)
            shoppingMap.put("name", shopping.getListName());
        pushRef.setValue(shoppingMap);

        // Get shopping id.
        String key = pushRef.getKey();
        shopping.setId(key);

        // Write list items.
        if (shopping.getItems() != null){
            ArrayList<String> itemKeys = new ArrayList<>();
            Item[] items = shopping.getItems();
            for (int i = 0; i < items.length; i++){
                itemKeys.add(writeItem(items[i]));
            }
            // Write item keys into shopping.
            database.child("shopping list").child(key).child("item keys").setValue(itemKeys);
        }

        // Write key into family.
        ArrayList<UserAccount> accounts = localAccounts.getLocalAccounts();
        for (int i = 0; i < accounts.size(); i++){
            localAccounts.getAccountAt(i).addShoppingKey(key);
            DatabaseReference keyRef = database.child("users").child(accounts.get(i).getUserID()).child("shopping IDs").push();
            keyRef.setValue(key);
        }
    }

    protected void deleteShoppingList(final Shopping shopping){
        if (shopping.getId() == null)
            return;

        database.child("shopping list").child(shopping.getId()).child("item keys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> keys = (ArrayList<String>) dataSnapshot.getValue();
                // Delete all items in the shopping list.
                for (int i = 0; i < keys.size(); i++) {
                    deleteItem(keys.get(i));
                }

                for (int i = 0; i < localAccounts.getFamilySize(); i++) {
                    //TODO: Remove shopping list from assigned user in database
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Delete shopping list.
        database.child("shopping list").child(shopping.getId()).removeValue();
    }

    protected void addToShoppingList(final Shopping shopping, final Item item){
        System.out.println("sid" + shopping.getId());

        database.child("shopping list").child(shopping.getId()).child("item keys").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> itemKeys = (ArrayList) dataSnapshot.getValue();
                itemKeys.add(writeItem(item));
                database.child("shopping list").child(shopping.getId()).child("item keys").setValue(itemKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /*
    // Updates a shopping list in the database.
    protected void updateShoppingList(Shopping shopping){
        if (shopping.getId() == null){
            // Shopping list not in database.
            writeShoppingList(shopping);
            return;
        }

        // Update shopping list.
        deleteShoppingList(shopping);
        writeShoppingList(shopping);
    }
    */

    // Writes an item and returns its key.
    protected String writeItem(Item item){
        // Get push reference.
        DatabaseReference pushRef = database.child("items").push();

        // Put item data in a map.
        Map<String, String> itemMap = new HashMap<String, String>();
        itemMap.put("name", item.getName());

        if (item.getIsShopping())
            itemMap.put("shopping", "true");
        else
            itemMap.put("shopping", "false");

        if (item.getHasBeenShopped())
            itemMap.put("bought", "true");
        else
            itemMap.put("bought", "false");

        // Write item.
        pushRef.setValue(itemMap);

        // Return item key.
        return pushRef.getKey();
    }

    // Updates an item.
    protected void updateItem(Item item){
        if (item == null)
            return;

        if (item.getId() != null) {
            Map<String, String> itemMap = new HashMap<String, String>();
            itemMap.put("name", item.getName());
            if (item.getIsShopping())
                itemMap.put("shopping", "true");
            else
                itemMap.put("shopping", "false");

            if (item.getHasBeenShopped())
                itemMap.put("bought", "true");
            else
                itemMap.put("bought", "false");

            database.child("items").child(item.getId()).setValue(itemMap);
        }
    }

    // Deletes an item from the database by its key.
    protected void deleteItem(String id){
        if (id == null)
            return;
        database.child("items").child(id).removeValue();
    }

    // Debugging. TO BE REMOVED.
    protected void clear(){
        database.child("users").removeValue();
    }

    // Private database methods. //

    private void completeRegistration(){
        registrationActivity.onRegisterComplete();
    }

    // Writes a new user to the database.
    private String writeUserToDatabase(UserAccount user) {
        DatabaseReference pushRef = database.child("users").push();

        // Put user data into a map.
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("first name", user.getFirstName());
        userMap.put("last name", user.getLastName());
        userMap.put("nickname", user.getNickname());
        userMap.put("email", user.getEmail());
        userMap.put("level", "0");
        userMap.put("points", "0");

        if (user.isParent())
            userMap.put("parent", "true");
        else
            userMap.put("parent", "false");

        // Write user to database.
        pushRef.setValue(userMap);
        String key = pushRef.getKey();
        user.setUserID(key);

        // Save user id.
        user.setUserID(key);
        localAccounts.addAccount(user);
        localAccounts.updateFamilyIDs();

        // Rewrite user info with user id.
        userMap.put("user id", user.getUserID());
        database.child("users").child(user.getUserID()).setValue(userMap);

        if (user.getFamilyIDs() != null)
            database.child("users").child(user.getUserID()).child("family IDs").setValue(user.getFamilyIDs());
        return key;
    }

    // Writes a new user to the database.
    private void writeUserToFamily() {
        DatabaseReference pushRef = database.child("users").push();

        UserAccount user = new UserAccount(localAccounts.getCurrentUser());

        // Put user data into a map.
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("first name", user.getFirstName());
        userMap.put("last name", user.getLastName());
        userMap.put("nickname", user.getNickname());
        userMap.put("email", user.getEmail());
        userMap.put("level", "0");
        userMap.put("points", "0");

        if (user.isParent())
            userMap.put("parent", "true");
        else
            userMap.put("parent", "false");

        // Write user to database.
        pushRef.setValue(userMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                // Set current user.
                //localAccounts.deleteAccounts();
                //localAccounts.setCurrentUser(user);

                System.out.println(localAccounts.getCurrentUser().getFamilyIDs());

                // Read family accounts
                updateLocalAccounts();
            }
        });

        final String key = pushRef.getKey();
        user.setUserID(key);

        // Save user id.
        user.setUserID(key);
        localAccounts.addAccount(user);
        //localAccounts.updateFamilyIDs();

        // Rewrite user info with user id.
        userMap.put("user id", user.getUserID());
        database.child("users").child(user.getUserID()).setValue(userMap);
        database.child("users").child(user.getUserID()).child("family IDs").setValue(localAccounts.getCurrentUser().getFamilyIDs());
        database.child("users").child(user.getUserID()).child("task IDs").setValue(localAccounts.getCurrentUser().getTaskIDs());

        if (localAccounts.getCurrentUser().getShoppingKeys() != null) {
            for (String id : localAccounts.getCurrentUser().getShoppingKeys()) {
                if (id != null) {
                    DatabaseReference pushref = database.child("users").child(user.getUserID()).child("shopping IDs").push();
                    pushref.setValue(id);
                }
            }
        }
        // Update other accounts.
        if (localAccounts.getCurrentUser().getFamilyIDs() != null) {
            for (final String id : localAccounts.getCurrentUser().getFamilyIDs()) {
                database.child("users").child(id).child("family IDs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> familyIDs = (ArrayList) dataSnapshot.getValue();


                        if (familyIDs == null)
                            familyIDs = new ArrayList<>();

                        familyIDs.add(key);
                        database.child("users").child(id).child("family IDs").setValue(familyIDs);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }


    // Writes an existing user to the database.
    private void updateUserDatabase(UserAccount user) {
        // Put user data into a map.
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("first name", user.getFirstName());
        userMap.put("last name", user.getLastName());
        userMap.put("nickname", user.getNickname());
        userMap.put("email", user.getEmail());
        userMap.put("user id", user.getUserID());
        userMap.put("points", user.getPoints());
        userMap.put("level", user.getLevel());

        if (user.isParent())
            userMap.put("parent", "true");
        else
            userMap.put("parent", "false");

        //System.out.println("comped tasks" + user.getCompletedTaskIDs());

        // Write data.
        database.child("users").child(user.getUserID()).setValue(userMap);
        if (user.getFamilyIDs() != null)
            database.child("users").child(user.getUserID()).child("family IDs").setValue(user.getFamilyIDs());
        if (user.getTaskIDs() != null)
            database.child("users").child(user.getUserID()).child("task IDs").setValue(user.getTaskIDs());
       // if (user.getCompletedTaskIDs() != null)
        //    database.child("users").child(user.getUserID()).child("completed task IDs").setValue(user.getCompletedTaskIDs());
        if (user.getShoppingKeys() != null)
            database.child("users").child(user.getUserID()).child("shopping IDs").setValue(user.getShoppingKeys());
    }



    private String getFamilyID(String email){

        return "1";
    }

    // Re-authenticate.
    private void reAuthenticateUser(){
        FirebaseUser user = getUser();
        AuthCredential credential = EmailAuthProvider.getCredential("user@example.com", "password1234");

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "User re-authenticated.");
                    }
                });
    }

    // Sends a verification email to the user.
    private void sendVerificationEmail(){
        FirebaseUser user = getUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void setLoginActivity(LoginActivity loginActivity){
        this.loginActivity = loginActivity;
    }

    private void makeToast(String s){
        Toast.makeText(loginActivity, s,Toast.LENGTH_SHORT).show();
    }

    // Returns the current user.
    private FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }



    // GETTERS AND SETTERS //

    // Singleton lazy initialization and instance getter.
    protected static Database getInstance(LoginActivity loginActivity){
        if (instance == null){
            synchronized (Database.class){
                if (instance == null){
                    if (loginActivity != null) {
                        instance = new Database(loginActivity);
                    }
                }
            }
        }
        else{
            synchronized (Database.class){
                if (instance != null){
                    if (loginActivity != null) {
                        Database db = instance;
                        db.setLoginActivity(loginActivity);
                    }
                }
            }
        }
        return instance;
    }

    protected void setRegistrationActivity(RegistrationActivity ra){
        registrationActivity = ra;
    }

    protected void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    protected void setGoogleSignIn(GoogleSignInAccount googleSignInAccount){
        this.googleSignInAccount = googleSignInAccount;
    }

    protected void setGoogleApiClient(GoogleApiClient googleApiClient){
        this.googleApiClient = googleApiClient;
    }
    protected GoogleApiClient getGoogleApiClient(){
        return googleApiClient;
    }

    // USER PROTECTED METHODS //

    // Checks if user is signed in.
    protected boolean isSignedIn(){
        return getUser() != null;
    }

    // Set's current user's nickname.
    protected void setNickname(String s){
        // TODO: validate nickname.
        localAccounts.getCurrentUser().setNickname(s);
    }

    protected void setEmail(String s){
        // TODO: Validate email.
        localAccounts.getCurrentUser().setEmail(s);

        // TODO: UPDATE DATABASE.

        sendVerificationEmail();
    }

    protected void setParent(boolean parent){
        localAccounts.getCurrentUser().setParent(parent);
    }
}