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
            ControlEvent controlEvent = (ControlEvent) event;
            KeyEvent keyEvent = toKeyEvent(controlEvent);
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

    private static KeyEvent toKeyEvent(ControlEvent event) {
        int key;
        int action;
        switch (event.getKey()) {
        case LEFT:
            key = KeyEvent.KEYCODE_DPAD_LEFT;
            break;
        case RIGHT:
            key = KeyEvent.KEYCODE_DPAD_RIGHT;
            break;
        case UP:
            key = KeyEvent.KEYCODE_DPAD_UP;
            break;
        case FIRE:
            key = KeyEvent.KEYCODE_SPACE;
            break;
        default:
            return null;
        }
        switch (event.getAction()) {
        case UP:
            action = KeyEvent.ACTION_UP;
            break;
        case DOWN:
            action = KeyEvent.ACTION_DOWN;
            break;
        default:
            return null;
        }
        return new KeyEvent(action, key);
    }

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
