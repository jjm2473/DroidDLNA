/*
 * RenderPlayerService.java
 * Description:
 * Author: zxt
 */

package com.zxt.dlna.dmr;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.zxt.dlna.dmp.GPlayer;
import com.zxt.dlna.dmp.ImageDisplay;
import com.zxt.dlna.util.Action;

public class RenderPlayerService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_NOT_STICKY;
    }

    public void onStart(Intent intent, int startId) {
        //xgf fix bug null point
        if (null != intent) {
            super.onStart(intent, startId);
            String type = intent.getStringExtra("type");
            Intent intent2;

            if (type.equals(MediaType.AUDIO)) {
                startAvPlayer(intent.getStringExtra("name"), type, intent.getStringExtra("playURI"));
            } else if (type.equals(MediaType.VIDEO)) {
                startAvPlayer(intent.getStringExtra("name"), type, intent.getStringExtra("playURI"));
            } else if (type.equals(MediaType.IMAGE)) {
                intent2 = new Intent(this, ImageDisplay.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("name", intent.getStringExtra("name"));
                intent2.putExtra("playURI", intent.getStringExtra("playURI"));
                intent2.putExtra("isRender", true);
                startActivity(intent2);
            } else {
                intent2 = new Intent(Action.DMR);
                intent2.putExtra("playpath", intent.getStringExtra("playURI"));
                sendBroadcast(intent2);
            }
        }
    }

    private void startAvPlayer(String name, String type, String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri), this, GPlayer.class);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
