package com.example.mohammed.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity implements TimePickerFragment.Communicator {

    TextView clock;
    AlarmManager alarm;
    PendingIntent pIntent;
    Intent intent;
    TextView zekr;
    GregorianCalendar geoCal;
    double latitude, longitude;

    protected TextView mTextViewCity;
    protected TextView mTextViewDate;
    protected TextView[][] mTextViewPrayers;
    PrayTime prayers;
    ArrayList prayerTimes;
    ArrayList prayerNames;
    SwitchCompat switchCompat;
    boolean mReceiverIsRegistered;
    protected BroadcastReceiver mReceiver;
    public final static String NOTIFY_MESSAGE = "org.linuxac.bilal.NOTIFY";
    public final static String UPDATE_MESSAGE = "org.linuxac.bilal.UPDATE";
    protected static final String TAG = "Zekr";

    static int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initReceiver();
        initViews();
        initViewData(geoCal);
        Intent getIntent = getIntent();
        String data = getIntent.getStringExtra("zekrData");
        zekr.setText(data);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String time = preferences.getString("formatKey", "Set Clock");
        if (!time.equalsIgnoreCase("")) {
            clock.setText(time);
        }


        Log.e("MainActivity ", "onCreate()");


    }

    private void initReceiver() {
        mReceiverIsRegistered = false;
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Extract data included in the Intent
                Log.e("Update", "update prayer times");
                updateUIData();
            }
        };
    }

    private void updateUIData() {

        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);

        prayers = new PrayTime();

        prayers.setTimeFormat(prayers.Time12);
        prayers.setCalcMethod(prayers.Egypt);
        prayers.setAsrJuristic(prayers.Shafii);
        prayers.setAdjustHighLats(prayers.AngleBased);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        int i, j;

        String[] dataArr = new String[prayerTimes.size()];
        dataArr = (String[]) prayerTimes.toArray(dataArr);

        ArrayList<String> pt = prayers.getPrayerTimes(cal, latitude, longitude, timezone);

        AlarmTimeHandFormat form=new AlarmTimeHandFormat();

        GregorianCalendar nowCal = new GregorianCalendar();
        // Find next prayer and set alarm
        GregorianCalendar next = null;
        GregorianCalendar[] ptCal = new GregorianCalendar[prayerTimes.size()];
        for (i = 0; i < prayerTimes.size(); i++) {
            ptCal[i] = (GregorianCalendar)nowCal.clone();
            ptCal[i].set(Calendar.HOUR_OF_DAY,form.getHours(pt.get(i)));
            ptCal[i].set(Calendar.MINUTE, form.getMinutes(pt.get(i)));
            ptCal[i].set(Calendar.SECOND,0);
        }

        for (i = 0; i < prayerTimes.size(); i++) {
            if (ptCal[i].after(nowCal)) {
                if (i == 1 || i==4) {
                    i++;            // skip sunrise,sunset which isn't a prayer
                }
                next = ptCal[i];
                count=i;
                break;
            }
        }


        for (int m = 0; m < prayerTimes.size(); m++) {
            Log.e("Pray ", prayerNames.get(m) + " - "
                    + prayerTimes.get(m));
        }

        if (next == null) {
            // next prayer is tomorrow's Fajr
            ArrayList<String> nextPT = prayers.getPrayerTimes(cal, latitude, longitude, timezone);
            /*
            // then i == Prayer.NB_PRAYERS
            mTextViewPrayers[i][1].setText(
                    String.format(" %3d:%02d\n", nextPT.hour, nextPT.minute));
            for (j = 0; j < 3; j++) {
                mTextViewPrayers[i][j].setVisibility(TextView.VISIBLE);
            }*/

            next = (GregorianCalendar)nowCal.clone();
            next.add(Calendar.DATE, 1);
            next.set(Calendar.HOUR_OF_DAY, form.getHours(nextPT.get(count)));
            next.set(Calendar.MINUTE, form.getMinutes(nextPT.get(count)));
            next.set(Calendar.SECOND, 0);
        }

        // prepare alarm message in AR only
        if (i >= prayerTimes.size()) {
            i = 0;
            count=i;
            // Removes "next" from "next fajr" loop from start again
        }

        for (j = 0; j < 3; j++) {
            mTextViewPrayers[count][j].setTypeface(null, Typeface.BOLD);
            mTextViewPrayers[count][j].setTextColor(Color.rgb(0, 200, 0));
        }



        String message = getString(R.string.hana_ar) + " " +
                mTextViewPrayers[count][0].getText().toString() + " " +
                mTextViewPrayers[count][1].getText().toString();

        // Schedule alarm
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        alarmIntent.putExtra(NOTIFY_MESSAGE, message);
        PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), sender);
        Log.e("Next ", "Alarm scheduled for " +
                DateFormat.getDateTimeInstance().format(next.getTime()));

    }

    private void initViewData(GregorianCalendar geoCal) {


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        double timezone = (Calendar.getInstance().getTimeZone()
                .getOffset(Calendar.getInstance().getTimeInMillis()))
                / (1000 * 60 * 60);


        String lat_long = sharedPref.getString("city_name", "30.044420/31.235712");
        latitude = Double.parseDouble(lat_long.substring(0, lat_long.indexOf('/')));
        longitude = Double.parseDouble(lat_long.substring(lat_long.indexOf('/') + 1));
        // Retrive lat, lng using location API


        prayers = new PrayTime();

        prayers.setTimeFormat(prayers.Time12);
        prayers.setCalcMethod(prayers.Egypt);
        prayers.setAsrJuristic(prayers.Shafii);
        prayers.setAdjustHighLats(prayers.AngleBased);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        Date today = geoCal.getTime();
        mTextViewDate.setText(DateFormat.getDateInstance().format(today));
        String cityName = getCityName(lat_long);
        mTextViewCity.setText(cityName);


        prayerTimes = prayers.getPrayerTimes(cal, latitude, longitude, timezone);
        prayerNames = prayers.getTimeNames();

        prayerTimes = prayers.getPrayerTimes(geoCal, latitude, longitude, timezone);
        prayerNames = prayers.getTimeNames();

        int i, j;

        // change Dhuhur to Jumuaa if needed.
        if (geoCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            mTextViewPrayers[2][0].setText(getString(R.string.jumu3a_ar));
            mTextViewPrayers[2][2].setText(getString(R.string.jumu3a_en));
        }

        for (i = 0; i < prayerTimes.size(); i++) {
            for (j = 0; j < 3; j++) {
                if(i==4){
                    mTextViewPrayers[i][j].setVisibility(View.GONE);
                }
                mTextViewPrayers[i][j].setTypeface(null, Typeface.NORMAL);
                mTextViewPrayers[i][j].setTextColor(Color.rgb(0, 0, 0));

            }
        }



        String[] dataArr = new String[prayerTimes.size()];
        dataArr = (String[]) prayerTimes.toArray(dataArr);


        for (int k = 0; k < prayerTimes.size(); k++) {

            mTextViewPrayers[k][1].setText(dataArr[k] + "");

        }

    }

    private void initViews() {
        switchCompat = (SwitchCompat) findViewById(R.id.btn_switch);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    String time=clock.getText().toString();
                    String am_pm=time.substring(time.length() -2,time.length());
                    int hours=Integer.parseInt(time.substring(0,time.indexOf(":")));
                    int minuts=Integer.parseInt(time.substring(time.indexOf(":")+1,time.length()-3));

                    Toast.makeText(getApplicationContext(),hours +" " + minuts +" "+ am_pm ,Toast.LENGTH_LONG).show();

                    if(am_pm.equalsIgnoreCase("PM")){
                       setTime(hours+12,minuts,true);
                    }else {
                        setTime(hours,minuts,false);
                    }
                } else {
                    cancelAlarm();
                }
            }
        });
        geoCal = new GregorianCalendar();
        clock = (TextView) findViewById(R.id.text_clock);
        zekr = (TextView) findViewById(R.id.text_zekr);
        mTextViewCity = (TextView) findViewById(R.id.textViewCity);
        mTextViewDate = (TextView) findViewById(R.id.textViewDate);
        TextView[][] tvp = {
                {
                        (TextView) findViewById(R.id.textViewFajrAR),
                        (TextView) findViewById(R.id.textViewFajr),
                        (TextView) findViewById(R.id.textViewFajrEN)
                },
                {
                        (TextView) findViewById(R.id.textViewSunriseAR),
                        (TextView) findViewById(R.id.textViewSunrise),
                        (TextView) findViewById(R.id.textViewSunriseEN)
                },
                {
                        (TextView) findViewById(R.id.textViewDhuhurAR),
                        (TextView) findViewById(R.id.textViewDhuhur),
                        (TextView) findViewById(R.id.textViewDhuhurEN)
                },
                {
                        (TextView) findViewById(R.id.textViewAsrAR),
                        (TextView) findViewById(R.id.textViewAsr),
                        (TextView) findViewById(R.id.textViewAsrEN)
                },

                {
                        (TextView) findViewById(R.id.textViewSunSetAR),
                        (TextView) findViewById(R.id.textViewSunset),
                        (TextView) findViewById(R.id.textViewSunSetEN)
                },
                {
                        (TextView) findViewById(R.id.textViewMaghribAR),
                        (TextView) findViewById(R.id.textViewMaghrib),
                        (TextView) findViewById(R.id.textViewMaghribEN)
                },
                {
                        (TextView) findViewById(R.id.textViewIshaAR),
                        (TextView) findViewById(R.id.textViewIsha),
                        (TextView) findViewById(R.id.textViewIshaEN)
                },


        };
        mTextViewPrayers = tvp.clone();

    }



    @Override
    protected void onStart() {
        super.onStart();

        Log.e("MainActivity ", "onStart()");


    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.e("MainActivity ", "onStop()");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void cancelAlarm() {
        // If the alarm has been set, cancel it.
        try {
            if (alarm != null) {
                alarm.cancel(pIntent);
            }
            if(switchCompat.isChecked()){
                switchCompat.setChecked(false);
            }

        } catch (Exception e) {

        } finally {
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_LONG).show();
        }


        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = this.getPackageManager();

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
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity ", "onResume()");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String lat_long = sharedPref.getString("city_name", "30.044420/31.235712");

        latitude = Double.parseDouble(lat_long.substring(0, lat_long.indexOf('/')));
        longitude = Double.parseDouble(lat_long.substring(lat_long.indexOf('/') + 1));

        initViewData(geoCal);
        if (!mReceiverIsRegistered) {
            registerReceiver(mReceiver, new IntentFilter(MainActivity.UPDATE_MESSAGE));
            mReceiverIsRegistered = true;
        }
        updateUIData();
    }

    private String getCityName(String lat_long_data) {
        String city = "";
        switch (lat_long_data) {
            case "30.044420/31.235712":
                city = "Cairo";
                break;
            case "31.200092/29.918739":
                city = "Alexandria";
                break;
            case "24.088938/32.899829":
                city = "Aswan";
                break;
            case "26.155061/32.716012":
                city = "Qena";
                break;
            case "30.600314/32.267365":
                city = "Mahala";
                break;
            case "27.178312/31.185926":
                city = "Asyut";
                break;
            case "30.013056/31.208853":
                city = "Giza";
                break;
            case "29.848319/31.336853":
                city = "Helwan";
                break;
            case "29.308402/30.842850":
                city = "Faiyum";

                break;
            case "31.040948/31.040948":
                city = "Mansura";
                break;

        }
        return city;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("MainActivity ", "onRestart()");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("MainActivity ", "onPause()");
        if (mReceiverIsRegistered) {
            unregisterReceiver(mReceiver);
            mReceiverIsRegistered = false;
        }

    }

    @Override
    public void setTime(int hours, int minuts, boolean am_pm) {

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minuts);
        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));

        formatClock(hours, minuts, am_pm);
        // Construct an intent that will execute the AlarmReceiver
        intent = new Intent(MainActivity.this, AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        pIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()
               , pIntent);

        if(!switchCompat.isChecked()){
            switchCompat.setChecked(true);
        }




    }


    private void formatClock(int hours, int minuts, boolean am_pm) {


        String am_pmFormat;
        String hourFormat;
        String minuteFormat;
        if (am_pm) {
            am_pmFormat = "PM";
        } else {
            am_pmFormat = "AM";
        }

        if (hours % 12 == 0) {
            hourFormat = "12";
        } else {
            hours = hours % 12;
            if (hours < 10) {
                hourFormat = "0" + hours;
            } else {
                hourFormat = hours + "";
            }
        }

        if (minuts < 10) {
            minuteFormat = "0" + minuts;
        } else {
            minuteFormat = minuts + "";
        }

        clock.setText(hourFormat + ":" + minuteFormat + " " + am_pmFormat);
        Toast.makeText(this, "Alarm set " + clock.getText().toString(), Toast.LENGTH_LONG).show();
        writeToSharedPref(hourFormat, minuteFormat, am_pmFormat, hours, minuts);
    }

    private void writeToSharedPref(String hourFormat, String minuteFormat, String am_pmFormat, int hours, int minutes) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("formatKey", hourFormat + ":" + minuteFormat + " " + am_pmFormat);
        editor.putInt("hours", hours);
        editor.putInt("minutes", minutes);
        editor.apply();


    }


}