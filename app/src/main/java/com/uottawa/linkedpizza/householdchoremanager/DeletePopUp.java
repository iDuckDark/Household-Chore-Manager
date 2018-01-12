package com.uottawa.linkedpizza.householdchoremanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

/**
 * Created by Harjote on 23/11/2017.
 */

public class DeletePopUp extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_task_popup);
        //DisplayMetrics display = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(display);
        //int width = display.widthPixels;
        //int height = display.heightPixels;
        //getWindow().setLayout((int)(width*0.75), (int)(height*0.4));
    }
}
