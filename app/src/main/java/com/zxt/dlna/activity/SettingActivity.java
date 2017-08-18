package com.zxt.dlna.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.zxt.dlna.R;
import com.zxt.dlna.util.PreferenceHead;

public class SettingActivity extends PreferenceActivity {
    public static final String RENDER_STATUS = "dmr_status";
    public static final String PLAYER_NAME = "player_name";

    public static boolean getRenderOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean(RENDER_STATUS, true);
    }

    public static String getRenderName(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(PLAYER_NAME,
                context.getString(R.string.player_name_local));
    }

    public static boolean getDmsOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("dms_status", true);
    }

    public static String getDeviceName(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString("dms_name",
                context.getString(R.string.device_local));
    }

    public static int getSlideTime(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("image_slide_time", "5"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        addTitleBar();
    }

    private void addTitleBar() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        PreferenceHead ph = new PreferenceHead(this);
        ph.setOrder(0);
        preferenceScreen.addPreference(ph);
    }

    private void setLayoutResource(Preference preference) {
        if (preference instanceof PreferenceScreen) {
            PreferenceScreen ps = (PreferenceScreen) preference;
            ps.setLayoutResource(R.layout.preference_screen);
            int cnt = ps.getPreferenceCount();
            for (int i = 0; i < cnt; ++i) {
                Preference p = ps.getPreference(i);
                setLayoutResource(p);
            }
        } else if (preference instanceof PreferenceCategory) {
            PreferenceCategory pc = (PreferenceCategory) preference;
            pc.setLayoutResource(R.layout.preference_category);
            int cnt = pc.getPreferenceCount();
            for (int i = 0; i < cnt; ++i) {
                Preference p = pc.getPreference(i);
                setLayoutResource(p);
            }
        } else {
            preference.setLayoutResource(R.layout.preference);
        }
    }


}
