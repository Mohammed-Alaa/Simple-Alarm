package com.example.mohammed.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity  implements TimePickerFragment.Communicator{

    TextView clock;
    AlarmManager alarm;
    PendingIntent pIntent;
    Intent intent;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clock=(TextView) findViewById(R.id.text_clock);

    }


    public void cancelAlarm(View view) {
      /*  Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
*/
        // If the alarm has been set, cancel it.
        if (alarm!= null) {
            alarm.cancel(pIntent);
        }

        disableBoorReciver();

    }

    private void disableBoorReciver() {
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void setAlarm(View view) {
        FragmentManager fm = getSupportFragmentManager();
        TimePickerFragment timeDialog = new TimePickerFragment();
        timeDialog.show(fm, "time_picker_fragment");
    }

    @Override
    public void setTime(int hours, int minuts, boolean am_pm) {


        enableBootReciver();
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hours);
        calendar.set(Calendar.MINUTE,minuts);
        calendar.set(Calendar.SECOND,0);

        formatClock(hours,minuts,am_pm);
        // Construct an intent that will execute the AlarmReceiver
        intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        pIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);
        // Setup periodic alarm every 5 seconds
        alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pIntent);

    }

    private void enableBootReciver() {

        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void formatClock(int hours, int minuts, boolean am_pm) {


        String am_pmFormat;
        String hourFormat;
        String minuteFormat;
        if(am_pm){
            am_pmFormat="PM";
        }else {
            am_pmFormat="AM";
        }

        if(hours%12==0){
            hourFormat="12";
        }else{
            hours=hours%12;
            if(hours<10){
                hourFormat="0"+hours;
            }else {
                hourFormat=hours+"";
            }
        }

        if(minuts<10){
            minuteFormat="0"+minuts;
        }else {
            minuteFormat=minuts+"";
        }

        clock.setText(hourFormat+":"+minuteFormat+ " "+am_pmFormat);
        Toast.makeText(this,"Alarm set "+clock.getText().toString(),Toast.LENGTH_LONG).show();
    }
}