package com.uottawa.linkedpizza.householdchoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PersonActivity extends AppCompatActivity {

    LocalAccounts localAccounts;
    private ListView tasks;
    private TaskAdapter adapter;
    Task[]Tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_page);

        int position = getIntent().getIntExtra("pos", 0);
        TextView PersonName = (TextView) findViewById(R.id.PersonName);
        TextView PersonPoints = (TextView) findViewById(R.id.pointsText);
        TextView PersonLevel = (TextView) findViewById(R.id.levelText);

        localAccounts = localAccounts.getInstance();
        PersonName.setText(localAccounts.getAccountAt(position).getFirstName()+" "+localAccounts.getAccountAt(position).getLastName());
        PersonPoints.setText("Points: "+localAccounts.getAccountAt(position).getPoints());
        PersonLevel.setText("Level: "+localAccounts.getAccountAt(position).getLevel());

        /*
        ArrayList<Task> allTasks = new ArrayList<>(localAccounts.getCurrentUser().getTasks());
        ArrayList<Task> assignedTasks = new ArrayList<>();

        for (int i = 0; i < allTasks.size(); i++) {
            System.out.print(allTasks.get(i).getTaskName() + " Assigned user: ");
            System.out.println(allTasks.get(i).getAssignedUser().getUserID());
            System.out.println(localAccounts.getAccountAt(position).getUserID());
            if (allTasks.get(i).getAssignedUser().getUserID().equals(localAccounts.getAccountAt(position).getUserID())) {
                System.out.println("added!");
                assignedTasks.add(allTasks.get(i));
            }
        }




        */
        //ArrayList assignedTasks = localAccounts.getAccountAt(position).getMyTasks();
        adapter = null;





        //ArrayList assignedTasks = localAccounts.getAccountAt(position).getMyTasks();


        ArrayList assignedTasks = localAccounts.getAccountAt(position).getMyTasks();

        if (assignedTasks != null){
            ListView tasks = (ListView) findViewById(R.id.TaskList);
            TaskAdapter adapter = new TaskAdapter(this, assignedTasks);
            tasks.setAdapter(adapter);

            if (assignedTasks.size() > 0){
                tasks = (ListView) findViewById(R.id.TaskList);
                adapter = new TaskAdapter(this,  assignedTasks);
                tasks.setAdapter(adapter);



                System.out.println(assignedTasks.get(0).toString());



                System.out.println(assignedTasks.get(0).toString());

                tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent viewTask = new Intent(getApplicationContext(), ViewTaskActivity.class);
                        viewTask.putExtra("index", i);
                        startActivity(viewTask);
                    }
                });
            }
        }
    }
}
