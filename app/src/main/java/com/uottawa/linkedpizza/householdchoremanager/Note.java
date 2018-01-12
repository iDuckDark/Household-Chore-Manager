package com.uottawa.linkedpizza.householdchoremanager;

/**
 * Created by Nick-JR on 11/20/2017.
 */

public class Note {
    private String text;
    Note(){
        text = null;
    }
    Note(String s){
        text = s;
    }
    // Setter for text.
    public void setText(String s){
        text = s;
    }
    // Getter for text.
    public String getText(){
        return text;
    }
}
