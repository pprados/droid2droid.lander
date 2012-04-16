package org.remoteandroid.apps.ralander;

import org.remoteandroid.apps.ralander.RemoteEventReceiver.RemoteEventListener;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private View lunarView;

    private RemoteAndroidController remoteAndroidController;
    private RemoteEventReceiver remoteEventReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemoteLunarLanderActivity.this) {
                RemoteEventService remoteService = RemoteEventService.Stub.asInterface(service);
                remoteEventReceiver = new RemoteEventReceiver(remoteService, remoteEventListener,
                        new Handler());
                remoteEventReceiver.start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (RemoteLunarLanderActivity.this) {
                if (remoteEventReceiver != null) {
                    remoteEventReceiver.stop();
                    remoteEventReceiver = null;
                }
            }
        }

    };

    private RemoteEventListener remoteEventListener = new RemoteEventListener() {

        @Override
        public void onStoppedByClient() {
            finish();
        }

        @Override
        public void onRemoteEvent(RemoteEvent remoteEvent) {
            KeyEvent event = remoteEvent.getKeyEvent();
            int keyCode = event.getKeyCode();
            switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                lunarView.onKeyDown(keyCode, event);
                break;
            case KeyEvent.ACTION_UP:
                lunarView.onKeyUp(keyCode, event);
                break;
            }
        }
    };

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
        remoteAndroidController.bindRemoteService(serviceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteAndroidController.unbindRemoteService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // synchronized (this) {
        // if (remoteService != null) {
        // stopService(new Intent(RalanderActions.REMOTE_EVENT_SERVICE));
        // }
        // }
    }

}
