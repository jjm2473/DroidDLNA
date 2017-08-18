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
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zxt.dlna.Manifest;
import com.zxt.dlna.R;
import com.zxt.dlna.activity.SettingActivity;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.util.FixedAndroidHandler;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.seamless.util.logging.LoggingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderService extends Service {
    private final static String LOGTAG = "RenderService";
    private static final String DEVICE_LIST_CHANGE_ACTION = "com.zxt.dlna.dmr.DEVICE_LIST_CHANGE_ACTION";

    private ArrayList<DeviceItem> mDmrList = new ArrayList<>();
    private AndroidUpnpService upnpService;
    private DeviceListRegistryListener deviceListRegistryListener;
    private ZxtMediaRenderer mediaRenderer;

    IDeviceList.Stub binder = new IDeviceList.Stub() {
        @Override
        public List<String> getList() throws RemoteException {
            List<String> devices = new ArrayList<>(mDmrList.size());
            for(DeviceItem d:mDmrList){
                devices.add(d.toString());
            }
            return devices;
        }

        @Override
        public void search() throws RemoteException {
            searchNetwork();
        }

        @Override
        public void updateName(String name) throws RemoteException {
            updateLocalDeviceName(name);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mDmrList.clear();

            upnpService = (AndroidUpnpService) service;
            Log.v(LOGTAG, "Connected to UPnP Service");

            mediaRenderer = new ZxtMediaRenderer(1, RenderService.this);
            if (SettingActivity.getRenderOn(RenderService.this.getApplicationContext())) {
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());
                deviceListRegistryListener.dmrAdded(new DeviceItem(mediaRenderer.getDevice()));
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    public void onCreate() {
        Log.i(LOGTAG, "onCreate");

        // Fix the logging integration between java.util.logging and Android
        // internal logging
        LoggingUtil.resetRootHandler(new FixedAndroidHandler());
        Logger.getLogger("org.teleal.cling").setLevel(Level.INFO);

        deviceListRegistryListener = new DeviceListRegistryListener();

        this.bindService(new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection, Context.BIND_AUTO_CREATE);

        Notification notification = new Notification.Builder(this.getApplicationContext()).setSmallIcon(R.drawable.icon_media_play).setContentTitle("DroidDLNA").setContentText("Running").build();//.setFlag(Notification.FLAG_FOREGROUND_SERVICE, true);
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE|Notification.FLAG_NO_CLEAR;
        startForeground(0x112, notification);
    }

    @Override
    public void onDestroy() {
        Log.i(LOGTAG, "onDestroy");
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(deviceListRegistryListener);
            upnpService = null;
        }
        this.unbindService(serviceConnection);
        stopForeground(true);
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

    private void updateLocalDeviceName(String name) {
        if(mediaRenderer != null){
            mediaRenderer.updateName(name);
            LocalDevice localDevice = mediaRenderer.getDevice();
            UDN localUdn = localDevice.getIdentity().getUdn();
            for(DeviceItem d:mDmrList){
                if(d.getUdn().equals(localUdn)){
                    mDmrList.remove(d);
                    mDmrList.add(new DeviceItem(localDevice));
                    break;
                }
            }
            deviceListRegistryListener.notifyChange();
        }
    }

    public class DeviceListRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        }

		/*
         * End of optimization, you can remove the whole block if your Android
		 * handset is fast (>= 600 Mhz)
		 */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.e("DeviceListRegistryListener", "remoteDeviceAdded:" + device.toString() + " Type: " +device.getType().getType());

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrAdded(dmrDisplay);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.e("DeviceListRegistryListener", "remoteDeviceRemoved:" + device.toString() + " Type: " +device.getType().getType());
            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrRemoved(dmrDisplay);
            }
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.e("DeviceListRegistryListener",
                    "localDeviceAdded:" + device.toString()
                            + " Type: " + device.getType().getType());

        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.e("DeviceListRegistryListener",
                    "localDeviceRemoved:" + device.toString()
                            + " Type: " + device.getType().getType());
        }

        public void dmrAdded(final DeviceItem di) {
            if (!mDmrList.contains(di)) {
                mDmrList.add(di);
            }
            notifyChange();
        }

        public void dmrRemoved(final DeviceItem di) {
            mDmrList.remove(di);
            notifyChange();
        }

        public void notifyChange(){
            RenderService.this.sendBroadcast(new Intent(DEVICE_LIST_CHANGE_ACTION), Manifest.permission.INTERNAL);
        }
    }

    public static interface DeviceListChangeListener{
        void onChange();
    }

    public static void registerListener(Context context, DeviceListChangeListener listener) {
        IntentFilter filter = new IntentFilter(DEVICE_LIST_CHANGE_ACTION);
        context.registerReceiver(new DeviceListChangeReceiver(listener), filter, Manifest.permission.INTERNAL, null);
    }

    private static class DeviceListChangeReceiver extends BroadcastReceiver {
        DeviceListChangeListener listener;

        public DeviceListChangeReceiver(DeviceListChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            listener.onChange();
        }
    }
}
