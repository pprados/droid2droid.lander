package org.droid2droid.util;

import static org.droid2droid.Droid2DroidManager.FLAG_PROPOSE_PAIRING;

import org.droid2droid.Droid2DroidManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

public class BindDroid2DroidHelper {
    
    private final Droid2DroidManager manager;
    private final String[] uris;
    private final ServiceConnection delegate;
    private int index;

    private boolean connectionSuccessful;

    private final ServiceConnection remoteAndroidConnection = new ServiceConnection() {

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

  
    public static void autobind(Droid2DroidManager manager, String[] uris, ServiceConnection conn) {
        new BindDroid2DroidHelper(manager, uris, conn).bind();
    }

    private BindDroid2DroidHelper(Droid2DroidManager manager, String[] uris, ServiceConnection conn) {
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
        manager.bindRemoteAndroid(intent, remoteAndroidConnection, FLAG_PROPOSE_PAIRING);
    }

}
