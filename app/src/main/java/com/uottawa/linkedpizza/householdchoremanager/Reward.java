package com.uottawa.linkedpizza.householdchoremanager;

/**
 * Created by Nick-JR on 11/20/2017.
 */

public class Reward {
    private int points;

    // No reward by default.
    Reward(){
        points = 0;
    }
    Reward(int points){
        this.points = points;
    }

    public void setPoints(int points){
        this.points = points;
    }

    public int getPoints(){
        return points;
    }
}