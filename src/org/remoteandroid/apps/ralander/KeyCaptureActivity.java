package org.remoteandroid.apps.ralander;

import org.remoteandroid.apps.ralander.RemoteEventAndroidService.RemoteEventServiceImpl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

public class KeyCaptureActivity extends Activity {

    private RemoteEventServiceImpl service;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KeyCaptureActivity.this.service = (RemoteEventServiceImpl) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            KeyCaptureActivity.this.service = null;
        }

    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_view);
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(RalanderActions.REMOTE_EVENT_SERVICE), serviceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
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

    private boolean mustSendEvent(KeyEvent event) {
        int code = event.getKeyCode();
        return code != KeyEvent.KEYCODE_BACK && code != KeyEvent.KEYCODE_HOME
                && code != KeyEvent.KEYCODE_MENU && code != KeyEvent.KEYCODE_SEARCH;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (service != null) {
            service.stopCapture();
        }
    }

}
