package com.uottawa.linkedpizza.householdchoremanager;


import android.content.Intent;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Waleed on 11/19/17.
 */

public class TaskAdapter extends ArrayAdapter<Task> {

    private final Context context;
    private LocalAccounts localAccounts;
    private ArrayList<Task> tasks;

    protected int[] imageIds = {
            R.drawable.nevin,
            R.drawable.nick,
            R.drawable.harjote,
            R.drawable.waleed,
            R.drawable.david,};


    public TaskAdapter(Context context, ArrayList<Task> tasksList) {
        super(context, R.layout.task_layout, tasksList);
        this.context = context;
        localAccounts = localAccounts.getInstance();
        tasks = new ArrayList<>(tasksList);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //ArrayList<Task> tasks = localAccounts.getCurrentUser().getTasks();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.task_layout, parent, false);

        TextView txtTaskName = (TextView) rowView.findViewById(R.id.txtTaskName);
        TextView txtTaskNotes = (TextView) rowView.findViewById(R.id.txtTaskNotes);
        ImageView userImage = (ImageView) rowView.findViewById(R.id.imgUser);
        ImageView notif = rowView.findViewById(R.id.notif);

        //assigning random images to tasks
        Random generator = new Random();
        int randomImageId = imageIds[generator.nextInt(imageIds.length)];

        switch (position) {
            default: userImage.setImageResource(randomImageId);
                break;
        }

        if (tasks != null) {
            if (position < tasks.size()) {
                Task task = tasks.get(position);
                txtTaskName.setText(task.getTaskName());

                if (task.getStatus().equals("verify")) {
                    notif.setVisibility(View.VISIBLE);
                }
                else {
                    notif.setVisibility(View.GONE);
                }


                // Setting task notes in adapter according to priority
                if (task.getNote() != null) {
                    txtTaskNotes.setText(task.getNote());
                }

                if (task.getDeadline() != null) {
                    txtTaskNotes.setText("Deadline: " + task.getDeadline().toEnglishString());
                }

                if (task.getDueTime() != null) {
                    txtTaskNotes.setText("Deadline: " + task.getDueTime().toString());
                }

                if (task.getDeadline() != null && task.getDueTime() != null) {
                    txtTaskNotes.setText("Deadline: " + task.getDeadline().toEnglishString() + " at " +
                    task.getDueTime().toString());
                }

                if (task.getShoppingList() != null && task.getShoppingList().getItemCount() != 0) {

                    int count = task.getShoppingList().getItemCount();

                    if (count == 1) {
                        txtTaskNotes.setText("1 item in shopping list");
                    } else {
                        txtTaskNotes.setText(count + " items in shopping list");
                    }
                }

            }
        }

        return rowView;
    }
}
