package com.zxt.dlna.dmr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zxt.dlna.activity.SettingActivity;

/**
 * Created by lantern on 2017/8/19.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(SettingActivity.getRenderOn(context.getApplicationContext())){
            new RenderServiceStarter(context, null).start();
        }
    }
}
