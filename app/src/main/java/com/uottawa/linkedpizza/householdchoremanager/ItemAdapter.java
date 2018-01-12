package com.uottawa.linkedpizza.householdchoremanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Waleed on 12/6/17.
 */

public class ItemAdapter extends  ArrayAdapter<Task>{
        private final Context context;
        private ArrayList<Task> lists;


        Database db;

        public ItemAdapter(Context context, ArrayList<Task> RequiredItemslist) {
            super(context, R.layout.activity_resource, RequiredItemslist);
            this.context = context;
            this.lists = RequiredItemslist;
            this.db = Database.getInstance(null);
        }

        public View getView(int position, final View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.shopping_layout, parent, false);

            TextView txtCategory = (TextView) rowView.findViewById(R.id.txtCategory);
            GridLayout grid = (GridLayout) rowView.findViewById(R.id.grdItems);

            txtCategory.setText(lists.get(position).getTaskName());

            Item[] listItems = lists.get(position).getRequiredItems();

            for (int i = 0; i < listItems.length; i++) {

                final Item item = listItems[i];
                final TextView chk = new TextView(context);
                chk.setText(item.getName());
                chk.setTextColor(Color.BLACK);
                chk.setWidth(parent.getWidth() / 2);

                chk.setVisibility(View.VISIBLE);
                grid.addView(chk);


            }



            return rowView;
        }}


