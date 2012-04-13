package org.remoteandroid.apps.ralander;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

public class KeyCaptureActivity extends Activity {

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {}

        @Override
        public void onServiceDisconnected(ComponentName name) {}

    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, RemoteEventAndroidService.class), serviceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

}
