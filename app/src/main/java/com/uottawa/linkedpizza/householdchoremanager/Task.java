package com.uottawa.linkedpizza.householdchoremanager;

/**
 * Class: Task.java
 *
 * Stores information about a task such as name,
 * description, notes, required items, and deadline.
 */

import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class Task extends MainActivity{

    private String id;
    private String name;
    private String note;
    private String status;
    private String reward;

    private ArrayList<String> itemIDs;
    private LinkedList<Item> itemsRequired;
    private Shopping shoppingList;
    private UserAccount assignedUser;
    private UserAccount creator;

    private Date deadline;
    private Date repeatDuration;
    private Time dueTime;

    private boolean isCompleted;
    private boolean isRepeated;
    private boolean isPostponed;
    private boolean reqVerify;

    // Constructors

    public Task(String name) {
        this.name = name;
        this.status = "Not started";
        this.itemIDs = null;
        this.itemsRequired = null;
        this.reward = null;
        this.assignedUser = null;
        this.creator = null;
        this.deadline = null;
        this.repeatDuration = null;
        this.dueTime = null;
        this.isCompleted = false;
        this.isRepeated = false;
        this.isPostponed = false;
        this.reqVerify = false;
    }

    public Task(String name, String note) {
        this.name = name;
        this.note = note;
        this.status = "Not started";
        this.itemIDs = null;
        this.itemsRequired = null;
        this.reward = null;
        this.assignedUser = null;
        this.creator = null;
        this.deadline = null;
        this.repeatDuration = null;
        this.dueTime = null;
        this.isCompleted = false;
        this.isRepeated = false;
        this.isPostponed = false;
        this.reqVerify = false;
    }

    // Public methods

    protected void setID(String id) { this.id = id; }

    protected String getID() { return id; }

    protected void setItemIDs(ArrayList itemIDs) { this.itemIDs = itemIDs; }

    protected ArrayList<String> getItemIDs() { return itemIDs; }

    protected void addItemID(String itemID){
        if (itemIDs == null)
            itemIDs = new ArrayList<>();
        itemIDs.add(itemID);
    }

    public String getTaskName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void addRequiredItem(Item item) {

        if (itemsRequired == null) {
            itemsRequired = new LinkedList<Item>();
        }

        if (!itemsRequired.contains(item)) {
            itemsRequired.add(item);
        }

    }

    public void deleteRequiredItem(Item item) {

        if (itemsRequired != null) {
            if (itemsRequired.contains(item))
            {
                itemsRequired.remove(item);
            }
        }
    }

    public Item[] getRequiredItems() {

        if (itemsRequired == null || itemsRequired.size() == 0) {
            return null;
        }

        Item[] items = new Item[itemsRequired.size()];
        ListIterator<Item> iterator = itemsRequired.listIterator();

        for (int i = 0; iterator.hasNext(); i++) {
            items[i] = iterator.next();
        }

        return items;

    }

    public boolean requiresItem(Item item) {

        if (itemsRequired == null) {
            return false;
        }

        for (int i = 0; i < itemsRequired.size(); i++) {
            if (itemsRequired.get(i).getName().toLowerCase().equals(item.getName().toLowerCase())) {
                return true;
            }
        }

        return false;
    }
    public void clearRequiredItems() {
        itemsRequired = null;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public void setShoppingList(Shopping shoppingList) {
        this.shoppingList = shoppingList;
    }

    public Shopping getShoppingList() {
        return shoppingList;
    }

    public void clearShoppingList() {
        shoppingList = null;
    }

    public void removeReward() {
        this.reward = null;
    }

    public String getReward() {
        return reward;
    }

    public void requireVerification() {
        reqVerify = true;
    }

    public boolean getVerification() {
        return reqVerify;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Time getDueTime() {
        return dueTime;
    }

    public void setDueTime(Time time) {
        this.dueTime = time;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setIsRepeated(boolean isRepeated) {
        this.isRepeated = isRepeated;
    }

    public Date getRepeatDuration() {
        return repeatDuration;
    }

    public void setRepeatDuration(Date date) {
        this.repeatDuration = date;
    }

    public void postponeTo(Date date) {

        if (deadline != null) {
            if (date.greaterThan(deadline)) {
                isPostponed = true;
                deadline = date;
            }
        }
    }

    public boolean getIsPostponed() {
        return isPostponed;
    }

    public void setIsPostponed(boolean p) {
        isPostponed=p;
    }

    public void setAssignedUser(UserAccount user) {
        this.assignedUser = user;
    }

    public UserAccount getAssignedUser() {
        return assignedUser;
    }

    public void setCreator(UserAccount user) {
        creator = user;
    }

    public UserAccount getCreator() {
        return creator;
    }

    public void setStatus(String status) {
        if (status == "completed") {
            isCompleted = true;
            reqVerify = false;
            isPostponed = false;
        }
        else if (status == "verify") {
            reqVerify = true;
            isCompleted = false;
            isPostponed = false;
        }
        else if (status == "postponed") {
            isPostponed = true;
            isCompleted = false;
            reqVerify = false;
        }

        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}