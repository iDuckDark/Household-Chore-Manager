package com.uottawa.linkedpizza.householdchoremanager;

import java.io.Serializable;

public class Item implements Serializable {

    private String name;
    private String id;
    private boolean isShopping;
    private boolean hasBeenShopped;

    public Item(){}

    public Item(String name) {
        this.name = name;
        this.isShopping = false;
        this.hasBeenShopped = false;
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setIsShopping(boolean isShopping) {
        this.isShopping = isShopping;
    }

    protected boolean getIsShopping() {
        return isShopping;
    }

    protected void setHasBeenShopped(boolean hasBeenShopped) {

        if (!isShopping) {
            this.hasBeenShopped = false;
            return;
        }

        this.hasBeenShopped = hasBeenShopped;
    }

    protected boolean getHasBeenShopped() {

        if (!isShopping) {
            return false;
        }

        return hasBeenShopped;
    }

    protected void setId(String id){
        this.id = id;
    }

    protected String getId(){
        return id;
    }
}
