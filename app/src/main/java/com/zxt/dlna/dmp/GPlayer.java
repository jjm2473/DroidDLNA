package com.zxt.dlna.dmp;

import android.content.IntentFilter;
import android.os.Bundle;

import com.zxt.dlna.util.Action;

public class GPlayer extends Player {
    private static MediaListener mMediaListener;

    public static void setMediaListener(MediaListener mediaListener) {
        mMediaListener = mediaListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMediaListener0(new MediaListener() {
            @Override
            public void pause() {
                if(mMediaListener != null){
                    mMediaListener.pause();
                }
            }

            @Override
            public void start() {
                if(mMediaListener != null){
                    mMediaListener.start();
                }
            }

            @Override
            public void stop() {
                if(mMediaListener != null){
                    mMediaListener.stop();
                }
            }

            @Override
            public void endOfMedia() {
                if(mMediaListener != null){
                    mMediaListener.endOfMedia();
                }
            }

            @Override
            public void positionChanged(int position) {
                if(mMediaListener != null){
                    mMediaListener.positionChanged(position);
                }
            }

            @Override
            public void durationChanged(int duration) {
                if(mMediaListener != null){
                    mMediaListener.durationChanged(duration);
                }
            }
        });
        super.onCreate(savedInstanceState);

        registerBrocast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBrocast();
        mMediaListener = null;
    }

    private PlayBrocastReceiver playRecevieBrocast = new PlayBrocastReceiver();

    public void registerBrocast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.DMR);
        intentFilter.addAction(Action.VIDEO_PLAY);
        registerReceiver(this.playRecevieBrocast, intentFilter);
    }

    public void unregisterBrocast() {
        unregisterReceiver(this.playRecevieBrocast);
    }
}
