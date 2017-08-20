package com.zxt.dlna.dmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.zxt.dlna.Manifest;
import com.zxt.dlna.util.Action;

public class GPlayer extends Player {
    private static final String EXTRA_STATUS = "status";
    private static final String EXTRA_VALUE = "value";
    private static enum Status{
        START,
        PAUSE,
        STOP,
        EOM,
        PC,
        DC
    }

    public static void setMediaListener(Context context, MediaListener mediaListener) {
        IntentFilter filter = new IntentFilter(Action.VIDEO_PLAY);
        context.registerReceiver(new StatusBroadcastReceiver(mediaListener), filter, Manifest.permission.INTERNAL, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMediaListener0(new MediaListener() {
            @Override
            public void pause() {
                sendStatusBroadcast(Status.PAUSE, 0);
            }

            @Override
            public void start() {
                sendStatusBroadcast(Status.START, 0);
            }

            @Override
            public void stop() {
                sendStatusBroadcast(Status.STOP, 0);
            }

            @Override
            public void endOfMedia() {
                sendStatusBroadcast(Status.EOM, 0);
            }

            @Override
            public void positionChanged(int position) {
                sendStatusBroadcast(Status.PC, position);
            }

            @Override
            public void durationChanged(int duration) {
                sendStatusBroadcast(Status.DC, duration);
            }
        });
        super.onCreate(savedInstanceState);

        registerBrocast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBrocast();
    }

    private PlayBrocastReceiver playRecevieBrocast = new PlayBrocastReceiver();

    public void registerBrocast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.DMR);
        registerReceiver(this.playRecevieBrocast, intentFilter);
    }

    public void unregisterBrocast() {
        unregisterReceiver(this.playRecevieBrocast);
    }

    private void sendStatusBroadcast(Status status, int value){
        Intent intent = new Intent(Action.VIDEO_PLAY);
        intent.putExtra(EXTRA_STATUS, status.name());
        intent.putExtra(EXTRA_VALUE, value);
        this.sendBroadcast(intent);
    }

    public static class StatusBroadcastReceiver extends BroadcastReceiver{
        MediaListener mMediaListener;

        public StatusBroadcastReceiver(MediaListener mMediaListener) {
            this.mMediaListener = mMediaListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Status status = Status.valueOf(intent.getStringExtra(EXTRA_STATUS));
            if(status != null){
                switch (status){
                    case START:
                        mMediaListener.start();
                        break;
                    case PAUSE:
                        mMediaListener.pause();
                        break;
                    case STOP:
                        mMediaListener.stop();
                        break;
                    case EOM:
                        mMediaListener.endOfMedia();
                        break;
                    case PC:
                        mMediaListener.positionChanged(intent.getIntExtra(EXTRA_VALUE, 0));
                        break;
                    case DC:
                        mMediaListener.durationChanged(intent.getIntExtra(EXTRA_VALUE, 0));
                        break;
                }
            }
        }
    }
}
