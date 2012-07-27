package org.droid2droid.poc;

import java.util.ArrayList;
import java.util.List;

import org.droid2droid.Droid2DroidManager;
import org.droid2droid.ListRemoteAndroidInfo;
import org.droid2droid.RemoteAndroidInfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NdefMessage;
public class D2D {

    private static final int BIND_TIMEOUT = 10000;

    private D2D() {}

    public static Droid2DroidManager createManager(Context context) {
        return new Droid2DroidManagerProxy(context);
    }

    private static class Droid2DroidManagerProxy extends Droid2DroidManager {

        private final Context context;

        private Droid2DroidManager delegate;

        private boolean bindRequested;

        private final List<Runnable> pendingCalls = new ArrayList<Runnable>();

        private final ManagerListener managerListener = new ManagerListener() {

            @Override
            public void bind(Droid2DroidManager manager) {
                synchronized (Droid2DroidManagerProxy.this) {
                    delegate = manager;
                    for (Runnable runnable : pendingCalls) {
                        runnable.run();
                    }
                    pendingCalls.clear();
                    Droid2DroidManagerProxy.this.notifyAll();
                }
            }

            @Override
            public void unbind(Droid2DroidManager manager) {
                synchronized (Droid2DroidManagerProxy.this) {
                    delegate = null;
                }
            }

        };

        Droid2DroidManagerProxy(Context context) {
            this.context = context;
        }

        private void bindDelegate() {
            if (!bindRequested) {
                Droid2DroidManager.bindManager(context, managerListener);
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

//        @Override
//        public ListRemoteAndroidInfo newDiscoveredAndroid(DiscoverListener callback) {
//            // TODO find a better way, here it just ignores the delegate ; and add a permission
//            // check
//            return new ListRemoteAndroidInfoImpl(this, callback);
//        }

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
        public synchronized ListRemoteAndroidInfo getBondedDevices() {
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
            return delegate.getBondedDevices();
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

//        @Override
//        public RemoteAndroidInfo parseNfcRawMessages(Context context, Parcelable[] rawMessage) {
//            // TODO Auto-generated method stub
//            return null;
//        }

        @Override
        @Deprecated
        public void setLog(int type, boolean state) {
            // TODO Auto-generated method stub
            // deprecated
        }

		@TargetApi(9)
		@Override
		public NdefMessage createNdefMessage()
		{
			// TODO Auto-generated method stub
			return null;
		}

    }

}
