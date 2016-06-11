package com.example.mohammed.myapplication;

/**
 * Created by mohammed on 6/11/16.
 */
public class AlarmTimeHandFormat {
    public int getHours(String s) {
        String am_pm=s.substring(s.length() -2,s.length());
        int hours=Integer.parseInt(s.substring(0,s.indexOf(":")));

        if(am_pm.equalsIgnoreCase("pm")){
            hours+=12;
        }
        return hours;
    }

    public int getMinutes(String s) {
        return  Integer.parseInt(s.substring(s.indexOf(":")+1,s.length()-3));

    }
}
