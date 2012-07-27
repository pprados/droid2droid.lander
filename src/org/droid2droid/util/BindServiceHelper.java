package org.droid2droid.util;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.RemoteAndroid;
import org.droid2droid.apps.ralander.RalanderActions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
public class BindServiceHelper {

    private final Droid2DroidManager manager;
    private final String[] uris;
    private final ServiceConnection delegateServiceConnection;
    private int index;

    private boolean connectionSuccessful;

    private final ServiceConnection remoteAndroidConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            next(name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RemoteAndroid remoteAndroid = (RemoteAndroid) service;
            Intent intent = new Intent(RalanderActions.REMOTE_CONTROL_SERVICE);
            remoteAndroid.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            next(name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionSuccessful = true;
            delegateServiceConnection.onServiceConnected(name, service);
        }
    };

    public static void autobind(Droid2DroidManager manager, String[] uris, ServiceConnection conn) {
        new BindServiceHelper(manager, uris, conn).bind();
    }

    private BindServiceHelper(Droid2DroidManager manager, String[] uris, ServiceConnection conn) {
        this.manager = manager;
        this.uris = uris;
        this.delegateServiceConnection = conn;
    }

    private void bind() {
        next(null);
    }

    private void next(ComponentName name) {
        if (!connectionSuccessful && index < uris.length) {
            connect(uris[index]);
            index++;
        } else {
            delegateServiceConnection.onServiceDisconnected(name);
        }
    }

    private void connect(String uri) {
        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(uri));
        manager.bindRemoteAndroid(intent, remoteAndroidConnection, 0);
    }

}
