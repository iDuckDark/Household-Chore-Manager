package com.uottawa.linkedpizza.householdchoremanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

public class ResourceActivity extends AppCompatActivity {



    LocalAccounts localAccounts;
    private ListView Resources;
    private ItemAdapter adapter;
    ArrayList<Task> TotalTaskswithitems=new ArrayList<Task>();
    ArrayList<Task> TotalTasks=new ArrayList<Task>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        localAccounts=localAccounts.getInstance();
        TotalTasks=localAccounts.getCurrentUser().getTasks();

        for(int i=0;i<TotalTasks.size();i++){
            if(TotalTasks.get(i)!=null && TotalTasks.get(i).getRequiredItems() != null && TotalTasks.get(i).getRequiredItems().length!=0){
                TotalTaskswithitems.add(TotalTasks.get(i));
            }
        }


        Resources  = (ListView) findViewById(R.id.ResourceList);
        adapter = new ItemAdapter(this,TotalTaskswithitems);
        Resources.setAdapter(adapter);
    }









}
