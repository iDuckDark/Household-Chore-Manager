package com.uottawa.linkedpizza.householdchoremanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by iDarkDuck on 11/26/17.
 */

public class SettingsActivity extends AppCompatActivity {
    private LocalAccounts localAccounts;


    private UserAccount CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        localAccounts = localAccounts.getInstance();


        UserAccount[] accounts = new UserAccount[localAccounts.getFamilySize()];

        CurrentUser = localAccounts.getCurrentUser();

        //converts arraylist to array
        for (int i = 0; i < localAccounts.getFamilySize(); i++) {

                accounts[i] = localAccounts.getAccountAt(i);

        }


        TextView PersonName = (TextView) findViewById(R.id.PersonSettingsName);
        TextView Group = (TextView) findViewById(R.id.Groupmembers);
        TextView ParentorChild = (TextView) findViewById(R.id.porc);
        Button Addchild = (Button) findViewById(R.id.addchild);
        Button ChangeName = (Button) findViewById(R.id.changename);
        //TextView familyID = (TextView) findViewById(R.id.family_id_text);


        Group.setText("Family Members");
        // TODO family code display on UI
        //familyID.setText("");

        Addchild.setOnClickListener(onClickListener);
        ChangeName.setOnClickListener(onClickListener);


        PersonName.setText(localAccounts.getCurrentUser().getNickname());

        if (CurrentUser.isParent()) {
            Addchild.setEnabled(true);


            ParentorChild.setText("Parent");
        } else if (!CurrentUser.isParent()) {
            Addchild.setEnabled(false);

            ParentorChild.setText("Child");

        }

        //Instance of the Accounter adapter to fill our list.
        if (localAccounts.getFamilySize() >= 1) {
            ListView people = (ListView) findViewById(R.id.SettingsPeople);
            AccountAdapter adapter = new AccountAdapter(this, accounts);
            people.setAdapter(adapter);


        }}
        //Reference Waleed
        ////https://stackoverflow.com/questions/3733858/android-onclick-method

        private View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                switch (v.getId()) {
                    case R.id.addchild:

                        Intent registrationActivity = new Intent(getApplicationContext(), RegistrationActivity.class);
                        registrationActivity.putExtra("state", false);
                        startActivity(registrationActivity);
                        break;


                    case R.id.changename:
                        popUpEditText();


                }
            }
        };


        @Override
        public void onBackPressed () {

            //Toast.makeText(SettingsActivity.this, " test GOING BACK in SettingActivityClass ", Toast.LENGTH_LONG).show();
            finish();
            //super.onBackPressed();
        }


        //Popup: Allows user to input desired name

    private void popUpEditText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your desired nickname");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("APPLY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                localAccounts.getCurrentUser().setNickname(input.getText().toString());
                finish();
                onUpdate();
                Toast.makeText(SettingsActivity.this, "Changes Applied", Toast.LENGTH_LONG).show();



            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    protected void onUpdate() {


        Intent mainActivity = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(mainActivity);
    }


}



