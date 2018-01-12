package com.uottawa.linkedpizza.householdchoremanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class NewShoppingListActivity extends AppCompatActivity {

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shopping_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ((ListView) findViewById(R.id.lstItems)).setAdapter(adapter);
    }

    public void newItemOnClick(View view) {

        EditText newItem = (EditText) findViewById(R.id.txtNewItem);

        if (newItem.getText().toString().equals("")) {
            return;
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) ((ListView) findViewById(R.id.lstItems)).getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().toLowerCase().equals(newItem.getText().toString().toLowerCase())) {
                Toast.makeText(NewShoppingListActivity.this, "Item already in list", Toast.LENGTH_LONG).show();
                return;
            }
        }
        adapter.add(newItem.getText().toString());
        newItem.setText("");
    }

    public void doneButtonOnClick(View view) {
        EditText listTitle = (EditText) findViewById(R.id.txtListName);
        ListView list = (ListView) findViewById(R.id.lstItems);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) list.getAdapter();

        listTitle.getBackground().clearColorFilter();

        if (listTitle.getText().toString().equals("")) {
            Toast.makeText(NewShoppingListActivity.this, "Shopping list must have a name", Toast.LENGTH_LONG).show();
            listTitle.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            return;
        }
        if (adapter.getCount() == 0) {
            Toast.makeText(NewShoppingListActivity.this, "Shopping list cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        Shopping newList = new Shopping(listTitle.getText().toString());
        for (int i = 0; i < adapter.getCount(); i++) {
            Item toAdd = new Item(adapter.getItem(i).toString());
            toAdd.setIsShopping(true);
            newList.addItem(toAdd);
        }
        // Write list to database.
        db = db.getInstance(null);
        db.writeShoppingList(newList);

        Toast.makeText(NewShoppingListActivity.this, "Shopping list created!", Toast.LENGTH_SHORT).show();
        Toast.makeText(NewShoppingListActivity.this, "Press and hold a shopping list to delete it.", Toast.LENGTH_LONG).show();

        Intent result = new Intent();
        result.putExtra("Shopping List", newList);
        setResult(RESULT_OK, result);
        finish();
    }

    public void cancelButtonOnClick(View view){
        dialog();
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
                        //Toast.makeText(NewShoppingListActivity.this, " DIALOG code shopping", Toast.LENGTH_LONG).show();
                        //Toast.makeText(NewShoppingListActivity.this, " DIALOG code shopping", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                }).create().show();
    }

}
