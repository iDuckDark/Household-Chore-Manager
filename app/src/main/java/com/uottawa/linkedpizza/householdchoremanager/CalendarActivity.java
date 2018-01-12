package com.uottawa.linkedpizza.householdchoremanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.CalendarView;
import android.widget.Toast;

/**
 * Created by iDarkDuck on 12/3/17.
 */

//http://abhiandroid.com/ui/calendarview
public class CalendarActivity extends AppCompatActivity {
    CalendarView simpleCalendarView;
    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_view);
        simpleCalendarView = (CalendarView) findViewById(R.id.simpleCalendarView);

        //3. setFirstDayOfWeek(int firstDayOfWeek): This method is used to set the first day of the week.
        //Below we set the 2 value means Monday as the first day of the week.
        simpleCalendarView.setFirstDayOfWeek(2);

        //4. getFirstDayOfWeek(): This method is used to get the first day of week. This method returns an int type value.
        //Below we get the first day of the week of CalendarView.

        int firstDayOfWeek= simpleCalendarView.getFirstDayOfWeek();

        //5. setMaxDate(long maxDate): This method is used to set the maximal date supported by thisCalendarView
        // in milliseconds since January 1, 1970 00:00:00 in user’s preferred time zone.
        //Below we set the long value for maximal date supported by the CalendarView.

        //simpleCalendarView.setMaxDate(1463918226920L);

        //6. getMaxDate(): This method is used to get the maximal date supported by this CalendarView
        //in milliseconds since January 1, 1970 00:00:00 in user’s preferred time zone. This method returns long type value for maximal date supported by this CalendarView.
        //Below we firstly set the long value for the maximal date and then get the maximal value supported by the CalendarView.
        //simpleCalendarView.setMaxDate(1463918226920L);

        simpleCalendarView.getShowWeekNumber(); // checks whether the week number are shown or not.

        Drawable verticalBar=simpleCalendarView.getSelectedDateVerticalBar();

        //simpleCalendarView.setSelectedDateVerticalBar(getResources().getDrawable(R.drawable.ic_launcher));

        simpleCalendarView.setSelectedDateVerticalBar(Color.BLUE); // set the color for the vertical bar
        simpleCalendarView.setSelectedWeekBackgroundColor(Color.BLACK); // set black color in the background of selected week
        // perform setOnDateChangeListener event on CalendarView
        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // add code here
                String date = " YEAR: " + Integer.toString(year) + " MONTH: " +Integer.toString(month)+ " DAY: " +Integer.toString(dayOfMonth);
                Toast.makeText(CalendarActivity.this,date,1).show();
            }
        });
    }



    @Override
    public void onBackPressed() {
        //Toast.makeText(CalendarActivity.this, " test GOING BACK in Calendar ActivityClass ", Toast.LENGTH_LONG).show();
        finish();
        //super.onBackPressed();
    }
}
