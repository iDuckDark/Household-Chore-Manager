package com.uottawa.linkedpizza.householdchoremanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Nick-JR on 11/20/2017.
 */

public class Shopping implements Serializable {

    private ArrayList<Item> items;
    private String listName;
    private String id;
    private Task associatedTask;

    public Shopping(String name) {
        this.listName = name;
        items = new ArrayList<Item>();
    }

    protected void addItem(Item item){

        if (contains(item))
            return;

        item.setIsShopping(true);
        items.add(item);
    }

    protected void modifyItem(Item item) {
        if (!contains(item))
            return;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(item.getName())) {
                items.get(i).setIsShopping(item.getIsShopping());
                items.get(i).setHasBeenShopped(item.getHasBeenShopped());
            }
        }
    }

    protected Item[] getItems() {
        if (items == null || items.size() == 0) {
            return null;
        }

        Item[] itemArray = new Item[items.size()];
        ListIterator<Item> iterator = items.listIterator();

        for (int i = 0; iterator.hasNext(); i++) {
            itemArray[i] = iterator.next();
        }

        return itemArray;
    }

    protected boolean contains(Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().toLowerCase().equals(item.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    protected void setAssociatedTask(Task task) {
        associatedTask = task;
    }

    protected Task getAssociatedTask() {
        return associatedTask;
    }

    protected void clearAssociatedTask() {
        associatedTask = null;
    }

    protected String getListName() {
        return this.listName;
    }

    protected void setListName(String name) {
        this.listName = name;
    }

    protected void setItemShopped(String itemName, boolean shopped) {

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(itemName)) {
                items.get(i).setHasBeenShopped(shopped);
                break;
            }
        }
    }

    protected int getItemCount() {
        return items.size();
    }

    protected void setId(String id){
        this.id = id;
    }

    protected String getId(){
        return id;
    }
}
