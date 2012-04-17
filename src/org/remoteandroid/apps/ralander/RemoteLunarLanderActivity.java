package org.remoteandroid.apps.ralander;

import org.remoteandroid.control.RemoteEventReceiver;
import org.remoteandroid.control.RemoteEventReceiver.RemoteEventListener;
import org.remoteandroid.control.RemoteEventService;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private View lunarView;

    private RemoteEventService remoteService;
    private RemoteAndroidController remoteAndroidController;
    private RemoteEventReceiver remoteEventReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemoteLunarLanderActivity.this) {
                remoteService = RemoteEventService.Stub.asInterface(service);
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
                    remoteService = null;
                }
            }
        }

    };

    private RemoteEventListener remoteEventListener = new RemoteEventListener() {

        @Override
        public void onStoppedByClient() {
            Toast.makeText(RemoteLunarLanderActivity.this, "Client disconnected",
                    Toast.LENGTH_SHORT).show();
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
        new Thread(new Runnable() {
            @Override public void run() {
                synchronized (RemoteLunarLanderActivity.this) {
                    if (remoteService != null) {
                        try {
                            remoteEventReceiver.stop();
                            remoteService.stopCapture();
                        } catch (RemoteException e) {}
                    }
                }
            }
        }).start();
        
    }

}
