package com.zxt.dlna.dmr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by lantern on 2017/8/19.
 */

public class RenderServiceStarter {
    private Context mContext;
    private ServiceConnectionImpl serviceConnection;
    private Callback callback;
    private IRenderService iRenderService;
    private volatile boolean binding;
    private Boolean startDev;
    private String setName;

    public RenderServiceStarter(Context mContext, Callback callback) {
        this.mContext = mContext;
        this.callback = callback;
        binding = false;
        serviceConnection = new ServiceConnectionImpl();
    }

    public synchronized void start() {
        setNextState(true);
    }

    public synchronized void stop() {
        setNextState(false);
    }

    public synchronized void setDevName(String name){
        if(iRenderService != null){
            try {
                iRenderService.updateName(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            setName = name;
            bind();
        }
    }

    private void setNextState(boolean start){
        if(iRenderService != null){
            try {
                if(start){
                    iRenderService.start();
                }else{
                    iRenderService.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            startDev = start;
            bind();
        }
    }

    public synchronized void bind(){
        if(!binding) {
            Intent intent = new Intent(mContext.getApplicationContext(), RenderService.class);
            mContext.getApplicationContext().startService(intent);
            mContext.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            binding = true;
        }
    }

    public synchronized void unbind(){
        if(binding) {
            mContext.getApplicationContext().unbindService(serviceConnection);
            binding = false;
        }
    }

    private class ServiceConnectionImpl implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            iRenderService = IRenderService.Stub.asInterface(service);
            try {
                if(startDev != null) {
                    if (startDev) {
                        iRenderService.start();
                    } else {
                        iRenderService.stop();
                    }
                    startDev = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                if (setName != null) {
                    iRenderService.updateName(setName);
                    setName = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (callback != null) {
                callback.onServiceConnected(iRenderService);
            } else {
                unbind();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            iRenderService = null;
            if (callback != null) {
                callback.onServiceDisconnected();
            }
        }
    }

    public static interface Callback {
        public void onServiceConnected(IRenderService iRenderService);
        public void onServiceDisconnected();
    }
}
