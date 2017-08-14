package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.application.BaseApplication;
import com.zxt.dlna.dmp.DeviceItem;
import com.zxt.dlna.dmr.RenderService;
import com.zxt.dlna.util.Utils;

import java.util.List;

public class DevicesActivity extends Activity implements RenderService.DeviceListChangeListener {

    public static final int DMR_GET_NO = 0;
    public static final int DMR_GET_SUC = 1;
    private final static String LOGTAG = "DevicesActivity";
    private static boolean serverPrepared = false;

    private List<DeviceItem> mDmrList;
    private long exitTime = 0;
    private DevAdapter mDmrDevAdapter;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            RenderService.DeviceListService deviceListService = (RenderService.DeviceListService) service;
            mDmrList = deviceListService.getDeviceList();
            init();
            deviceListService.registerListener(DevicesActivity.this, DevicesActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.devices);

        Intent intent = new Intent(this.getApplicationContext(), RenderService.class);
        getApplicationContext().startService(intent);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void init() {

        ListView dmrLv = (ListView) findViewById(R.id.renderer_list);

        mDmrDevAdapter = new DevAdapter(DevicesActivity.this, 0, mDmrList);
        dmrLv.setAdapter(mDmrDevAdapter);
        dmrLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (null != mDmrList && mDmrList.size() > 0) {
                    if (null != mDmrList.get(arg2).getDevice()
                            && null != BaseApplication.deviceItem
                            && null != mDmrList.get(arg2).getDevice().getDetails().getModelDetails()
                            && Utils.DMR_NAME.equals(mDmrList.get(arg2)
                            .getDevice().getDetails().getModelDetails()
                            .getModelName())
                            && Utils.getDevName(
                            mDmrList.get(arg2).getDevice().getDetails()
                                    .getFriendlyName()).equals(
                            Utils.getDevName(BaseApplication.deviceItem
                                    .getDevice().getDetails()
                                    .getFriendlyName()))) {
                        BaseApplication.isLocalDmr = true;
                    } else {
                        BaseApplication.isLocalDmr = false;
                    }
                    BaseApplication.dmrDeviceItem = mDmrList.get(arg2);
                    mDmrDevAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(serviceConnection);
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
                //searchNetwork();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.exit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onChange() {
        mDmrDevAdapter.notifyDataSetChanged();
    }

    class DevAdapter extends ArrayAdapter<DeviceItem> {

        private static final String TAG = "DeviceAdapter";
        public int dmrPosition = 0;
        private LayoutInflater mInflater;
        private List<DeviceItem> deviceItems;

        public DevAdapter(Context context, int textViewResourceId, List<DeviceItem> objects) {
            super(context, textViewResourceId, objects);
            this.mInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            this.deviceItems = objects;
        }

        public int getCount() {
            return this.deviceItems.size();
        }

        public DeviceItem getItem(int paramInt) {
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

            DeviceItem item = (DeviceItem) this.deviceItems.get(position);
            holder.filename.setText(item.toString());
            return view;
        }

        public final class DevHolder {
            public TextView filename;
        }

    }
}
