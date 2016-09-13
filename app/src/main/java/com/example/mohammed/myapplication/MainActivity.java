package com.example.mohammed.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity{

    TextView clock;
    AlarmManager alarm;
    PendingIntent pIntent;
    Intent intent;
    GregorianCalendar geoCal;
    double latitude, longitude;

    protected TextView mTextViewCity;
    protected TextView mTextViewDate;
    protected TextView mTextViewHijri;
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

    public void updateUIData() {

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
            next = (GregorianCalendar)nowCal.clone();
            next.add(Calendar.DATE, 1);
            next.set(Calendar.HOUR_OF_DAY, form.getHours(nextPT.get(count)));
            next.set(Calendar.MINUTE, form.getMinutes(nextPT.get(count)));
            next.set(Calendar.SECOND, 0);
        }

        Log.e("Size",prayerTimes.size()+"");
        // prepare alarm message in AR only
        if (i >= prayerTimes.size()) {
            i = 0;
            count=i;
            // Removes "next" from "next fajr" loop from start again
        }

        for (j = 0; j < 3; j++) {
            mTextViewPrayers[count][j].setTypeface(null, Typeface.BOLD);
            mTextViewPrayers[count][j].setTextColor(Color.rgb(0, 0, 255));
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


        geoCal = new GregorianCalendar();
        mTextViewCity = (TextView) findViewById(R.id.textViewCity);
        mTextViewHijri = (TextView) findViewById(R.id.textViewHijriData);
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



}