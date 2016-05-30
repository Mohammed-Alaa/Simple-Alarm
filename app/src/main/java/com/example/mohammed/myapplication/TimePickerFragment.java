package com.example.mohammed.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by mohammed on 5/29/16.
 */
public class TimePickerFragment  extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    Communicator communicator=null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

       try {
           communicator= (Communicator) activity;
       }catch (Exception e){

       }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        communicator=null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        boolean is24Hour;
        if(hourOfDay>12){
            is24Hour=true;
        }else {
            is24Hour=false;
        }
        communicator.setTime(hourOfDay,minute,is24Hour);
    }


    public interface Communicator {
        public void setTime(int hours,int minuts,boolean am_pm);
    }
}