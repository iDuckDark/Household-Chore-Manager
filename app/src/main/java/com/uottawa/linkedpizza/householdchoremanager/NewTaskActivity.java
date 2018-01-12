package com.uottawa.linkedpizza.householdchoremanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK;
import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;

/**
 * Created by iDarkDuck on 11/26/17.
 */

public class NewTaskActivity extends AppCompatActivity {

    private Database db;
    private LocalAccounts localAccounts;

    Button cancel_createTask;
    Button done_createTask;
    Spinner dropdown;

    Boolean editTask;

    int taskIndex = -1;

    Switch repeatTask;
    boolean isRepeatTask;
    Switch rewardGiven;
    boolean isRewardGiven;

    //to store previous Task

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task_layout);

        db = db.getInstance(null);
        localAccounts = localAccounts.getInstance();

        if (!localAccounts.getCurrentUser().isParent())
            return;

        cancel_createTask = (Button)findViewById(R.id.create_cancel_button);
        done_createTask = (Button)findViewById(R.id.create_done_button);

        cancel_createTask.setOnClickListener(onClickListener);
        done_createTask.setOnClickListener(onClickListener);

        // Get the spinner from the xml.
        dropdown = (Spinner)findViewById(R.id.select_child);

        //=============== REMOVED TEMPORARY =================
        //SWITCHES
//        repeatTask = (Switch)findViewById(R.id.isrepeatedswitch);
//        // check current state of a Switch (true or false).
//        isRepeatTask = repeatTask.isChecked();
//        repeatTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(NewTaskActivity.this, "TOGGLEEEEEEEEE is repeated", Toast.LENGTH_LONG).show();
//                repeatTask.setChecked(true);
//                isRepeatTask=true;
//
//            }
//        });
//        rewardGiven = (Switch)findViewById(R.id.isrewardgivenswitch);
//        // check current state of a Switch (true or false).
//        isRewardGiven = rewardGiven.isChecked();
//        rewardGiven.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(NewTaskActivity.this, "TOGGLEEEEEEEEE reward is given", Toast.LENGTH_LONG).show();
//                rewardGiven.setChecked(true);
//                isRewardGiven=true;
//            }
//        });

        // Retrieve task index from intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskIndex = extras.getInt("index");
        }

        // Fill in form.
        if (taskIndex > 0) {
            Task task = localAccounts.getCurrentUser().getTask(taskIndex);
            task.setIsRepeated(isRepeatTask);
        }


        //============================END OF SWITCHES========================

        //for view -> edit task
        editTask = getIntent().getExtras().getBoolean("editTask");
        //Toast.makeText(NewTaskActivity.this, editTask.toString(), Toast.LENGTH_LONG).show();

        final Spinner items = (Spinner) findViewById(R.id.spnItems);
        items.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item));

        Button btnDelete = (Button) findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item = items.getSelectedItem().toString();
                dialog(item, true);
            }
        });

        if(editTask){
            TextView title = (TextView)findViewById(R.id.txtTitle);
            title.setText("Editing a Chore Assignment");

            EditText taskName = (EditText)findViewById(R.id.tasknamefield);
            EditText taskDate = (EditText)findViewById(R.id.datefield);
            //EditText taskItems = (EditText)findViewById(R.id.itemsfield);
            EditText taskTime = (EditText)findViewById(R.id.timefield);
            EditText taskNotes = (EditText)findViewById(R.id.notefield);
            EditText taskReward= (EditText)findViewById(R.id.rewardfield); // added dec 2nd
            taskIndex=getIntent().getExtras().getInt("taskIndex");

            //============================REMOVE TEMPORARY========================
            //Boolean postponedTrue;
            //Switch repeatTask_edit = (Switch)findViewById(R.id.isrepeatedswitch);
            //Switch rewardGiven_edit = (Switch)findViewById(R.id.isrewardgivenswitch);

            Task task = localAccounts.getCurrentUser().getTask(taskIndex);
            taskName.setText(task.getTaskName());

            if (task.getDeadline() != null){
                taskDate.setText(task.getDeadline().toEnglishString());
                task.setIsPostponed(true);
            }
            if (task.getDueTime() != null) {
                taskTime.setText(task.getDueTime().toString());
                task.setIsPostponed(true);
            }

            Item[] itemsToAdd = task.getRequiredItems();

            if (itemsToAdd != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) items.getAdapter();
                for (int i = 0; i < itemsToAdd.length; i++) {
                    if (itemsToAdd[i].getIsShopping()) {
                        adapter.add(itemsToAdd[i].getName() + " (Shopping)");
                    } else {
                        adapter.add(itemsToAdd[i].getName());
                    }
                }
            }

            if (!items.getAdapter().isEmpty())
                btnDelete.setEnabled(true);

            if (task.getNote() != null) {
                taskNotes.setText(task.getNote());
            }
            if( task.getReward()!=null){
                taskReward.setText(task.getReward());
            }

            //=============== REMOVED =================
            //switches
//            Boolean yes =  task.isRepeated();  Boolean yes2 =  task.rewardISGiven();
//            String yesss = yes.toString() + "  repeated LINE 168 edit success ";
//            String yesss2 = yes.toString() + "  reward LINE 169 edit success ";
//            Toast.makeText(NewTaskActivity.this, yesss, Toast.LENGTH_LONG).show();
//            Toast.makeText(NewTaskActivity.this, yesss2, Toast.LENGTH_LONG).show();
//            repeatTask_edit.setChecked(task.isRepeated());
//            // we need to know if there is reward or not
//            //boolean rewardedOrNot = task.getReward()!=null;
//            rewardGiven_edit.setChecked(task.rewardISGiven());

        }


        // Get children names.
        ArrayList<UserAccount> children = localAccounts.getChildAccounts();
        if (children != null) {
            String[] nicknames = new String[children.size()];

            for (int i = 0; i < children.size(); i++) {
                nicknames[i] = children.get(i).getNickname();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nicknames);
            //set the spinners adapter to the previously created one.
            dropdown.setAdapter(adapter);
        }

        // Date On-Click listener and selection box
        final EditText txtDate = (EditText) findViewById(R.id.datefield);

        txtDate.setOnClickListener(new View.OnClickListener() {
            Date date = new Date(1980, 1, 1);
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                findViewById(R.id.txtTitle).requestFocus();
                DatePickerDialog a= new DatePickerDialog (getApplicationContext(),THEME_DEVICE_DEFAULT_DARK);
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {


                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        if (year != date.getYear() || month != date.getMonth() || day != date.getDay())
                            date = new Date(year, month + 1, day);

                        txtDate.setText(date.toEnglishString());
                    }
                };

                Calendar today = Calendar.getInstance();

                DatePickerDialog datePicker;

                if (date.getYear() == 1980) {
                    datePicker = new DatePickerDialog(NewTaskActivity.this, listener,
                            today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
                } else {
                    datePicker = new DatePickerDialog(NewTaskActivity.this, listener,
                            date.getYear(), date.getMonth() - 1, date.getDay());
                }

                // modified from https://stackoverflow.com/questions/20970963/how-to-disable-future-dates-in-android-date-picker
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis());

                datePicker.show();
            }
        });

        // Time On-Click listener and selection box
        final EditText txtTime = (EditText) findViewById(R.id.timefield);

        txtTime.setOnClickListener(new View.OnClickListener() {

            Time time = new Time(0, 0);

            @Override
            public void onClick(View view) {

                findViewById(R.id.txtTitle).requestFocus();

                TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        if (hour != time.getHour() || minute != time.getMinute())
                            time = new Time(hour, minute);

                        txtTime.setText(time.toString());

                    }
                };

                TimePickerDialog timePicker = new TimePickerDialog(NewTaskActivity.this, listener,
                        time.getHour(), time.getMinute(), false);
                timePicker.show();
            }
        });
        
    }
    //reference Nevin
    //https://stackoverflow.com/questions/3733858/android-onclick-method
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            EditText taskName = (EditText) findViewById(R.id.tasknamefield);
            EditText taskDate = (EditText) findViewById(R.id.datefield);
            EditText taskTime = (EditText) findViewById(R.id.timefield);
            EditText taskNotes = (EditText) findViewById(R.id.notefield);
            EditText taskReward = (EditText) findViewById(R.id.rewardfield);
            Spinner items = (Spinner) findViewById(R.id.spnItems);

            //=============== REMOVED =================
            Switch repeatedSwitch = (Switch) findViewById(R.id.isrepeatedswitch);
            Switch rewardSwitch = (Switch) findViewById(R.id.isrewardgivenswitch);

            switch(v.getId()){
                case R.id.create_cancel_button:
                    dialog();
                    break;
                case R.id.create_done_button:

                    boolean allRequiredFields = true;

                    taskName.getBackground().clearColorFilter();

                    Task task = null;

                    // Check required fields

                    if (taskName.getText().toString().equals("")) {
                        allRequiredFields = false;
                        taskName.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    }

                    // TODO: Check that a user is assigned

                    // Display message if not all required fields are entered
                    if (!allRequiredFields) {
                        findViewById(R.id.txtTitle).requestFocus();
                        Toast.makeText(NewTaskActivity.this, "Required fields are empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Otherwise, create the new task
                    if (!taskNotes.getText().toString().equals("")) {
                        task = new Task(taskName.getText().toString(), taskNotes.getText().toString());
                    } else {
                        task = new Task(taskName.getText().toString());
                    }
                    //REWARD STRING
                    if (!taskReward.getText().toString().equals("")) {
                        task.setReward(taskReward.getText().toString());
                    }
                    // Add task attributes

                    // Deadline date
                    if (!taskDate.getText().toString().equals("")) {
                        task.setDeadline(new Date(taskDate.getText().toString()));
                    }
                    // Deadline time
                    if (!taskTime.getText().toString().equals("")) {
                        task.setDueTime(new Time(taskTime.getText().toString()));
                    }
                    //=============== REMOVED =================
//                    Boolean yes=repeatedSwitch.isChecked();
//                    String result = yes.toString() + " LINE318 ";
//                    Toast.makeText(NewTaskActivity.this, result, Toast.LENGTH_LONG).show();
//                    task.setIsRepeated(repeatedSwitch.isChecked());
//                    task.setRewardISGiven(rewardSwitch.isChecked());

                    ArrayList<Item> itemsBeingAdded = new ArrayList<Item>();
                    Shopping newList = null;
                    // Get items
                    if (items.getCount() != 0) {
                        task.clearRequiredItems();
                        task.clearShoppingList();
                        for (int i = 0; i < items.getCount(); i++) {
                            String tmp = items.getItemAtPosition(i).toString();

                            if (tmp.contains("(Shopping)")) {
                                tmp = tmp.substring(0, tmp.indexOf("(Shopping)"));
                                tmp = tmp.trim();
                                Item toAdd = new Item(tmp);
                                toAdd.setIsShopping(true);
                                itemsBeingAdded.add(toAdd);
                                task.addRequiredItem(toAdd);

                                if (newList == null) {
                                    newList = new Shopping(task.getTaskName());
                                }

                                newList.addItem(toAdd);
                            } else {
                                tmp = tmp.trim();
                                Item toAdd = new Item(tmp);
                                itemsBeingAdded.add(toAdd);
                                task.addRequiredItem(toAdd);
                            }
                        }
                    }



                    ArrayList<Task> currentTasks = localAccounts.getCurrentUser().getTasks();
                    ArrayList<Task> usingItem = new ArrayList<Task>();

                    /*
                    for (int i = 0; i < itemsBeingAdded.size(); i++) {
                        Item item = itemsBeingAdded.get(i);
                        usingItem.add(task);
                        // start at 1 since we don't want to add the new task again
                        for (int j = 1; j < currentTasks.size(); j++) {
                            Task toCompare = currentTasks.get(j);
                            if (task.requiresItem(item)) {
                                usingItem.add(toCompare);
                            }
                        }
                        db.writeItem(item, usingItem);
                        usingItem.clear();
                    }
                    */

                    // we still have a lot of work to do in this method, so let gc free memory early on
                    usingItem = null;
                    currentTasks = null;

                    // Add required items to task.
                    for (Item item : itemsBeingAdded){
                        task.addRequiredItem(item);
                    }

                    // Add shopping list if there is one.
                    if (newList != null) {
                        task.setShoppingList(newList);
                        // Write shopping list.
                        db.writeShoppingList(newList);
                    }

                    // Add task to assigned child.
                    String child = dropdown.getSelectedItem().toString();
                    System.out.println("Child: " + child);

                    // Search for selected child account.
                    ArrayList<UserAccount> family = localAccounts.getLocalAccounts();
                    //added
                    //int indexAssignedUser=0;
                    for (int i = 0; i < family.size(); i++){
                        if (family.get(i).getNickname().equals(child)){
                            // Selected child found.
                            task.setAssignedUser(localAccounts.getAccountAt(i)); //indexAssignedUser=i;
                            task.setCreator(localAccounts.getCurrentUser());
                            break;
                        }
                    }

                    if (!editTask) {
                        // Add task to everyone.
                        for (int i = 0; i < family.size(); i++) {
                            localAccounts.getAccountAt(i).addTask(task);
                        }
                    }

                    // ============================ VIEW MY TASK ONLY ==============================
                    //getting user id of the child
                    //String currentID = localAccounts.getCurrentUser().getUserID();
                    //UserAccount assignedAccount = localAccounts.getAccountAt(indexAssignedUser);
                    //if(assignedAccount.getUserID()==currentID){
                    //        assignedAccount.addMyTask(task);
                    //}
                    // ============================  END VIEW MY TASK ONLY ==============================
                    //if(localAccounts.getCurrentUser().getInstance())

                    task.setID(localAccounts.getCurrentUser().getTask(taskIndex).getID());
                    System.out.println("new tsk" + task.getID());

                    // Update database.
                    if (editTask)
                        db.editTask(task);
                    else
                        db.writeTask(task);


                    Intent createdTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(createdTaskIntent);
                    break;
            }

        }
    };

    @Override
    public void onBackPressed() {
        if (editTask == null)
            editTask = false;

        if(editTask){
            Intent mainActivity = new Intent(getApplicationContext(), ViewTaskActivity.class);
            startActivity(mainActivity);
            finish();
        }
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();
    }


    public void addItemClick(View view) {

        EditText txtItem = (EditText) findViewById(R.id.txtNewItem);

        if (txtItem.getText().toString().equals("")) {
            return;
        }

        if (txtItem.getText().toString().contains("(Shopping)")) {
            Toast.makeText(NewTaskActivity.this, "Please use shopping checkbox for shopping items", Toast.LENGTH_LONG).show();
            return;
        }

        Spinner items = (Spinner) findViewById(R.id.spnItems);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) items.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {

            if (adapter.getItem(i).toString().trim().toLowerCase().equals(txtItem.getText().toString().trim().toLowerCase())) {
                Toast.makeText(NewTaskActivity.this, "Item already in list", Toast.LENGTH_LONG).show();
                return;
            }

            if (adapter.getItem(i).toString().contains("(Shopping)")) {
                String tmp = adapter.getItem(i).toString();
                tmp = tmp.substring(0, tmp.indexOf("(Shopping)")).trim().toLowerCase();
                if (tmp.equals(txtItem.getText().toString().trim().toLowerCase())) {
                    Toast.makeText(NewTaskActivity.this, "Item already in list", Toast.LENGTH_LONG).show();
                    return;
                }
            }

        }
        CheckBox shopping = (CheckBox) findViewById(R.id.chkShoppingItem);

        if (shopping.isChecked()) {
            adapter.add(txtItem.getText().toString() + " (Shopping)");
        } else {
            adapter.add(txtItem.getText().toString());
        }

        if (!((Button) findViewById(R.id.btnDelete)).isEnabled())
            ((Button) findViewById(R.id.btnDelete)).setEnabled(true);

        txtItem.setText("");
        shopping.setChecked(false);

    }
    //reference Nevin
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    private void dialog(){
        new AlertDialog.Builder(this)
                .setTitle("EXIT")
                .setMessage("Do you really want to exit?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //code for yes button
                        //Toast.makeText(NewTaskActivity.this, " DIALOG code shopping", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                }).create().show();
    }

    //reference Nevin
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    public void dialog(final String item, final boolean newTask){
        new AlertDialog.Builder(this)
                .setTitle("DELETE ITEM")
                .setMessage("Do you want to delete " + item + "?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (newTask)
                            onlyDeleteFromList(item);
                        else
                            deleteFromListAndDatabase(item);
                    }
                }).create().show();
    }

    private void onlyDeleteFromList(String item) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) ((Spinner) findViewById(R.id.spnItems)).getAdapter();
        adapter.remove(item);
        if (adapter.isEmpty())
            ((Button) findViewById(R.id.btnDelete)).setEnabled(false);
    }

    private void deleteFromListAndDatabase(String item) {
        onlyDeleteFromList(item);

        Item toDelete;

        if (item.contains("(Shopping)")) {
            item = item.substring(0, item.indexOf("(Shopping)")).trim().toLowerCase();
        }
    }
}
