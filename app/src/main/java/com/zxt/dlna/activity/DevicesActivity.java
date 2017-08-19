package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.dmr.IRenderService;
import com.zxt.dlna.dmr.RenderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DevicesActivity extends Activity implements RenderService.DeviceListChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int DMR_GET_NO = 0;
    public static final int DMR_GET_SUC = 1;
    private final static String LOGTAG = "DevicesActivity";

    private IRenderService iRenderService;
    private List<String> mDmrList;
    private DevAdapter mDmrDevAdapter;
    private long exitTime = 0;
    private String localDevieName = "";
    private boolean localDevieRunning = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(LOGTAG, "Service connected");
            iRenderService = IRenderService.Stub.asInterface(service);
            try {
                localDevieRunning = iRenderService.isRunning();
                localDevieName = iRenderService.getDeviceName();
                init();
                RenderService.registerListener(DevicesActivity.this, DevicesActivity.this);
                if(SettingActivity.getRenderOn(DevicesActivity.this)){
                    iRenderService.start();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(DevicesActivity.this, "Call render service failed!", Toast.LENGTH_LONG).show();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOGTAG, "Service disconnected");
            iRenderService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iRenderService = null;

        setContentView(R.layout.devices);

        Intent intent = new Intent(this.getApplicationContext(), RenderService.class);
        getApplicationContext().startService(intent);
        getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    private void init() {
        ListView dmrLv = (ListView) findViewById(R.id.renderer_list);

        mDmrList = new ArrayList<>(1);
        updateList();
        mDmrDevAdapter = new DevAdapter(DevicesActivity.this, 0, mDmrList);
        dmrLv.setAdapter(mDmrDevAdapter);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        getApplicationContext().unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.search_lan).setIcon(android.R.drawable.ic_menu_search);
        menu.add(0, 1, 0, R.string.menu_exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                break;
            case 1: {
                finish();
                break;
            }
        }
        return false;
    }

    private void updateList(){
        List<String> list = Collections.singletonList(getNs());
        mDmrList.clear();
        mDmrList.addAll(list);
    }

    private String getNs(){
        return localDevieName + " " + (localDevieRunning?"Running":"Stoped");
    }

    @Override
    public void onNameChange() {
        try {
            localDevieName = iRenderService.getDeviceName();
            updateList();
            mDmrDevAdapter.notifyDataSetChanged();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Update list failed:Remote Exception!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStateChange() {
        try {
            localDevieRunning = iRenderService.isRunning();
            updateList();
            mDmrDevAdapter.notifyDataSetChanged();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Update list failed:Remote Exception!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(iRenderService == null){
            return;
        }
        if(SettingActivity.PLAYER_NAME.equals(key)){
            try {
                iRenderService.updateName(SettingActivity.getRenderName(this));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if(SettingActivity.RENDER_STATUS.equals(key)){
            boolean renderOn = SettingActivity.getRenderOn(this);
            if(renderOn != localDevieRunning){
                if(renderOn){
                    try {
                        iRenderService.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        iRenderService.stop();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class DevAdapter extends ArrayAdapter<String> {

        private LayoutInflater mInflater;
        private List<String> deviceItems;

        public DevAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            this.mInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            this.deviceItems = objects;
        }

        public int getCount() {
            return this.deviceItems.size();
        }

        public String getItem(int paramInt) {
            return this.deviceItems.get(paramInt);
        }

        public long getItemId(int paramInt) {
            return paramInt;
        }

        public View getView(int position, View view, ViewGroup viewGroup) {

            DevHolder holder;
            if (view == null) {
                view = this.mInflater.inflate(R.layout.dmr_item, null);
                holder = new DevHolder();
                holder.filename = ((TextView) view.findViewById(R.id.dmr_name_tv));
                view.setTag(holder);
            } else {
                holder = (DevHolder) view.getTag();
            }

            String item = this.deviceItems.get(position);
            holder.filename.setText(item);
            return view;
        }

        public final class DevHolder {
            public TextView filename;
        }

    }
}
