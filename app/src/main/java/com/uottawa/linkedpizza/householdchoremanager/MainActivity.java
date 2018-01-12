package com.uottawa.linkedpizza.householdchoremanager;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LocalAccounts localAccounts;
    private Database db;
    private CalendarView simpleCalendarView;
    private TaskAdapter taskAdapter;
    private ShoppingAdapter shoppingAdapter;
    private ArrayList<Shopping> shoppingLists;
    private ListView shoppingList;

    FloatingActionButton createTaskButton;
    private Switch viewMyTaskToggle;
    private ImageView broom;
    private TextView targetText;
    private View target;
    private View targetChange;
    private int colour;

    protected static Task[] taskList;

    // Activity with result request codes
    // Add to these if we need more
    private final int CREATE_NEW_SHOPPING_LIST = 2;
    private final int ADD_NEW_TASK = 3;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        // Get database instance.

        db = db.getInstance(null);
        localAccounts = localAccounts.getInstance();

        // Check if user is signed in.
        if (localAccounts.getCurrentUser() == null)
            logout();

        db.setMainActivity(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /*
        FloatingActionButton createTaskButton = (FloatingActionButton) findViewById(R.id.btnFloatingAction);
        if(!localAccounts.getCurrentUser().isParent()){
            createTaskButton.setVisibility(View.GONE);
        }
        */

        // Initialize tabs
        TabHost mainHost = (TabHost) findViewById(R.id.mainTabHost);
        mainHost.setup();

        // Tab 1
        TabSpec tab1 = mainHost.newTabSpec("Shopping");
        tab1.setIndicator("Shopping");
        tab1.setContent(R.id.tab1);

        // Tab 2
        TabSpec tab2 = mainHost.newTabSpec("Tasks");
        tab2.setIndicator("Tasks");
        tab2.setContent(R.id.tab2);

        // Tab 3
        TabSpec tab3 = mainHost.newTabSpec("People");
        tab3.setIndicator("People");
        tab3.setContent(R.id.tab3);

        // Add tabs to TabHost
        mainHost.addTab(tab1);
        mainHost.addTab(tab2);
        mainHost.addTab(tab3);

        setNewItemInputEnabled(false);
        shoppingList = (ListView) findViewById(R.id.lstShopping);

        // Task adapter.
        taskAdapter = new TaskAdapter(this, localAccounts.getCurrentUser().getTasks());
        // testing adding colours by Nevin, setting font to white, background colour already in content_main.xml file
        // https://stackoverflow.com/questions/5577688/android-change-tab-text-color-programmatically
        TabHost tabhost = mainHost;
        tabhost.setCurrentTab(1);

        for(int i=0;i<tabhost.getTabWidget().getChildCount();i++) {
            TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#ecf0f1"));
            tv.setTextSize(16);
            tv.setTypeface(Typeface.SANS_SERIF);
        }
        createTaskButton = (FloatingActionButton) findViewById(R.id.btnFloatingAction);
        myDragEventListener dragEventListener = new myDragEventListener();
        targetText = findViewById(R.id.target_text);
        targetChange = findViewById(R.id.enter);
        broom = findViewById(R.id.broom);
        target = findViewById(R.id.target);
        target.setOnDragListener(dragEventListener);
        // Disable drop target.
        targetChange.setVisibility(GONE);
        broom.setVisibility(GONE);
        target.setVisibility(GONE);
        targetText.setVisibility(GONE);
        colour = targetChange.getSolidColor();

        UserAccount user = localAccounts.getCurrentUser();

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                System.out.println(s);
                if (localAccounts.getCurrentUser().isParent()) {
                    createTaskButton.setVisibility(View.VISIBLE);
                    if (s == "People") {
                        createTaskButton.setImageResource(R.drawable.stat_notify_chat_black);
                    }
                    else if (s == "Tasks") {
                        createTaskButton.setImageResource(R.drawable.open);
                    }
                    else {
                        createTaskButton.setVisibility(View.VISIBLE);
                        createTaskButton.setImageResource(R.drawable.shopping);
                    }

                    updateUI();
                }
                else
                    createTaskButton.setVisibility(GONE);
            }
        });

        ListView tasks = (ListView) findViewById(R.id.lstTasks);
        if (user.getTasks() != null) {
            TaskAdapter adapter = new TaskAdapter(this, user.getTasks());
            tasks.setAdapter(adapter);
        }

        tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent viewTask = new Intent(getApplicationContext(), ViewTaskActivity.class);
                viewTask.putExtra("index", i);
                startActivity(viewTask);
            }
        });
        TaskAdapter adapter = new TaskAdapter(this, user.getTasks());
        tasks.setAdapter(adapter);

        //View my task only
        //reference https://stackoverflow.com/questions/11278507/android-widget-switch-on-off-event-listener
        viewMyTaskToggle = (Switch)findViewById(R.id.swtMyTasksOnly);
        // check current state of a Switch (true or false).
        viewMyTaskToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, " You are currently viewing your own task", Toast.LENGTH_LONG).show();
                }
                // do something, the isChecked will be
                // true if the switch is in the On position
                UserAccount user = localAccounts.getCurrentUser();
                //ArrayList<Task> allTask = user.getTasks();
                ArrayList<Task> myTasks = user.getMyTasks();

                updateTaskView();

                //Toast.makeText(MainActivity.this, "Null task adapter test", Toast.LENGTH_LONG).show();
            }
        });

        // TEST CODE FOR SHOPPING LIST
        shoppingLists = new ArrayList<Shopping>();
        /*
        shoppingLists.add(new Shopping("Materials"));
        shoppingLists.add(new Shopping("Food"));

        lists.get(0).addItem(new Item("Lysol Wipes"));
        lists.get(0).addItem(new Item("Toilet Bowel Cleaner"));
        lists.get(0).addItem(new Item("Mop"));
        lists.get(0).addItem(new Item("Kitchen Tongs"));
        lists.get(0).addItem(new Item("Matches"));

        lists.get(1).addItem(new Item("Marshmallows"));
        lists.get(1).addItem(new Item("Kraft Dinner"));
        lists.get(1).addItem(new Item("Apples"));
        lists.get(1).addItem(new Item("Sour Cream"));
        lists.get(1).addItem(new Item("Carrots"));
        lists.get(1).addItem(new Item("Chicken"));
        lists.get(1).addItem(new Item("Frozen Pizza"));
        lists.get(1).addItem(new Item("Celery"));

        ListView shopping = (ListView) findViewById(R.id.lstShopping);
        shoppingAdapter = new ShoppingAdapter(this, shoppingLists);
        shopping.setAdapter(shoppingAdapter);
        // END TESTING CODE
        */

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView shopping = (ListView) findViewById(R.id.lstShopping);
        shopping.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                setNewItemInputEnabled(true);
                System.out.println("view " + view);

                // getting index of shopping list
                // code modified from https://stackoverflow.com/questions/20541821/get-listview-item-position-on-button-click

                ShoppingAdapter adapter = (ShoppingAdapter) shoppingList.getAdapter();
                adapter.setSelectedIndex(pos);
            }
        });

        //if child cannot create task so set floating action button to gone
        //Boolean child = !user.isParent();
        //Toast.makeText(MainActivity.this,child.toString()+ " IS CHILD ",Toast.LENGTH_LONG).show();
        updateTaskView();
        updateUI();
    }

    protected void addShoppingList(Shopping list){
        if (list == null)
            return;

        shoppingLists.add(list);
    }

    public void updateShoppingView(){
        if (localAccounts.getCurrentUser().getShoppingLists() == null)
            return;

        runOnUiThread(new Runnable(){
            public void run(){
                if (localAccounts.getCurrentUser().getShoppingLists() == null)
                    return;

                ArrayList<Shopping> lists = localAccounts.getCurrentUser().getShoppingLists();

                if (shoppingAdapter == null)
                    shoppingAdapter = new ShoppingAdapter(MainActivity.this, lists);

                shoppingList = (ListView) findViewById(R.id.lstShopping);
                shoppingAdapter.notifyDataSetChanged();
                shoppingAdapter = new ShoppingAdapter(MainActivity.this, lists);
                shoppingAdapter.notifyDataSetChanged();
                shoppingList.setAdapter(shoppingAdapter);
            }
        });
    }

    public void updateTaskView(){
        runOnUiThread(new Runnable(){
            public void run(){
                if(!localAccounts.getCurrentUser().isParent()){
                    createTaskButton.setVisibility(GONE);
                }
                else
                    createTaskButton.setVisibility(View.VISIBLE);

                ListView tasks = (ListView) findViewById(R.id.lstTasks);
                if (taskAdapter != null)
                    taskAdapter.notifyDataSetChanged();

                if (viewMyTaskToggle.isChecked() && localAccounts.getCurrentUser().getMyTasks() != null)
                    taskAdapter = new TaskAdapter(MainActivity.this, localAccounts.getCurrentUser().getMyTasks());
                else if (localAccounts.getCurrentUser().getTasks() != null)
                    taskAdapter = new TaskAdapter(MainActivity.this, localAccounts.getCurrentUser().getTasks());

                if (taskAdapter != null)
                    taskAdapter.notifyDataSetChanged();
                tasks.setAdapter(taskAdapter);
            }
        });
    }

    public void updatePeopleView(){
        runOnUiThread(new Runnable(){
            public void run(){

                ListView tasks = (ListView) findViewById(R.id.lstTasks);
                taskAdapter.notifyDataSetChanged();

                // Convert arraylist to array for account adapter.
                UserAccount[] accounts = new UserAccount[localAccounts.getFamilySize()];
                for (int i = 0; i < localAccounts.getFamilySize(); i++){
                    accounts[i] = localAccounts.getAccountAt(i);
                }

                ListView people = (ListView) findViewById(R.id.lstPeople);
                people.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent showAccount = new Intent(getApplicationContext(), PersonActivity.class);
                        showAccount.putExtra("pos",i);
                        startActivity(showAccount);
                    }
                });

                AccountAdapter adapter2 = new AccountAdapter(MainActivity.this, accounts);

                /*for (int i = 0; i < adapter2.getCount(); i++) {
                    UserAccount u1 = adapter2.getItem(i);
                    for (int j = i + 1; j < adapter2.getCount(); j++) {
                        UserAccount u2 = adapter2.getItem(j);
                        if (u1.getUserID().equals(u2.getUserID())) {
                            adapter2.remove(u2);
                            i = 0;
                        }
                    }
                }*/

                people.setAdapter(adapter2);
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            //Toast.makeText(MainActivity.this, " test GOING BACK ", Toast.LENGTH_LONG).show();
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            //startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //Toast.makeText(getApplicationContext(),"TEST OPTIONS MENU SELECTED", Toast.LENGTH_LONG).show();
        getMenuInflater().inflate(R.menu.main, menu);

        // Set text.
        TextView email = findViewById(R.id.txtUserEmail);
        TextView name = findViewById(R.id.txtUserName);
        email.setText(localAccounts.getCurrentUser().getEmail());
        name.setText(localAccounts.getCurrentUser().getFirstName() + " "
                    + localAccounts.getCurrentUser().getLastName());

        //Progress bar and points UI
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);

        bar.setProgress(100 * Integer.parseInt(localAccounts.getCurrentUser().getPoints())/localAccounts.getCurrentUser().getNextLevel());
        //bar.setProgress(100 - (((Integer.parseInt(localAccounts.getCurrentUser().getLevel())*1000) - Integer.parseInt(localAccounts.getCurrentUser().getPoints()))/10));

        TextView pointsfield = (TextView) findViewById(R.id.points_and_level_text);
        pointsfield.setText("Points: "+localAccounts.getCurrentUser().getPoints());

        TextView nextlevelfield = (TextView) findViewById(R.id.points_and_level_text2);
        nextlevelfield.setText("Next Level: "+(localAccounts.getCurrentUser().getNextLevel()));

        TextView currentLevel = (TextView) findViewById(R.id.current_level);
        currentLevel.setText("Lvl "+localAccounts.getCurrentUser().getLevel());

        TextView nextLevel = (TextView) findViewById(R.id.next_level);
        nextLevel.setText("Lvl "+(Integer.parseInt(localAccounts.getCurrentUser().getLevel())+1));

        // Create logout button listener.
        Button logout = (Button)findViewById(R.id.btnLogout);
        logout.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                logout();
            }
        });

        return true;
    }

    public void addTaskToTaskList(Task task) {
        TaskAdapter adapter = (TaskAdapter) ((ListView) findViewById(R.id.lstTasks)).getAdapter();
        adapter.add(task);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(),"TEST OPTIONS ITEM SELECTED", Toast.LENGTH_LONG).show();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingActivity = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //@SuppressLint("ResourceType")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //added
        if (id ==R.id.nav_open_tasks){
            createTaskButton.setImageResource(R.drawable.open);
            selectTab(1);
        }
        else if(id==R.id.nav_my_tasks){
            selectTab(1);
            viewMyTaskToggle.setChecked(true);
        }
        else if (id ==R.id.nav_shopping_list){
            createTaskButton.setImageResource(R.drawable.stat_notify_chat_black);
            updateShoppingView();
            selectTab(0);
        }
        else if (id ==R.id.nav_schedule){
            Intent calendarActivity = new Intent(getApplicationContext(), CalendarActivity.class);
            startActivity(calendarActivity);
        }
        else if (id ==R.id.nav_task_backlog){
            //not done yet but we do this
            selectTab(1);
            viewMyTaskToggle.setChecked(false);
        }
        else if (id ==R.id.nav_people){
            selectTab(2);

        }
        else if (id ==R.id.nav_broom_closet_tools){
            //ignore this code
        }
        else if (id ==R.id.nav_settings){
            Intent settingActivity = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingActivity);
        }
        else if (id ==R.id.nav_about){
            Intent aboutActivity = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(aboutActivity);
        }else if (id ==R.id.nav_resource_list){
            boolean flag =true;
            if(localAccounts.getCurrentUser().getTasks().size()>0){
            for(int i=0; i<localAccounts.getCurrentUser().getTasks().size();i++){
                if(localAccounts.getCurrentUser().getTasks().get(i).getRequiredItems()!=null){
                    flag=false;
                    Intent resourceActivity = new Intent(getApplicationContext(), ResourceActivity.class);
                    startActivity(resourceActivity);

                    break;

                }


            }

            if (flag){
                Toast.makeText(MainActivity.this, "There are no resources assigned", Toast.LENGTH_LONG).show();

            }


            }else{
                Toast.makeText(MainActivity.this, "There are no resources assigned", Toast.LENGTH_LONG).show();

            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Incomplete
    //Code Sample
    //http://abhiandroid.com/ui/calendarview
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void viewCalendar(){
        setContentView(R.layout.calendar_view);
        simpleCalendarView = (CalendarView) findViewById(R.id.simpleCalendarView); // get the reference of CalendarView
        simpleCalendarView.setFocusedMonthDateColor(Color.RED); // set the red color for the dates of  focused month
        simpleCalendarView.setUnfocusedMonthDateColor(Color.BLUE); // set the yellow color for the dates of an unfocused month
        simpleCalendarView.setSelectedWeekBackgroundColor(Color.RED); // red color for the selected week's background
        simpleCalendarView.setWeekSeparatorLineColor(Color.GREEN); // green color for the week separator line
        // perform setOnDateChangeListener event on CalendarView
        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                Toast.makeText(getApplicationContext(), dayOfMonth + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void selectTab(int index) {
        TabHost tabhost = (TabHost) findViewById(R.id.mainTabHost);
        tabhost.setCurrentTab(index);
    }

    public void setNewItemInputEnabled(boolean enabled) {
        EditText newItem = (EditText) findViewById(R.id.txtNewItem);
        ImageButton btnNewItem = (ImageButton) findViewById(R.id.btnNewItem);
        newItem.setEnabled(enabled);
        btnNewItem.setEnabled(enabled);
    }

    public void noShoppingListSelected(View view) {
        setNewItemInputEnabled(false);
    }

    public void btnFloatingClick(View view) {
        TabHost tabs = (TabHost) findViewById(R.id.mainTabHost);
        if (tabs.getCurrentTab() == 0) {
            Intent newShoppingList = new Intent(getApplicationContext(), NewShoppingListActivity.class);
            startActivityForResult(newShoppingList, CREATE_NEW_SHOPPING_LIST);

        } else if (tabs.getCurrentTab() == 1) {
            btnNewTaskClick(view);
        } else if (tabs.getCurrentTab() == 2) {
            Toast.makeText(MainActivity.this, "Feature not implemented yet", Toast.LENGTH_LONG).show();
        }
    }
    // On click methods.
    public void btnNewTaskClick(View view) {
        Intent newTask = new Intent(getApplicationContext(), NewTaskActivity.class);
        newTask.putExtra("editTask", false);
        startActivity(newTask);
    }
    public void btnNewItemClick(View view) {
        String text = ((EditText) findViewById(R.id.txtNewItem)).getText().toString();
        if (text.equals(""))
            return;

        ListView shoppingList = (ListView) findViewById(R.id.lstShopping);

        ShoppingAdapter adapter = (ShoppingAdapter) shoppingList.getAdapter();

        ArrayList<Shopping> items = localAccounts.getCurrentUser().getShoppingLists();

        int selected = adapter.getSelectedIndex();

        Item toAdd = new Item(text);
        toAdd.setIsShopping(true);
        items.get(selected).addItem(toAdd);

        ShoppingAdapter newAdapter = new ShoppingAdapter(this, items);
        shoppingList.setAdapter(newAdapter);

        adapter = null; // Doing to make sure we don't keep the old adapter in memory

        EditText newItem = (EditText) findViewById(R.id.txtNewItem);

        // Update shopping list in database.
        db.addToShoppingList(items.get(selected), toAdd);

        newItem.setEnabled(false);
        newItem.setText("");
        ((ListView) findViewById(R.id.lstShopping)).requestFocus();
        ((ImageButton) findViewById(R.id.btnNewItem)).setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_NEW_SHOPPING_LIST && resultCode == RESULT_OK && data != null) {
            ShoppingAdapter adapter = (ShoppingAdapter) ((ListView) findViewById(R.id.lstShopping)).getAdapter();
            adapter.add((Shopping) data.getSerializableExtra("Shopping List"));
        }
    }
    // Updates the UI with current data.
    protected void updateUI(){
        // Add task for testing.
        UserAccount user = localAccounts.getCurrentUser();
        myDragEventListener dragEventListener = new myDragEventListener();

        final ListView tasks = (ListView) findViewById(R.id.lstTasks);
        if (user.getTasks() != null) {
            TaskAdapter adapter = new TaskAdapter(this, user.getTasks());
            tasks.setAdapter(adapter);
            tasks.setTag("task");
            View taskView = new View(this);
            taskView.setOnDragListener(dragEventListener);
        }

        tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent viewTask = new Intent(getApplicationContext(), ViewTaskActivity.class);
                viewTask.putExtra("index", i);
                startActivity(viewTask);
            }
        });
        // Reference: https://developer.android.com/guide/topics/ui/drag-drop.html
        tasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (localAccounts.getCurrentUser().isParent()) {
                    // Enable drop target.
                    targetChange.setVisibility(View.VISIBLE);
                    broom.setVisibility(View.VISIBLE);
                    target.setVisibility(View.VISIBLE);
                    targetText.setVisibility(View.VISIBLE);

                    System.out.println("LOOONG CLICK " + i);
                    ClipData.Item item = new ClipData.Item(Integer.toString(i));

                    ClipData dragData = new ClipData((CharSequence) Integer.toString(i), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

                    View.DragShadowBuilder myShadow = new MyDragShadowBuilder(view);

                    view.startDragAndDrop(dragData, myShadow, view, 0);
                }
                return false;
            }
        });
        TaskAdapter adapter = new TaskAdapter(this, user.getTasks());
        tasks.setAdapter(adapter);
        // Convert arraylist to array for account adapter.
        UserAccount[] accounts = new UserAccount[localAccounts.getFamilySize()];
        for (int i = 0; i < localAccounts.getFamilySize(); i++){
            accounts[i] = localAccounts.getAccountAt(i);
        }
        ListView people = (ListView) findViewById(R.id.lstPeople);
        people.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showAccount = new Intent(getApplicationContext(), PersonActivity.class);
                showAccount.putExtra("pos",i);
                startActivity(showAccount);
            }
        });
        AccountAdapter adapter2 = new AccountAdapter(this, accounts);
        people.setAdapter(adapter2);

        shoppingList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                deleteShoppingList(position);
                return false;
            }
        });
        updateShoppingView();
        updatePeopleView();
        //updateNavUI();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 2;

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }

    protected class myDragEventListener implements View.OnDragListener {

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate();

                        // returns true to indicate that the View can accept the dragged data.
                        return true;
                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;

                case DragEvent.ACTION_DRAG_ENTERED:
                    // Applies a green tint to the View. Return true; the return value is ignored.
                    targetChange.setVisibility(View.VISIBLE);
                    v.setBackgroundColor(Color.RED);

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    targetChange.setVisibility(GONE);
                    return true;

                case DragEvent.ACTION_DROP:

                    System.out.println("DROPPED");

                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    String dragData = (String) item.getText().toString();

                    // Displays a message containing the dragged data.
                    Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_LONG).show();

                    // Delete task.
                    Task task = localAccounts.getCurrentUser().getTask(Integer.parseInt(dragData));
                    try {
                        // Update database.
                        db.deleteTask(task);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    // Returns true. DragEvent.getResult() will return true.
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    // Disable drop target.
                    targetChange.setVisibility(GONE);
                    broom.setVisibility(GONE);
                    target.setVisibility(GONE);
                    targetText.setVisibility(GONE);

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // Does a getResult(), and displays what happened.
                    /*if (event.getResult()) {
                        Toast.makeText(MainActivity.this, "The drop was handled.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(MainActivity.this, "The drop didn't work.", Toast.LENGTH_LONG).show();

                    }*/

                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                    break;
            }
            return false;
        }
    };

    // Called when logout button is clicked.
    private void logout(){
        // Get confirmation
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        // Logout of Firebase.
        db.logout();
        localAccounts.deleteAccounts();
        finish();
    }

    // Called when (floating action bar) create new task button is clicked.
    private void createNewTask(){
        Intent newTaskIntent = new Intent(getApplicationContext(), NewTaskActivity.class);
        startActivity(newTaskIntent);
        finish();
    }
    // Called when the user clicks on a task.
    private void viewTask(){
        //not used
    }

    //reference Nevin, reused by David
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    private void deleteItem(final Item toDelete){
        new AlertDialog.Builder(this)
                .setTitle("EXIT")
                .setMessage("Do you really want to delete " + toDelete.getName() + "?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //code for yes button
                        onBackPressed();
                    }
                }).create().show();
    }
    //reference Nevin, reused by David
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    private void deleteShoppingList(int positionInList){
        final Shopping toDelete = (Shopping) shoppingList.getAdapter().getItem(positionInList);
        new AlertDialog.Builder(this)
                .setTitle("DELETE SHOPPING LIST")
                .setMessage("Do you really want to delete shopping list " + toDelete.getListName() + "?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //code for yes button
                        db.deleteShoppingList(toDelete);
                        localAccounts.getCurrentUser().removeShoppingList(toDelete);
                        localAccounts.getCurrentUser().removeShoppingKey(toDelete.getId());
                        updateUI();
                    }
                }).create().show();
    }
}