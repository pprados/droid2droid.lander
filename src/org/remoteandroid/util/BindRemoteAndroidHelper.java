package org.remoteandroid.util;

import org.remoteandroid.RemoteAndroid;
import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.apps.ralander.RalanderActions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

public class BindRemoteAndroidHelper {
    
    private RemoteAndroidManager manager;
    private String[] uris;
    private ServiceConnection delegate;
    private int index;

    private boolean connectionSuccessful;

    private ServiceConnection remoteAndroidConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            next(name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionSuccessful = true;
            delegate.onServiceConnected(name, service);
        }
    };

  
    public static void autobind(RemoteAndroidManager manager, String[] uris, ServiceConnection conn) {
        new BindRemoteAndroidHelper(manager, uris, conn).bind();
    }

    private BindRemoteAndroidHelper(RemoteAndroidManager manager, String[] uris, ServiceConnection conn) {
        this.manager = manager;
        this.uris = uris;
        this.delegate = conn;
    }

    private void bind() {
        next(null);
    }

    private void next(ComponentName name) {
        if (!connectionSuccessful && index < uris.length) {
            connect(uris[index]);
            index++;
        } else {
            delegate.onServiceDisconnected(name);
        }
    }

    private void connect(String uri) {
        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(uri));
        manager.bindRemoteAndroid(intent, remoteAndroidConnection, RemoteAndroidManager.FLAG_PROPOSE_PAIRING);
    }

}
