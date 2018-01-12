package com.uottawa.linkedpizza.householdchoremanager;

import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.View;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.content.res.ColorStateList;

import java.util.ArrayList;

/**
 * Created by david on 2017-11-22.
 */

public class ShoppingAdapter extends ArrayAdapter<Shopping> {

    private final Context context;
    private ArrayList<Shopping> lists;
    private int selectedIndex;
    Database db;

    public ShoppingAdapter(Context context, ArrayList<Shopping> shoppingLists) {
        super(context, R.layout.shopping_layout, shoppingLists);
        this.context = context;
        this.lists = shoppingLists;
        this.db = Database.getInstance(null);
    }

    public View getView(int position, final View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.shopping_layout, parent, false);

        TextView txtCategory = (TextView) rowView.findViewById(R.id.txtCategory);
        GridLayout grid = (GridLayout) rowView.findViewById(R.id.grdItems);

        txtCategory.setText(lists.get(position).getListName());

        Item[] listItems = lists.get(position).getItems();

        for (int i = 0; i < listItems.length; i++) {

            final Item item = listItems[i];
            final CheckBox chk = new CheckBox(context);
            chk.setText(item.getName());
            chk.setWidth(parent.getWidth() / 2);
            chk.setFocusable(false);
            chk.setChecked(item.getHasBeenShopped());
            final int pos = position;

            chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    item.setHasBeenShopped(checked);
                    db.updateItem(item);
                }
            });

            chk.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    dialog(pos, chk.getText().toString());
                    return false;
                }
            });

            grid.addView(chk);
        }

        return rowView;
    }

    public ArrayList<Shopping> getLists() {
        return lists;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int pos) {
        selectedIndex = pos;
    }
    //reference Nevin, reused by David
    //https://www.youtube.com/watch?v=IH3sWb1WacI
    public void dialog(final int listIndex, final String item){
        new AlertDialog.Builder(context)
                .setTitle("DELETE ITEM")
                .setMessage("Do you want do delete item " + item + "?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }
}