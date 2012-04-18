package org.remoteandroid.apps.ralander;

import org.remoteandroid.apps.ralander.RemoteAndroidController.RemoteAndroidDefaultListener;
import org.remoteandroid.apps.ralander.RemoteAndroidController.RemoteAndroidListener;
import org.remoteandroid.control.RemoteControlHelper;
import org.remoteandroid.control.RemoteEventReceiver.RemoteEventListener;

import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private View lunarView;

    private RemoteAndroidController remoteAndroidController;
    private boolean stoppedByClient;

    private RemoteAndroidListener remoteAndroidListener = new RemoteAndroidDefaultListener() {

        @Override
        public void discovered() {}

        @Override
        public void disconnected() {
            remoteControlHelper.getServiceConnection().onServiceDisconnected(null);
            if (stoppedByClient) {
                Toast.makeText(RemoteLunarLanderActivity.this, "Client disconnected",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RemoteLunarLanderActivity.this, "Disconnected", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }

        @Override
        public void connected(IBinder service) {
            remoteControlHelper.getServiceConnection().onServiceConnected(null, service);
        }

    };

    private RemoteEventListener remoteEventListener = new RemoteEventListener() {

        @Override
        public void onStoppedByClient() {
            stoppedByClient = true;
            // Toast.makeText(RemoteLunarLanderActivity.this, "Client disconnected",
            // Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onRemoteEvent(Object event) {
            KeyEvent keyEvent = (KeyEvent) event;
            int keyCode = keyEvent.getKeyCode();
            switch (keyEvent.getAction()) {
            case KeyEvent.ACTION_DOWN:
                lunarView.onKeyDown(keyCode, keyEvent);
                break;
            case KeyEvent.ACTION_UP:
                lunarView.onKeyUp(keyCode, keyEvent);
                break;
            }
        }
    };

    private RemoteControlHelper remoteControlHelper = new RemoteControlHelper(remoteEventListener);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lunarView = (View) findViewById(R.id.lunar);

        remoteAndroidController = ((RalanderApplication) getApplication())
                .getRemoteAndroidController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteAndroidController.addRemoteAndroidListener(remoteAndroidListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteAndroidController.removeRemoteAndroidListener(remoteAndroidListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        remoteControlHelper.asyncStopClient();
    }

}
