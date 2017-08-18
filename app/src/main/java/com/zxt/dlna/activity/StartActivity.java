package com.zxt.dlna.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.zxt.dlna.R;

public class StartActivity extends Activity {

    private Handler mHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            jumpToMain();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_lay);
        mHandle.sendEmptyMessageDelayed(0, 200);
    }

    private void jumpToMain() {
        Intent intent = new Intent(StartActivity.this, IndexActivity.class);
        startActivity(intent);
        this.finish();
    }
}
