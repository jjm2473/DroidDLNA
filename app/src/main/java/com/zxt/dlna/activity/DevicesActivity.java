package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.dmr.IRenderService;
import com.zxt.dlna.dmr.RenderService;
import com.zxt.dlna.dmr.RenderServiceStarter;

public class DevicesActivity extends Activity implements RenderService.DeviceListChangeListener {
    private final static String LOGTAG = "DevicesActivity";

    private IRenderService iRenderService;
    private ImageButton serviceSwitch;
    private TextView deviceName;
    private TextView deviceState;
    private String localDevieName = "...";
    private boolean localDevieRunning = false;
    private BroadcastReceiver listenerReceiver;

    private RenderServiceStarter.Callback serviceConnection = new RenderServiceStarter.Callback() {
        public void onServiceConnected(IRenderService service) {
            Log.e(LOGTAG, "Service connected");
            iRenderService = service;
            onNameChange();
            onStateChange();
        }

        public void onServiceDisconnected() {
            Log.e(LOGTAG, "Service disconnected");
            iRenderService = null;
        }
    };
    RenderServiceStarter starter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iRenderService = null;

        setContentView(R.layout.devices);

        initView();

        starter = new RenderServiceStarter(this, serviceConnection);
        if(SettingActivity.getRenderOn(DevicesActivity.this)){
            starter.start();
        }else{
            starter.bind();
        }

        listenerReceiver = RenderService.registerListener(DevicesActivity.this, DevicesActivity.this);
    }

    private void initView() {
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                jumpToSettings();
            }
        });

        deviceName = (TextView) findViewById(R.id.device_name_tv);
        deviceState = (TextView) findViewById(R.id.device_state_tv);

        serviceSwitch = (ImageButton) findViewById(R.id.service_switch);
        updateState();
        serviceSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(iRenderService != null){
                    try {
                        if (localDevieRunning) {
                            iRenderService.stop();
                        } else {
                            iRenderService.start();
                        }
                    } catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(listenerReceiver);
        starter.unbind();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.setting).setIcon(R.drawable.button_setting);
        menu.add(0, 1, 0, R.string.menu_exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: {
                jumpToSettings();
                break;
            }
            case 1: {
                finish();
                break;
            }
        }
        return false;
    }

    private void updateState() {
        deviceState.setText(localDevieRunning?"Running":"Stopped");
        serviceSwitch.setImageResource(localDevieRunning?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play);
    }

    private void updateName() {
        deviceName.setText(localDevieName);
    }

    private void jumpToSettings(){
        Intent intent = new Intent(this, SettingActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onNameChange() {
        try {
            localDevieName = iRenderService.getDeviceName();
            updateName();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Update list failed:Remote Exception!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStateChange() {
        try {
            localDevieRunning = iRenderService.isRunning();
            updateState();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Update list failed:Remote Exception!", Toast.LENGTH_LONG).show();
        }
    }
}
