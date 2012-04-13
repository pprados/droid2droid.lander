package org.remoteandroid.apps.ralander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class RemoteEventAndroidService extends Service {

    public class RemoteEventServiceImpl extends RemoteEventService.Stub {

        private List<RemoteEvent> pendingEvents = new ArrayList<RemoteEvent>();

        @Override
        public List<?> getEvents(boolean start) throws RemoteException {
            if (start) {
                Intent intent = new Intent(RemoteEventAndroidService.this, KeyCaptureActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            List<Integer> keyCodes = Arrays.asList(1, 2, 3);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            return keyCodes;
        }

        public void stopCapture() {
            // TODO terminate any current getEvents()
        }

        public void sendEvent(RemoteEvent event) {
            // TODO
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RemoteEventServiceImpl();
    }

}
