package com.uottawa.linkedpizza.householdchoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by iDarkDuck on 11/26/17.
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }

    @Override
    public void onBackPressed() {
        //Toast.makeText(AboutActivity.this, " test GOING BACK in AboutActivityClass ", Toast.LENGTH_LONG).show();
        finish();
        //super.onBackPressed();
    }
}
