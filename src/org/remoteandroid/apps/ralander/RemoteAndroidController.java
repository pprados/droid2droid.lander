package org.remoteandroid.apps.ralander;

import java.util.ArrayList;
import java.util.List;

import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.poc.RA;
import org.remoteandroid.util.BindServiceHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

public class RemoteAndroidController {

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
            BindServiceHelper.autobind(remoteAndroidManager, remoteAndroidInfo.getUris(),
                    serviceConnection);
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

    private List<ServiceConnection> connections = new ArrayList<ServiceConnection>();

    private Context context;

    public RemoteAndroidController(Context context) {
        this.context = context;
        remoteAndroidManager = RA.createManager(context);
    }

    public void open() {
        remoteAndroidManager.newDiscoveredAndroid(discoverListener);
    }

    public void connectHardcoded() {
        String uri = "ip://192.168.0.101";
        BindServiceHelper.autobind(remoteAndroidManager, new String[] { uri }, serviceConnection);
    }

    public void close() {
        if (remoteAndroidManager != null) {
            remoteAndroidManager.close();
        }
    }

    public void bindRemoteService(ServiceConnection serviceConnection) {
        if (connections.contains(serviceConnection)) {
            throw new IllegalArgumentException("Trying to bind a serviceConnection already bound");
        }
        connections.add(serviceConnection);
        if (remoteService != null) {
            // If the service exists, notify it is connected
            serviceConnection.onServiceConnected(componentName, remoteService);
        }
    }

    public void unbindRemoteService(ServiceConnection serviceConnection) {
        if (!connections.contains(serviceConnection)) {
            throw new IllegalArgumentException(
                    "Trying to unbind a serviceConnection which was not bound");
        }
        connections.remove(serviceConnection);
        serviceConnection.onServiceDisconnected(componentName);
    }

    private void connected() {
        for (ServiceConnection connection : connections) {
            connection.onServiceConnected(componentName, remoteService);
        }
    }

    private void disconnected() {
        for (ServiceConnection connection : connections) {
            connection.onServiceDisconnected(componentName);
        }
    }

}
