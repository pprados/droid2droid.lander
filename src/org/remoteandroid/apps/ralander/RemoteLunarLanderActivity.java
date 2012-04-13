package org.remoteandroid.apps.ralander;

import java.util.List;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private RemoteAndroidController remoteAndroidController;
    private RemoteEventService remoteService;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemoteLunarLanderActivity.this) {
                remoteService = RemoteEventService.Stub.asInterface(service);
                new Thread(new RemoteReceiver()).start();
                new Thread(new EventPlayer()).start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (RemoteLunarLanderActivity.this) {
                remoteService = null;
            }
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    private synchronized boolean isConnected() {
        return remoteService != null;
    }

    private class RemoteReceiver implements Runnable {

        @Override
        public void run() {
            // TODO use handler for uithread
            while (isConnected()) {
                try {
                    boolean start = true;
                    List<?> events = remoteService.getEvents(start);
                    start = false;
                    // TODO
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }
        }
    }

    private class EventPlayer implements Runnable {
        @Override
        public void run() {
            // TODO
        }
    }

}
