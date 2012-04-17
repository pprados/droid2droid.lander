package org.remoteandroid.apps.ralander;

import org.remoteandroid.control.RemoteControlActions;
import org.remoteandroid.control.RemoteControlServiceBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;

public class KeyCaptureActivity extends Activity {

    private RemoteControlServiceBinder service;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KeyCaptureActivity.this.service = (RemoteControlServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            KeyCaptureActivity.this.service = null;
        }

    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(KeyCaptureActivity.this, "Server disconnected", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_view);
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(RalanderActions.REMOTE_CONTROL_SERVICE), serviceConnection, 0);
        registerReceiver(stopReceiver, new IntentFilter(RemoteControlActions.STOP_CAPTURE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        unregisterReceiver(stopReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(keyCode, event);
        if (mustSendEvent(event) && service != null) {
            service.sendEvent(event);
        }
        return result;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = super.onKeyUp(keyCode, event);
        if (mustSendEvent(event) && service != null) {
            service.sendEvent(event);
        }
        return result;
    }

    private static boolean mustSendEvent(KeyEvent event) {
        int code = event.getKeyCode();
        return code != KeyEvent.KEYCODE_BACK && code != KeyEvent.KEYCODE_HOME
                && code != KeyEvent.KEYCODE_MENU && code != KeyEvent.KEYCODE_SEARCH;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (service != null) {
            service.stopCaptureByClient();
        }
    }

}
