package com.example.mohammed.myapplication;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by mohammed on 8/17/16.
 */
public class Custom_Athan_pref extends DialogPreference {



    public Custom_Athan_pref(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.azan_ringtone_custom_pref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

}
