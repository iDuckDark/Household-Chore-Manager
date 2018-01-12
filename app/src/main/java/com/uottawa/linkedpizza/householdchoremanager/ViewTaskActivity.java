package com.uottawa.linkedpizza.householdchoremanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewTaskActivity extends AppCompatActivity implements Runnable {

    private static final String DEVELOPER_KEY = "AIzaSyAn2hKb-dwBT9jg3_L3autSMXDzmwapFHo";

    private LocalAccounts localAccounts;
    private Database db;
    private Task task;
    private ArrayList<String> videoIds = new ArrayList<>();

    private int index = -1;

    // UI Objects.
    private Button youtubeButton;
    private Button deleteButton;
    private ImageButton editButton;
    private Button completeButton;
    private TextView name;
    private TextView child;
    private TextView creator;
    private TextView noteView;
    private TextView dateView;
    private TextView timeView;
    private TextView statusString;
    private ListView lstItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_task_layout);
        localAccounts = localAccounts.getInstance();
        db = db.getInstance(null);
        videoIds.add("dQw4w9WgXcQ");

        // Retrieve task index from intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            index = extras.getInt("index");
        }

        // Search youtube.
        Thread ytWorker = new Thread(this);
        ytWorker.start();

        // Get UI.
        Button back = findViewById(R.id.back);
        youtubeButton = (Button) findViewById(R.id.how_to);
        deleteButton = (Button) findViewById(R.id.Delete_Task);
        completeButton = findViewById(R.id.verified);
        editButton = (ImageButton) findViewById(R.id.edit);
        name = findViewById(R.id.task_name);
        child = (TextView) findViewById(R.id.child_name);
        creator = findViewById(R.id.creator);
        noteView = findViewById(R.id.view_notes);
        timeView = findViewById(R.id.time_view);
        dateView = findViewById(R.id.date_view);
        statusString = findViewById(R.id.status_text);
        lstItems = findViewById(R.id.lstItems);

        // Set task. Error checking.
        if (index != -1 && localAccounts.getCurrentUser().getTask(index) != null) {
            task = localAccounts.getCurrentUser().getTask(index);
        } else {
            //Toast.makeText(ViewTaskActivity.this, "Task is null", Toast.LENGTH_LONG);
            Intent main = new Intent(ViewTaskActivity.this, MainActivity.class);
            startActivity(main);
            finish();
            return;
        }

        // Set UI.
        // Change button view and functionality for a child
        if (!localAccounts.getCurrentUser().isParent()) {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);

        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskOnClick();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ytActivity = new Intent(getApplicationContext(), MainActivity.class);
                completeTask();
                startActivity(ytActivity);
                finish();
            }
        });

        name.setText(task.getTaskName());
        statusString.setText(task.getStatus());

        String assigned = null;
        if (task.getAssignedUser() != null) {
             assigned = task.getAssignedUser().getNickname() + " (" +
                    task.getAssignedUser().getFirstName() + ")";
        }
        else {
            assigned = "No child assigned.";
        }
        child.setText(assigned);

        String by = null;
        if (task.getCreator() != null) {
            by = task.getCreator().getNickname() + " (" +
                    task.getCreator().getFirstName() + ")";
        }
        else {
            by = "Creator not set.";
        }
        creator.setText(by);

        youtubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ytActivity = new Intent(getApplicationContext(), YoutubeAcitvity.class);
                ytActivity.putExtra("videoID", videoIds);
                startActivity(ytActivity);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent deletePopUpActivity = new Intent(getApplicationContext(), DeletePopUp.class);
                //startActivity(deletePopUpActivity);
                dialog();
            }
        });
        //added
        if (task.getNote() != null)
            noteView.setText(task.getNote());
        else
            noteView.setText("Note not set");
        if (task.getDueTime() != null)
            timeView.setText(task.getDueTime().toString());
        else
            timeView.setText("Time not set");
        if (task.getDeadline() != null)
            dateView.setText(task.getDeadline().toEnglishString());
        else
            dateView.setText("Deadline not set");

        if (task.getRequiredItems() != null && task.getRequiredItems().length != 0) {
            ListView list = (ListView) findViewById(R.id.lstItems);
            TextView noItems = (TextView) findViewById(R.id.view_items);

            list.setVisibility(View.VISIBLE);
            noItems.setVisibility(View.INVISIBLE);

            Item[] items = task.getRequiredItems();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

            for (int i = 0; i < items.length; i++) {
                if (items[i].getIsShopping()) {
                    adapter.add(items[i].getName() + " (Shopping)");
                } else {
                    adapter.add(items[i].getName());
                }
            }

            list.setAdapter(adapter);

        } else {
            ListView list = (ListView) findViewById(R.id.lstItems);
            TextView noItems = (TextView) findViewById(R.id.view_items);

            list.setVisibility(View.INVISIBLE);
            noItems.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void run(){
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            // Search youtube for video based on chore.
            searchVideo();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void searchVideo() throws Exception{
        StringBuilder result = new StringBuilder();
        URL url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&q=How to " + task.getTaskName() +"&type=video&key=" + DEVELOPER_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        JSONObject json = new JSONObject(result.toString());

        JSONArray aj = json.getJSONArray("items");
        JSONArray arr = null;

        String[] word = null;
        String s = null;

        ArrayList<String> parseIds = new ArrayList<>(5);

        for (int i = 0; i < aj.length(); i++){
            s = aj.getJSONObject(i).toString();
            word = s.split(":");
            boolean found = false;
            for (int j= 0; j < word.length; j++){
                if (word[j].contains("videoId")){
                    // The next index of aj contains the videoId.
                    s = word[j+1];
                    StringBuilder sb = new StringBuilder(s);
                    sb.deleteCharAt(0);
                    parseIds.add(sb.substring(0, 11));
                    break;
                }
            }
        }
        videoIds = new ArrayList<>(parseIds);
    }


    public void editTaskOnClick() {
        Intent editTask = new Intent(getApplicationContext(), NewTaskActivity.class);
        editTask.putExtra("editTask", true);
        editTask.putExtra("taskIndex",index);
        startActivity(editTask);
    }
    //reference Nevin
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    public void dialog(){
        new AlertDialog.Builder(this)
                .setTitle("EXIT")
                .setMessage("Do you really want to delete this task?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //code for yes button

                        deleteTask();
                        onBackPressed();
                    }
                }).create().show();
    }


    @Override
    public void onBackPressed() {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();
    }

    public void deleteTask(){
        // Child can't delete a task.
        if (!localAccounts.getCurrentUser().isParent())
            return;

        try {
            // Update database.
            db.deleteTask(task);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void completeTask(){
        try {
            // Update database.
            if (localAccounts.getCurrentUser().isParent()) {
                // Set task as completed.
                task.setStatus("completed");

                localAccounts.getCurrentUser().taskCompleted(task);
               // ArrayList completedTasks = new ArrayList(localAccounts.getCurrentUser().getCompletedTasks());
                ArrayList tasks = new ArrayList(localAccounts.getCurrentUser().getTasks());
                ArrayList taskIDs = new ArrayList(localAccounts.getCurrentUser().getTaskIDs());

                // Update users.
                for (int i = 0; i < localAccounts.getFamilySize(); i++){
                    localAccounts.getAccountAt(i).setTasks(tasks);
                    localAccounts.getAccountAt(i).setTaskIDs(taskIDs);
                    //localAccounts.getAccountAt(i).setCompletedTasks(completedTasks);
                }

                // Assign points to assigned child
                for (int i = 0; i < localAccounts.getFamilySize(); i++) {
                    if (localAccounts.getAccountAt(i).getUserID() == task.getAssignedUser().getUserID()) {
                        localAccounts.getAccountAt(i).addPoints(100);
                        localAccounts.getAccountAt(i).levelUp();
                        break;
                    }
                }

                // Update database.
                db.taskCompleted(task);
                Toast.makeText(ViewTaskActivity.this, "100 points awarded to " + task.getAssignedUser().getNickname(), Toast.LENGTH_LONG).show();
            }
            else {
                if (task.getAssignedUser().getUserID() != localAccounts.getCurrentUser().getUserID()){
                    Toast.makeText(ViewTaskActivity.this,"This is not my task.", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;
                }
                // Remove task from all users.
                for (int i = 0; i < localAccounts.getFamilySize(); i++){
                    localAccounts.getAccountAt(i).removeTask(task);
                }
                task.setStatus("verify");
                db.setTaskStatus(task);

                Toast.makeText(ViewTaskActivity.this, "Task complete", Toast.LENGTH_LONG).show();
            }


        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}