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
//NOT USED
public class DeleteTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_task_popup);
    }

    @Override
    public void onBackPressed() {
        //Toast.makeText(DeleteTaskActivity.this, " test GOING BACK in DeleteTaskActivityClass ", Toast.LENGTH_LONG).show();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
        finish();
        //super.onBackPressed();
    }
}
