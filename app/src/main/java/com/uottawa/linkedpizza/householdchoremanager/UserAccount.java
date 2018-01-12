package com.uottawa.linkedpizza.householdchoremanager;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.GenericTypeIndicator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Nick-JR on 11/20/2017.
 */

public class UserAccount implements Serializable{

    private ArrayList<Task> tasks = null; //family task
    private ArrayList<Task> myTasks = null; //my task only
    //private ArrayList<Task> completedTasks = null;
    private ArrayList<Shopping> shoppingLists = null;
    private ArrayList<String> taskIDs = null;
   // private ArrayList<String> completedTaskIDs = null;
    private ArrayList<String> familyIDs = null;
    private ArrayList<String> shoppingKeys = null;
    private String nickname;
    private String firstName;
    private String lastName;
    private String email;
    private String userID;
    private int points;
    private boolean parent;
    private int level;

    // Constructors.
    UserAccount(){}

    UserAccount(String email){
        taskIDs = new ArrayList<>();
        familyIDs = new ArrayList<>();
        tasks = new ArrayList<>();
        //completedTasks = new ArrayList<>();
        shoppingLists = new ArrayList<>();
        this.email = email;
    }

    UserAccount(UserAccount user){
        this.nickname = user.getNickname();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.userID = user.getUserID();
        this.familyIDs = user.getFamilyIDs();
        this.taskIDs = user.getTaskIDs();
        this.tasks = user.getTasks();
        this.shoppingLists = user.shoppingLists;
        this.shoppingKeys = user.shoppingKeys;
        this.points = user.points;
        this.parent = user.isParent();
        this.level = user.level;
    }

    // Adds a task.
    protected void addTask(Task task){
        if (task == null)
            return;
        if (tasks == null)
            tasks = new ArrayList<>();
        if (myTasks == null)
            myTasks = new ArrayList<>();

        // Check for duplicates.
        for (int i = 0; i < tasks.size(); i++){
            if (tasks.get(i).getID() == task.getID()) {
                tasks.remove(i);
                break;
            }
        }
        if (task.getAssignedUser() != null) {
            if (task.getAssignedUser().getUserID() == userID || task.getCreator().getUserID() == userID) {
                for (int i = 0; i < myTasks.size(); i++) {
                    if (myTasks.get(i).getID() == task.getID()) {
                        myTasks.remove(i);
                        break;
                    }
                }
            }
        }
        tasks.add(task);

        // Check if task is my task.
        if (task.getCreator().getUserID() == userID || task.getAssignedUser().getUserID() == userID)
            myTasks.add(task);
    }

    /*
    protected void addCompletedTask(Task task){
        if (task == null)
            return;
        if (completedTasks == null)
            completedTasks = new ArrayList<>();

        // Check for duplicates.
        for (int i = 0; i < completedTasks.size(); i++){
            if (completedTasks.get(i).getID() == task.getID()) {
                completedTasks.remove(i);
            }
        }
        if (task.getAssignedUser().getUserID() == userID || task.getCreator().getUserID() == userID) {
            for (int i = 0; i < myTasks.size(); i++) {
                if (myTasks.get(i).getID() == task.getID()) {
                    myTasks.remove(i);
                }
            }
        }
        tasks.add(task);

        // Check if task is my task.
        if (task.getCreator().getUserID() == userID)
            myTasks.add(task);
        if (task.getAssignedUser().getUserID() == userID)
            myTasks.add(task);
    }

    protected void addCompletedTaskID(String id){
        if (id == null)
            return;
        if (completedTaskIDs == null)
            completedTaskIDs = new ArrayList<>();

        completedTaskIDs.remove(id);
        completedTaskIDs.add(id);
    }
    */

    // Moves the task from current to completed.
    protected void taskCompleted(Task task){
    //    if (completedTasks == null)
    //        completedTasks = new ArrayList<>();
        // Find task.
        for (int i = 0; i < tasks.size(); i++){
            if (tasks.get(i).getID() == task.getID()) {
    //            if (completedTaskIDs == null)
    //                completedTaskIDs = new ArrayList<>();
                // Transfer task id.
    //            completedTaskIDs.add(task.getID());
                removeTaskID(task.getID());

                // Transfer task.
                removeTask(i);
    //            completedTasks.add(task);
            }
        }
        levelUp();
    }

    protected void addTaskID(String id){
        if (taskIDs == null)
            taskIDs = new ArrayList<>();
        taskIDs.add(id);
    }

    protected void removeTaskID(String id){
        if (id == null)
            return;
        if (taskIDs == null)
            taskIDs = new ArrayList<>();

        for (int i = 0; i < taskIDs.size(); i++){
            if (taskIDs.get(i).equals(id))
                taskIDs.remove(i);
        }
    }

    // Removes task at index i.
    protected Task removeTask(int i){
        return tasks.remove(i);
    }

    protected void removeTask(Task task){
        taskIDs.remove(task.getID());
        tasks.remove(task);

        // Check if task is my task.
        if (task.getCreator().getUserID() == userID)
            myTasks.remove(task);
        if (task.getAssignedUser().getUserID() == userID)
            myTasks.remove(task);
    }

    protected Task getTask(int i){
        if (i < tasks.size())
            return tasks.get(i);
        else
            return null;
    }

    protected void setTasks(ArrayList tasks){
        this.tasks = tasks;
    }

    /*

    protected void setCompletedTasks(ArrayList tasks){
        if (tasks == null)
            return;

        completedTasks = tasks;

        // Update IDs.
        completedTaskIDs = new ArrayList<>();
        for (Task task : completedTasks){
            completedTaskIDs.add(task.getID());
        }
    }
    */


    // ============================ VIEW MY TASK ONLY ==============================
    // Get users task at index i.
    /*
    protected Task getMyTasks(int i){
        if (i < myTasks.size())
            return myTasks.get(i);
        else
            return null;
    }
    */

    // Get this users tasks.
    protected ArrayList<Task> getMyTasks(){
        ArrayList<Task> myTasks1 = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++){
            if (tasks.get(i).getAssignedUser().getEmail().equals(email)){
                System.out.println("Add " + tasks.get(i).getAssignedUser().getEmail());
                myTasks1.add(tasks.get(i));
            }
        }

        return myTasks1;
    }
    // ============================ END OF VIEW MY TASK ONLY ==============================


    protected void addShoppingList(Shopping list){
        if (list == null)
            return;

        if (shoppingLists == null)
            shoppingLists = new ArrayList<>();

        // Check for duplicates.
        for (int i = 0; i < shoppingLists.size(); i++){
            if (shoppingLists.get(i).getId() == list.getId()) {
                shoppingLists.remove(i);
            }
        }

        shoppingLists.add(list);
    }

    protected void removeShoppingList(Shopping list){
        if (list == null)
            return;

        if (shoppingLists == null)
            return;

        // Remove shopping list.
        for (int i = 0; i < shoppingLists.size(); i++){
            if (shoppingLists.get(i).getId() == list.getId()) {
                shoppingLists.remove(i);
            }
        }
    }

    protected ArrayList getShoppingLists(){
        return shoppingLists;
    }

    protected void addShoppingKey(String key){
        if (key == null)
            return;

        if (shoppingKeys == null)
            shoppingKeys = new ArrayList<>();

        shoppingKeys.add(key);
    }

    protected void removeShoppingKey(String key) {
        if (key == null)
            return;

        if (shoppingKeys == null)
            return;

        shoppingKeys.remove(key);
    }

    protected void setShoppingKeys(ArrayList<String> keys){
        shoppingKeys = keys;
    }

    protected ArrayList<String> getShoppingKeys(){
        return shoppingKeys;
    }

    // Returns current tasks.
    protected ArrayList<Task> getTasks(){
        return tasks;
    }

    /*
    // Returns completed tasks.
    protected ArrayList<Task> getCompletedTasks(){
        return completedTasks;
    }

    protected ArrayList<String> getCompletedTaskIDs() { return completedTaskIDs; }
*/

    // Number of task.
    protected int getNumberOfTasks(){
        return tasks.size();
    }

    protected void addFamilyID(String id){
        familyIDs.add(id);
    }

    // Removes id from list.
    protected void removeFamilyID(String id){
        for (int i = 0; i < familyIDs.size(); i++){
            if (familyIDs.get(i).equals(id)){
                // Remove this id.
                familyIDs.remove(i);
                break;
            }
        }
    }

    protected int getNextLevel(){
        return (int) (Math.floor(level/5)*100)+1000;
    }

    // Increase points by value.
    protected void addPoints(int points){
        this.points += points;
    }

    // Decrease points by value.
    protected void removePoints(int points){
        this.points -= points;
    }

    // Setters and Getters.
    protected void setNickname(String nickname){
        this.nickname = nickname;
    }
    protected String getNickname(){
        return nickname;
    }

    protected void setFirstName(String firstName){
        this.firstName = firstName;
    }
    protected String getFirstName(){
        return firstName;
    }

    protected void setLastName(String lastName){
        this.lastName = lastName;
    }
    protected String getLastName(){
        return lastName;
    }

    protected void setEmail(String email){
        this.email = email;
    }
    protected String getEmail(){
        return email;
    }

    protected void setParent(boolean parent){
        this.parent = parent;
    }
    protected boolean isParent(){
        return parent;
    }

    protected void setFamilyIDs(ArrayList familyIDs){
        this.familyIDs = new ArrayList(familyIDs);
    }
    protected ArrayList<String> getFamilyIDs(){
        return familyIDs;
    }

    protected void setUserID(String id){
        userID = id;
    }
    protected String getUserID(){
        return userID;
    }

    protected void setTaskIDs(ArrayList<String> id){
        taskIDs = id;
    }
    protected ArrayList<String> getTaskIDs(){
        return taskIDs;
    }

    protected String getLevel(){return ""+level;}
    protected void setLevel(int level){this.level = level;}

    protected String getPoints(){return ""+points;}
    protected void setPoints(int points){this.points = points;}

    // level up method for each user
    protected void levelUp(){
        if (points > (getNextLevel())) {
            level++;
        }
    }
}