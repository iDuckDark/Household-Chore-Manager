package com.uottawa.linkedpizza.householdchoremanager;

/**
 * Holds a date that is able to be printed as a string of format
 * YYYY/MM/DD or in plain English (ex. September 25, 2017).
 */

public class Date {

    private int year;
    private int month;
    private int day;

    public Date(int year, int month, int day) {
        
        if (year > 2999 || (month > 12 || month < 1) || (day > 31 || day < 1))
            throw new IllegalArgumentException("Invalid date");

        this.year = year;
        this.month = month;
        this.day = day;

    }

    public Date(String date) {

        String tmp = "";

        year = -1;
        month = -1;
        day = -1;

        try {

            for (int i = 0; i < date.length(); i++) {

                tmp += date.charAt(i);

                if (month == -1 && date.charAt(i + 1) == ' ') {

                    switch (tmp) {
                        case "January":
                            month = 1;
                            break;
                        case "February":
                            month = 2;
                            break;
                        case "March":
                            month = 3;
                            break;
                        case "April":
                            month = 4;
                            break;
                        case "May":
                            month = 5;
                            break;
                        case "June":
                            month = 6;
                            break;
                        case "July":
                            month = 7;
                            break;
                        case "August":
                            month = 8;
                            break;
                        case "September":
                            month = 9;
                            break;
                        case "October":
                            month = 10;
                            break;
                        case "November":
                            month = 11;
                            break;
                        case "December":
                            month = 12;
                            break;
                    }

                    tmp = "";
                    i++;

                }

                if (day == -1 && date.charAt(i + 1) == ',') {
                    day = Integer.parseInt(tmp);
                    tmp = "";
                    i += 2;
                }

                if (year == -1 && i == date.length() - 1) {
                    year = Integer.parseInt(tmp);
                }

            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Given string is not of valid date format");
        }
    }

    public boolean greaterThan(Date date) {
        if (date.year < year)
            return true;

        if (date.year == year && date.month < month)
            return true;

        if (date.year == year && date.month == month && date.day < day)
            return true;

        return false;
    }

    public boolean equals(Date date) {
        return year == date.year && month == date.month && day == date.day;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String toString() {

        String date = "";

        // Add year
        date += year + "/";

        // Add month
        date += (month < 10) ? "0" + month : month;
        date += "/";

        // Add day
        date += (day < 10) ? "0" + day : day;

        return date;

    }

    public String toEnglishString() {

        String date = "";

        switch (month) {
            case 1: date += "January ";
                    break;
            case 2: date += "February ";
                    break;
            case 3: date += "March ";
                    break;
            case 4: date += "April ";
                    break;
            case 5: date += "May ";
                    break;
            case 6: date += "June ";
                    break;
            case 7: date += "July ";
                    break;
            case 8: date += "August ";
                    break;
            case 9: date += "September ";
                    break;
            case 10: date += "October ";
                    break;
            case 11: date += "November ";
                    break;
            case 12: date += "December ";
                    break;
        }

        date += day + ", " + year;

        return date;

    }

}