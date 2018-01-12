package com.uottawa.linkedpizza.householdchoremanager;

import android.widget.ArrayAdapter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick-JR on 11/26/2017.
 */

class LocalAccounts implements Serializable{
    private static volatile LocalAccounts instance = null;
    private ArrayList<UserAccount> localAccounts;
    private ArrayList<UserAccount> storeAccounts;
    private int currentUser = 0;
    private int familySize = 0;

    private LocalAccounts(){
        // TODO RETRIEVE THE SERIALIZED ACCOUNTS
        localAccounts = new ArrayList<>(6);
    }

    // Adds the account to the local accounts.
    protected void addAccount(UserAccount account){
        if (account == null)
            return;

        if (account != null) {
            // Check if account already exists.
            if (localAccounts != null) {
                for (int i = 0; i < familySize; i++) {
                    if (localAccounts.get(i).getEmail() == account.getEmail()) {
                        // Update account.
                        localAccounts.remove(i);
                        localAccounts.add(account);
                        return;
                    }
                }
            }

            // New family member.
            localAccounts.add(familySize, account);
            familySize++;
            updateFamilyIDs();
        }
    }

    protected void removeAccountAt(int index){
        if (index == currentUser)
            currentUser = 0;
        localAccounts.remove(index);
        familySize--;
    }

    // Updates account.
    protected void updateAccount(UserAccount account){
        // Update account.
        for (int i = 0; i < familySize; i++){
            if (localAccounts.get(i).getEmail() == account.getEmail()){
                // Update account.
                localAccounts.remove(i);
                localAccounts.add(i, new UserAccount(account));
            }
        }
    }

    protected void updateFamilyIDs(){
        // Store all the user IDs.
        ArrayList<String> userIDs = new ArrayList<>(familySize);
        for (int i = 0; i < familySize; i++){
            userIDs.add(localAccounts.get(i).getUserID());
        }

        // Add
        for (int i = 0; i < familySize; i++) {
            // Remove the current id as it's the same user's id.
            String id = userIDs.remove(i);
            // Add everyone else's id.
            localAccounts.get(i).setFamilyIDs(userIDs);
            // Add this id back.
            userIDs.add(i, id);
        }
    }

    // Returns the user account at position i.
    protected UserAccount getAccountAt(int i){
        return localAccounts.get(i);
    }

    // Getters //

    // Sets current user.
    protected void setCurrentUser(UserAccount user){
        for (int i = 0; i < familySize; i++){
            if (localAccounts.get(i).getUserID().equals(user.getUserID())){
                // Account found.
                localAccounts.remove(i);
                localAccounts.add(i, new UserAccount(user));
                currentUser = i;
                return;
            }
        }
        // Account not in family.
        localAccounts.add(user);
        currentUser = familySize++;
    }

    // Returns the user that's logged in.
    protected UserAccount getCurrentUser(){
        return localAccounts.get(currentUser);
    }

    // Returns an arraylist of all local accounts.
    protected ArrayList<UserAccount> getLocalAccounts(){
        return localAccounts;
    }

    protected void deleteAccounts(){
        for (int i = 0; i < familySize;) {
            localAccounts.remove(--familySize);
        }
        currentUser = 0;
    }

    // Returns all family nicknames.
    protected String[] getLocalAccountNicknames(){
        String[] names = new String[familySize];

        for (int i = 0; i < familySize; i++){
            names[i] = localAccounts.get(i).getFirstName();
        }
        return names;
    }

    // Returns all child accounts in an arraylist.
    protected ArrayList getChildAccounts(){
        ArrayList<UserAccount> children = new ArrayList<>();
        for (int i = 0; i < familySize; i++){
            if (!localAccounts.get(i).isParent())
                children.add(localAccounts.get(i));
        }
        return children;
    }

    // Return family size.
    protected int getFamilySize(){
        return familySize;
    }

    // Get singleton instance.
    protected static LocalAccounts getInstance(){
        if (instance == null){
            synchronized (LocalAccounts.class){
                if (instance == null){
                    instance = new LocalAccounts();
                }
            }
        }
        return instance;
    }

    // Local account storage //

    protected void offlineMode(){
        deserializeAccounts();
        localAccounts = new ArrayList<>(storeAccounts);
    }

    // Returns index if email belongs to a local account for offline authentication.
    protected int isLocalAccount(String email){
        for (int i = 0; i < familySize; i++) {
            UserAccount user = getAccountAt(i);
            if (user.getEmail().equals(email)){
                return i;
            }
        }
        return -1;
    }

    // Store accounts.
    protected void serializeAccounts() {
        try {
            FileOutputStream fos = new FileOutputStream("family.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(localAccounts);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // Retrieve accounts.
    private void deserializeAccounts() {
        try {
            FileInputStream fis = new FileInputStream("family.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeAccounts = (ArrayList<UserAccount>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
    }
}