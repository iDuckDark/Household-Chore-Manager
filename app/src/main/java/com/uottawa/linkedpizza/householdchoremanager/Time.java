package com.uottawa.linkedpizza.householdchoremanager;

/**
 * Created by david on 2017-11-29.
 */

public class Time {

    private int hour;
    private int minute;

    public Time(int hour, int minute) {

        if ((hour < 0 || hour > 23) || (minute < 0 || minute > 59))
            throw new IllegalArgumentException("Invalid time");

        this.hour = hour;
        this.minute = minute;
    }

    public Time(int hour, int minute, String amOrPm) {

        if ((hour < 0 || hour > 23) || (minute < 0 || minute > 59) || (!amOrPm.equals("AM") && !amOrPm.equals("PM")))
            throw new IllegalArgumentException("Invalid time");

        this.hour = convert12HrTo24Hr(hour, amOrPm);

        this.minute = minute;

    }

    public Time(String time) {

        hour = -1;
        minute = -1;
        String tmp = "";

        try {
            for (int i = 0; i < time.length(); i++) {

                tmp += time.charAt(i);

                if (hour == -1 && time.charAt(i + 1) == ':') {
                    hour = Integer.parseInt(tmp);
                    tmp = "";
                    i++;
                }

                if (minute == -1 && time.charAt(i + 1) == ' ') {
                    minute = Integer.parseInt(tmp);
                    tmp = "";
                    i++;
                }
            }

            hour = convert12HrTo24Hr(hour, tmp);

        } catch (Exception e) {
            throw new IllegalArgumentException("Given string is not of valid time format");
        }
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public boolean equals(Time time) {
        return hour == time.hour && minute == time.minute;
    }

    public int getMinute() {
        return minute;
    }

    public String toString() {
        String time = "";

        if (hour == 0) {
            time += 12;
        } else if (hour > 12) {
            time += hour - 12;
        } else {
            time += hour;
        }

        time += ":" + ((minute < 10) ? "0" + minute : minute);

        if (hour > 11) {
            time += " PM";
        } else {
            time += " AM";
        }

        return time;
    }

    public String to24HourString() {
        String time = hour + ":" + ((minute < 10) ? "0" + minute : minute);
        return time;
    }

    private int convert12HrTo24Hr(int hour, String amOrPm) {

        if (amOrPm.equals("AM")) {
            if (hour == 12) {
                hour = 0;
            }
        }

        if (amOrPm.equals("PM")) {
            if (hour < 12) {
                hour = hour + 12;
            }
        }

        return hour;

    }
}
