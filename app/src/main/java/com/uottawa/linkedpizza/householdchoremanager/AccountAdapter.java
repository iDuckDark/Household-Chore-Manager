package com.uottawa.linkedpizza.householdchoremanager;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by david on 2017-11-22.
 */

public class AccountAdapter extends ArrayAdapter<UserAccount> {

    private final Context context;
    private final UserAccount[] accounts;

    protected int[] imageIds = {
            R.drawable.nevin,
            R.drawable.nick,
            R.drawable.harjote,
            R.drawable.waleed,
            R.drawable.david,};


    public AccountAdapter(Context context, UserAccount[] accountsList) {
        super(context, R.layout.account_layout, accountsList);
        this.context = context;
        this.accounts = accountsList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.account_layout, parent, false);

        TextView txtName = (TextView) rowView.findViewById(R.id.txtName);
        TextView txtAllocatedTasks = (TextView) rowView.findViewById(R.id.txtAllocatedTasks);
        TextView txtNextTask = (TextView) rowView.findViewById(R.id.txtNextTask);
        ImageView actionImage = (ImageView) rowView.findViewById(R.id.imgChat);
        ImageView userImage = (ImageView) rowView.findViewById(R.id.imgUser);

        txtName.setText(accounts[position].getFirstName());

        //FOR UI DEMO; DELETE LATER
        txtAllocatedTasks.setText(accounts[position].getFirstName());
        txtNextTask.setText(accounts[position].getLastName());
        actionImage.setImageResource(R.drawable.stat_notify_chat_black);

        if (txtName.getText().equals("Me"))
            actionImage.setImageResource(R.drawable.toolbar_dots);

        final String name = txtName.getText().toString();
        // setting random icons to other users
        Random generator = new Random();
        int randomImageId = imageIds[generator.nextInt(imageIds.length)];

        switch (name) {
            default: userImage.setImageResource(randomImageId);
                break;
        }
        //END DELETE LATER

        ((ImageView) rowView.findViewById(R.id.imgChat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Feature not implemented yet", Toast.LENGTH_LONG).show();
            }
        });

        return rowView;
    }

}