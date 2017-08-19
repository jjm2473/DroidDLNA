/*
 * Copyright (C) 2014 zxt
 * RenderService.java
 * Description:
 * Author: zxt
 * Date:  2014-1-23 上午10:30:58
 */

package com.zxt.dlna.dmr;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zxt.dlna.Manifest;
import com.zxt.dlna.R;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.util.FixedAndroidHandler;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.seamless.util.logging.LoggingUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderService extends Service {
    private final static String LOGTAG = "RenderService";
    private static final String DEVICE_STATE_CHANGE_ACTION = "com.zxt.dlna.dmr.DEVICE_STATE_CHANGE_ACTION";
    private static final String EXTRA_TYPE = "type";
    private static final int TYPE_NAME = 1;
    private static final int TYPE_STATE = 2;
    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    private static enum State {
        INIT(0),
        //UPNPCONNECTED(1),
        STARTING(2),
        RUNNING(3),
        //UPNPDISCONNECTED(4),
        STOPPING(5),
        STOPPED(6);

        public final int v;

        State(int v){
            this.v = v;
        }
    }

    private AndroidUpnpService upnpService;
    private ZxtMediaRenderer mediaRenderer;
    private DeviceItem localDevice;
    private State state;
    private boolean renderOn = false;
    private Thread eventFireThread = null;

    IRenderService.Stub binder = new IRenderService.Stub() {

        @Override
        public String getDeviceName() throws RemoteException {
            return localDevice.toString();
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return state == State.RUNNING;
        }

        @Override
        public void start() throws RemoteException {
            renderOn = true;
            startLocalDevice();
        }

        @Override
        public void stop() throws RemoteException {
            renderOn = false;
            stopLocalDevice();
        }

        @Override
        public void updateName(String name) throws RemoteException {
            updateLocalDeviceName(name);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.v(LOGTAG, "Connected to UPnP Service");
            upnpService = (AndroidUpnpService) service;
            //state = State.UPNPCONNECTED;

            if (renderOn) {
                startLocalDevice();
            }
            // Getting ready for future device advertisements
            //upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            //upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
            //state = State.UPNPDISCONNECTED;
            stopLocalDevice();
        }
    };

    @Override
    public void onCreate() {
        Log.i(LOGTAG, "onCreate");

        // Fix the logging integration between java.util.logging and Android
        // internal logging
        LoggingUtil.resetRootHandler(new FixedAndroidHandler());
        Logger.getLogger("org.teleal.cling").setLevel(Level.INFO);

        mediaRenderer = new ZxtMediaRenderer(1, RenderService.this);
        localDevice = new DeviceItem(mediaRenderer.getDevice());

        state = State.INIT;

        this.bindService(new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        Log.i(LOGTAG, "onDestroy");
        super.onDestroy();
        this.unbindService(serviceConnection);
        stopLocalDevice();
        if (upnpService != null) {
            upnpService = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    protected void searchNetwork() {
        if (upnpService == null)
            return;
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    private void startLocalDevice(){
        synchronized (State.class) {
            if (upnpService != null && state != State.RUNNING) {
                state = State.STARTING;
                upnpService.getRegistry().resume();
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());
                startEventFire();

                Notification notification = new Notification.Builder(this.getApplicationContext()).setSmallIcon(R.drawable.icon_media_play).setContentTitle("DroidDLNA").setContentText("Running").build();//.setFlag(Notification.FLAG_FOREGROUND_SERVICE, true);
                notification.flags = Notification.FLAG_FOREGROUND_SERVICE|Notification.FLAG_NO_CLEAR;
                startForeground(0x112, notification);
            }
        }
    }

    private void stopLocalDevice() {
        synchronized (State.class) {
            if (state.v >= State.RUNNING.v && state.v < State.STOPPED.v) {
                stopEventFire();
                if (upnpService != null) {
                    upnpService.getRegistry().removeDevice(mediaRenderer.getDevice());
                    upnpService.getRegistry().pause();
                }
            }
        }
        stopForeground(true);
    }

    private void startEventFire(){
        synchronized (State.class) {
            if (eventFireThread == null && state == State.STARTING) {
                state = RenderService.State.RUNNING;
                notifyStateChange();
                eventFireThread = new Thread() {
                    @Override
                    public void run() {
                        while (state == RenderService.State.RUNNING) {
                            mediaRenderer.fireLastChange();
                            try {
                                Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        synchronized (RenderService.State.class) {
                            state = RenderService.State.STOPPED;
                            RenderService.State.class.notifyAll();
                        }
                        notifyStateChange();
                    }
                };
                eventFireThread.start();
                try {
                    State.class.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopEventFire(){
        if (eventFireThread != null && eventFireThread.isAlive()) {
            synchronized (State.class) {
                if (state.v < State.STOPPING.v) {
                    state = State.STOPPING;
                }
                eventFireThread.interrupt();
                try {
                    State.class.wait(100);
                    eventFireThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                eventFireThread = null;
            }
        }
    }

    private void updateLocalDeviceName(String name) {
        if(mediaRenderer != null){
            mediaRenderer.updateName(name);
            notifyNameChange();
        }
    }

    private void notifyNameChange(){
        notifyChange(TYPE_NAME);
    }
    private void notifyStateChange(){
        notifyChange(TYPE_STATE);
    }
    private void notifyChange(int type){
        Intent intent = new Intent(DEVICE_STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_TYPE, type);
        RenderService.this.sendBroadcast(intent, Manifest.permission.INTERNAL);
    }

    public static interface DeviceListChangeListener{
        void onNameChange();
        void onStateChange();
    }

    public static void registerListener(Context context, DeviceListChangeListener listener) {
        IntentFilter filter = new IntentFilter(DEVICE_STATE_CHANGE_ACTION);
        context.registerReceiver(new DeviceListChangeReceiver(listener), filter, Manifest.permission.INTERNAL, null);
    }

    private static class DeviceListChangeReceiver extends BroadcastReceiver {
        DeviceListChangeListener listener;

        public DeviceListChangeReceiver(DeviceListChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(EXTRA_TYPE, 0);
            switch (type){
                case TYPE_NAME:
                    listener.onNameChange();
                    break;
                case TYPE_STATE:
                    listener.onStateChange();
                    break;
            }
        }
    }
}
