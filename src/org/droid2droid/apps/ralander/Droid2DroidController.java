package org.droid2droid.apps.ralander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.ListRemoteAndroidInfo.DiscoverListener;
import org.droid2droid.RemoteAndroid;
import org.droid2droid.RemoteAndroid.PublishListener;
import org.droid2droid.RemoteAndroidInfo;
import org.droid2droid.internal.RemoteAndroidInfoImpl;
import org.droid2droid.poc.D2D;
import org.droid2droid.util.BindDroid2DroidHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class Droid2DroidController {

    public interface RemoteAndroidListener {
        void discovered();

        void connected(IBinder service);

        void disconnected();

        void pushStarted();

        void pushProgress(int progress);

        void pushFinished(int status);
    }

    public static class RemoteAndroidDefaultListener implements RemoteAndroidListener {

        @Override
        public void discovered() {}

        @Override
        public void connected(IBinder service) {}

        @Override
        public void disconnected() {}

        @Override
        public void pushStarted() {}

        @Override
        public void pushProgress(int progress) {}

        @Override
        public void pushFinished(int status) {}

    }

    private IBinder remoteService;

    private ComponentName componentName;

    private final Droid2DroidManager remoteAndroidManager;
    private RemoteAndroid remoteAndroid;

    private final DiscoverListener discoverListener = new DiscoverListener() {

        @Override
        public void onDiscoverStop() {}

        @Override
        public void onDiscoverStart() {}

        @Override
        public void onDiscover(RemoteAndroidInfo remoteAndroidInfo, boolean update) {
            // BindServiceHelper.autobind(remoteAndroidManager, remoteAndroidInfo.getUris(),
            // serviceConnection);
            discovered();
            BindDroid2DroidHelper.autobind(remoteAndroidManager, remoteAndroidInfo.getUris(),
                    remoteAndroidConnection);
        }
    };

    private final ServiceConnection remoteAndroidConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnected();
            remoteAndroid = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteAndroid = (RemoteAndroid) service;
            int flags = 1; // Where are the constants ?
            int timeout = 30; // seconds
            try {
                remoteAndroid.pushMe(context, publishListener, flags, timeout);
            } catch (RemoteException e) {
                // can never happen, pushMe start a new thread and do nothing else, it should not
                // declare RemoteException
            } catch (IOException e) {
                // same comment
            }
            pushStarted();
        }
    };

    private final PublishListener publishListener = new PublishListener() {

        @Override
        public void onProgress(int progress) {
            pushProgress(progress);
        }

        @Override
        public void onFinish(int status) {
            pushFinished(status);
            if (status >= 0 && remoteAndroid != null) {
                Intent intent = new Intent(RalanderActions.REMOTE_CONTROL_SERVICE);
                remoteAndroid.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            } else {
                Toast.makeText(context, "Install APK failed (status=" + status + ")",
                        Toast.LENGTH_SHORT);
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            onFinish(-1);
        }

        @Override
        public boolean askIsPushApk() {
            return true;
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {

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

    private final List<RemoteAndroidListener> listeners = new ArrayList<RemoteAndroidListener>();

    private final Context context;

    public Droid2DroidController(Context context) {
        this.context = context;
        remoteAndroidManager = D2D.createManager(context);
    }

    public void open() {
        Droid2DroidManager.newDiscoveredAndroid(context, discoverListener);
    }

    public void connectHardcoded() {
        String uri = "ip://192.168.1.117";
        RemoteAndroidInfoImpl info = new RemoteAndroidInfoImpl();
        info.uris = new ArrayList<String>();
        info.uris.add(uri);
        discoverListener.onDiscover(info, false);
        // BindServiceHelper.autobind(remoteAndroidManager, new String[] { uri },
        // serviceConnection);
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
            throw new IllegalArgumentException("Trying to remove a listener not added");
        }
        listeners.remove(listener);
        if (remoteService != null) {
            // If the service exists, notify the disconnection
            listener.disconnected();
        }
        if (listeners.isEmpty() && remoteAndroid != null) {
        	new AsyncTask<Void, Void, Void>()
        	{
        		@Override
        		protected Void doInBackground(Void... params)
        		{
                    remoteAndroid.close();
        			return null;
        		}
        	}.execute();
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

    private void pushStarted() {
        for (RemoteAndroidListener listener : listeners) {
            listener.pushStarted();
        }
    }

    private void pushProgress(int progress) {
        for (RemoteAndroidListener listener : listeners) {
            listener.pushProgress(progress);
        }
    }

    private void pushFinished(int status) {
        for (RemoteAndroidListener listener : listeners) {
            listener.pushFinished(status);
        }
    }

}
