package com.example.mohammed.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by mohammed on 5/29/16.
 */
public class AlarmService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    MediaPlayer audioPlayer = null;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MainActivity.TAG, "Audio service created!");


    }


    private void initMediaPlayer() {
        if (null == audioPlayer) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultSound="android.resource://" + getPackageName() + "/" + R.raw.athan;
            String path = preferences.getString("athan_sound", defaultSound);
            switch (path) {
                case "1":
                    path = "android.resource://" + getPackageName() + "/" + R.raw.athan;
                    break;
                case "2":
                    path = "android.resource://" + getPackageName() + "/" + R.raw.abd_el_basset;
                    break;
                case "3":
                    path = "android.resource://" + getPackageName() + "/" + R.raw.el_haram;
                    break;
            }
            Log.e("azan",path);
            audioPlayer = new MediaPlayer();
            try {
                audioPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(MainActivity.TAG, e.getMessage(), e);
            }
            audioPlayer.setOnPreparedListener(this);
            audioPlayer.setOnErrorListener(this);
            audioPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            audioPlayer.prepareAsync(); // prepare async to not block main thread
            Log.d(MainActivity.TAG, "Audio player started asynchronously!");
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        initMediaPlayer();
        return Service.START_STICKY;
    }

    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        audioPlayer.start();
        Log.d(MainActivity.TAG, "Audio started playing!");
        if (!audioPlayer.isPlaying()) {
            Log.d(MainActivity.TAG, "Problem in playing audio");
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        Log.e(MainActivity.TAG, "what=" + what + " extra=" + extra);
        return false; // TODO change to true if error is handed by this fnct.
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (audioPlayer == null) initMediaPlayer();
                else if (!audioPlayer.isPlaying()) audioPlayer.start();
                audioPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (audioPlayer.isPlaying()) audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (audioPlayer.isPlaying()) audioPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioPlayer.isPlaying()) audioPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public void onStop() {
        if (audioPlayer != null) {
            if (audioPlayer.isPlaying()) audioPlayer.stop();
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    public void onPause() {
        if (audioPlayer.isPlaying()) audioPlayer.stop();
    }

    public void onDestroy() {
        onStop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}