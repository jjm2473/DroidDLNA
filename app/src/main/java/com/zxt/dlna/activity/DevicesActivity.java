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
import com.zxt.dlna.dmr.IDeviceList;
import com.zxt.dlna.dmr.RenderService;

import java.util.List;

public class DevicesActivity extends Activity implements RenderService.DeviceListChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int DMR_GET_NO = 0;
    public static final int DMR_GET_SUC = 1;
    private final static String LOGTAG = "DevicesActivity";
    private static boolean serverPrepared = false;

    private IDeviceList iDeviceList;
    private List<String> mDmrList;
    private long exitTime = 0;
    private DevAdapter mDmrDevAdapter;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(LOGTAG, "Service connected");
            iDeviceList = IDeviceList.Stub.asInterface(service);
            try {
                mDmrList = iDeviceList.getList();
                init();
                RenderService.registerListener(DevicesActivity.this, DevicesActivity.this);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(DevicesActivity.this, "Call render service failed!", Toast.LENGTH_LONG).show();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(LOGTAG, "Service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.devices);

        Intent intent = new Intent(this.getApplicationContext(), RenderService.class);
        getApplicationContext().startService(intent);
        getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    private void init() {
        ListView dmrLv = (ListView) findViewById(R.id.renderer_list);

        mDmrDevAdapter = new DevAdapter(DevicesActivity.this, 0, mDmrList);
        dmrLv.setAdapter(mDmrDevAdapter);

        findViewById(R.id.select_renderer_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    iDeviceList.search();
                } catch (RemoteException e) {
                    e.printStackTrace();

                }
            }
        });
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
                try {
                    iDeviceList.search();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case 1: {
                finish();
                System.exit(0);
                break;
            }
        }
        return false;
    }

    @Override
    public void onChange() {
        try {
            List<String> list = iDeviceList.getList();
            mDmrList.clear();
            mDmrList.addAll(list);
            mDmrDevAdapter.notifyDataSetChanged();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Update list failed:Remote Exception!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(SettingActivity.PLAYER_NAME.equals(key)){
            try {
                iDeviceList.updateName(SettingActivity.getRenderName(this));
            } catch (RemoteException e) {
                e.printStackTrace();
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
