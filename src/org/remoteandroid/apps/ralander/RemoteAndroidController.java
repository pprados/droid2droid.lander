package org.remoteandroid.apps.ralander;

import java.util.ArrayList;
import java.util.List;

import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.RemoteAndroid;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.RemoteAndroidManager.ManagerListener;
import org.remoteandroid.internal.RemoteAndroidInfoImpl;
import org.remoteandroid.poc.RA;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

public class RemoteAndroidController {

    private IBinder remoteService;

    private ComponentName componentName;

    private RemoteAndroidManager remoteAndroidManager;
    private RemoteAndroid remoteAndroid;

    private boolean onlyOnceHack;

//    private ManagerListener managerListener = new ManagerListener() {
//        @Override
//        public void bind(RemoteAndroidManager manager) {
//            if (onlyOnceHack)
//                return;
//            remoteAndroidManager = manager;
//            // remoteAndroidManager.newDiscoveredAndroid(discoverController);
//            // MOCK
//            final Handler handler = new Handler();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {}
//                    final RemoteAndroidInfoImpl remoteAndroidInfo = new RemoteAndroidInfoImpl();
//                    remoteAndroidInfo.addUris("ip://192.168.0.100");
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            onlyOnceHack = true;
//                            discoverController.onDiscover(remoteAndroidInfo, false);
//                        }
//                    });
//
//                }
//            }).start();
//        }
//
//        @Override
//        public void unbind(RemoteAndroidManager manager) {
//            remoteAndroidManager = null;
//            disconnected();
//        }
//    };
//
//    private DiscoverListener discoverController = new DiscoverListener() {
//
//        @Override
//        public void onDiscover(RemoteAndroidInfo remoteAndroidInfo, boolean update) {
//            // TODO unregister once logged
//            String uri = remoteAndroidInfo.getUris()[0];
//            Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(uri));
//            remoteAndroidManager.bindRemoteAndroid(intent, remoteAndroidConnection, 0);
//        }
//
//        @Override
//        public void onDiscoverStart() {}
//
//        @Override
//        public void onDiscoverStop() {}
//
//    };

    private ServiceConnection remoteAndroidConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteAndroid = (RemoteAndroid) service;
            Intent intent = new Intent(RalanderActions.REMOTE_EVENT_SERVICE);
            remoteAndroid.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteAndroid = null;
            disconnected();
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
    private RemoteAndroidManager manager;

    public RemoteAndroidController(Context context) {
        this.context = context;
        manager = RA.createManager(context);
    }

    public void open() {
        // RemoteAndroidManager.bindManager(context, managerListener);
        String uri = "ip://192.168.0.102";
        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(uri));
        manager.bindRemoteAndroid(intent, remoteAndroidConnection, 0);
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
