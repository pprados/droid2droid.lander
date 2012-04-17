package org.remoteandroid.poc;

import java.util.ArrayList;
import java.util.List;

import org.remoteandroid.ListRemoteAndroidInfo;
import org.remoteandroid.ListRemoteAndroidInfo.DiscoverListener;
import org.remoteandroid.internal.ListRemoteAndroidInfoImpl;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NdefMessage;
import android.os.Parcelable;

public class RA {

    private static final int BIND_TIMEOUT = 10000;

    private RA() {}

    public static RemoteAndroidManager createManager(Context context) {
        return new RemoteAndroidManagerProxy(context);
    }

    private static class RemoteAndroidManagerProxy extends RemoteAndroidManager {

        private Context context;

        private RemoteAndroidManager delegate;

        private boolean bindRequested;

        private List<Runnable> pendingCalls = new ArrayList<Runnable>();

        private ManagerListener managerListener = new ManagerListener() {

            @Override
            public void bind(RemoteAndroidManager manager) {
                synchronized (RemoteAndroidManagerProxy.this) {
                    delegate = manager;
                    for (Runnable runnable : pendingCalls) {
                        runnable.run();
                    }
                    pendingCalls.clear();
                    RemoteAndroidManagerProxy.this.notifyAll();
                }
            }

            @Override
            public void unbind(RemoteAndroidManager manager) {
                synchronized (RemoteAndroidManagerProxy.this) {
                    delegate = null;
                }
            }

        };

        RemoteAndroidManagerProxy(Context context) {
            this.context = context;
        }

        private void bindDelegate() {
            if (!bindRequested) {
                RemoteAndroidManager.bindManager(context, managerListener);
                bindRequested = true;
            }
        }

        @Override
        public synchronized boolean bindRemoteAndroid(final Intent service,
                final ServiceConnection conn, final int flags) {
            if (delegate != null) {
                return delegate.bindRemoteAndroid(service, conn, flags);
            }
            bindDelegate();
            pendingCalls.add(new Runnable() {
                @Override
                public void run() {
                    delegate.bindRemoteAndroid(service, conn, flags);
                }
            });
            // TODO return what in that case ?
            return false;
        }

        @Override
        public ListRemoteAndroidInfo newDiscoveredAndroid(DiscoverListener callback) {
            // TODO find a better way, here it just ignores the delegate ; and add a permission
            // check
            return new ListRemoteAndroidInfoImpl(this, callback);
        }

        @Override
        public void startDiscover(int flags, long timeToDiscover) {
            // TODO Auto-generated method stub

        }

        @Override
        public void cancelDiscover() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isDiscovering() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub
        }

        @Override
        public NdefMessage createNdefMessage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public synchronized ListRemoteAndroidInfo getBoundedDevices() {
            bindDelegate();
            if (delegate == null) {
                try {
                    wait(BIND_TIMEOUT);
                } catch (InterruptedException e) {}
                if (delegate == null) {
                    // TODO create a dedicated exception
                    throw new RuntimeException("Cannot bind the RemoteAndroidManager");
                }

            }
            return delegate.getBoundedDevices();
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public RemoteAndroidInfo getInfos() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getVersion() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public RemoteAndroidInfo parseNfcRawMessages(Context context, Parcelable[] rawMessage) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        @Deprecated
        public void setLog(int type, boolean state) {
            // TODO Auto-generated method stub
            // deprecated
        }

    }

}
