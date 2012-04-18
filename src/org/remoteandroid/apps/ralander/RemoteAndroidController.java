package org.remoteandroid.apps.ralander;

import java.util.ArrayList;
import java.util.List;

import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.RemoteAndroid;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.poc.RA;
import org.remoteandroid.util.BindRemoteAndroidHelper;
import org.remoteandroid.util.BindServiceHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class RemoteAndroidController {

    public interface RemoteAndroidListener {
        void discovered();
        void connected(IBinder service);
        void disconnected();
    }
    
    private IBinder remoteService;

    private ComponentName componentName;

    private RemoteAndroidManager remoteAndroidManager;

    private DiscoverListener discoverListener = new DiscoverListener() {

        @Override
        public void onDiscoverStop() {}

        @Override
        public void onDiscoverStart() {}

        @Override
        public void onDiscover(RemoteAndroidInfo remoteAndroidInfo, boolean update) {
            // BindServiceHelper.autobind(remoteAndroidManager, remoteAndroidInfo.getUris(),
            // serviceConnection);
            discovered();
            BindRemoteAndroidHelper.autobind(remoteAndroidManager, remoteAndroidInfo.getUris(),
                    remoteAndroidConnection);
        }
    };

    private ServiceConnection remoteAndroidConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnected();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RemoteAndroid remoteAndroid = (RemoteAndroid) service;
            Intent intent = new Intent(RalanderActions.REMOTE_CONTROL_SERVICE);
            remoteAndroid.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = service;
            componentName = name;
            connected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnected();
            remoteService = null;
            componentName = null;
        }

    };

    private List<RemoteAndroidListener> listeners = new ArrayList<RemoteAndroidListener>();

    private Context context;

    public RemoteAndroidController(Context context) {
        this.context = context;
        remoteAndroidManager = RA.createManager(context);
    }

    public void open() {
        remoteAndroidManager.newDiscoveredAndroid(discoverListener);
    }

    public void connectHardcoded() {
        String uri = "ip://192.168.1.114";
        BindServiceHelper.autobind(remoteAndroidManager, new String[] { uri }, serviceConnection);
    }

    public void close() {
        if (remoteAndroidManager != null) {
            remoteAndroidManager.close();
        }
    }

    public void addRemoteAndroidListener(RemoteAndroidListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Trying to add a listener already added");
        }
        listeners.add(listener);
        if (remoteService != null) {
            // If the service exists, notify it is connected
            listener.connected(remoteService);
        }
    }

    public void removeRemoteAndroidListener(RemoteAndroidListener listener) {
        if (!listeners.contains(listener)) {
            throw new IllegalArgumentException(
                    "Trying to remove a listener not added");
        }
        listeners.remove(listener);
        if (remoteService != null) {
            // If the service exists, notify the disconnection
            listener.disconnected();
        }
    }
    
    private void discovered() {
        for (RemoteAndroidListener listener : listeners) {
            listener.discovered();
        }
    }

    private void connected() {
        for (RemoteAndroidListener listener : listeners) {
            listener.connected(remoteService);
        }
    }

    private void disconnected() {
        for (RemoteAndroidListener listener : listeners) {
            listener.disconnected();
        }
    }

}
