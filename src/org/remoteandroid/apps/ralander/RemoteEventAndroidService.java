package org.remoteandroid.apps.ralander;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteEventAndroidService extends Service {

    public class RemoteEventServiceImpl extends RemoteEventService.Stub {

        private List<RemoteEvent> pendingEvents = new ArrayList<RemoteEvent>();

        private boolean stopped;

        @Override
        public synchronized List<?> getEvents(boolean start) throws RemoteException {
            if (start) {
                Intent intent = new Intent(RemoteEventAndroidService.this, KeyCaptureActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopped = false;
            }
            try {
                while (pendingEvents.isEmpty() && !stopped) {
                    wait();
                }
                if (stopped) {
                    return null;
                }
                List<RemoteEvent> result = pendingEvents;
                pendingEvents = new ArrayList<RemoteEvent>();
                return result;

            } catch (InterruptedException e) {
                // should never happen
                return null;
            }
        }

        public synchronized void stopCapture() {
            stopped = true;
            notify();
        }

        public synchronized void sendEvent(KeyEvent event) {
            pendingEvents.add(toRemoteEvent(event));
            notify();
        }

    }

    private static RemoteEvent toRemoteEvent(KeyEvent event) {
        return new RemoteEvent(now(), event);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RemoteEventServiceImpl();
    }

}
