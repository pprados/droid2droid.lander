package org.remoteandroid.apps.ralander;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private View lunarView;

    private RemoteAndroidController remoteAndroidController;
    private RemoteEventService remoteService;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemoteLunarLanderActivity.this) {
                remoteService = RemoteEventService.Stub.asInterface(service);
                new Thread(new RemoteReceiver()).start();
                new Thread(new EventPlayer()).start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (RemoteLunarLanderActivity.this) {
                remoteService = null;
            }
            finish();
        }

    };

    private final BlockingQueue<RemoteEvent> eventQueue = new ArrayBlockingQueue<RemoteEvent>(256,
            true);

    private boolean remoteStopped;

    private synchronized boolean isRemoteStopped() {
        return remoteStopped;
    }

    private synchronized void setRemoteStopped(boolean remoteStopped) {
        this.remoteStopped = remoteStopped;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lunarView = (View) findViewById(R.id.lunar);

        remoteAndroidController = ((RalanderApplication) getApplication())
                .getRemoteAndroidController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteAndroidController.bindRemoteService(serviceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteAndroidController.unbindRemoteService(serviceConnection);
    }

    private synchronized boolean isConnected() {
        return remoteService != null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        synchronized (this) {
            if (remoteService != null) {
                stopService(new Intent(RalanderActions.REMOTE_EVENT_SERVICE));
            }
        }
    }

    private class RemoteReceiver implements Runnable {

        @Override
        public void run() {
            // TODO use handler for uithread
            while (isConnected() && !isRemoteStopped()) {
                try {
                    boolean start = true;
                    @SuppressWarnings("unchecked")
                    List<RemoteEvent> events = (List<RemoteEvent>) remoteService.getEvents(start);
                    start = false;
                    if (events != null) {
                        if (events.isEmpty()) {
                            // The server return either null (stopped), either a non-empty list
                            // (if it is empty, it waits until an item is added)
                            throw new RuntimeException(
                                    "events should never be empty, check RemoteEventAndroidService");
                        }
                        RemoteEvent event = events.get(0);

                        // resync timestamps
                        long remoteTimestamp = event.getTimestamp();
                        long now = System.currentTimeMillis();
                        long delta = now - remoteTimestamp;
                        event.setTimestamp(now);
                        eventQueue.put(event);
                        for (int i = 1; i < events.size(); i++) {
                            event = events.get(i);
                            event.setTimestamp(event.getTimestamp() + delta);
                            eventQueue.put(event);
                        }
                    } else {
                        setRemoteStopped(true);
                    }
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                } catch (InterruptedException e) {
                    // should never happen
                }
            }
        }
    }

    private class EventPlayer implements Runnable {
        @Override
        public void run() {
            try {
                while (!isRemoteStopped()) {
                    final RemoteEvent remoteEvent = eventQueue.take();

                    // Events are ordered (naturally, by timestamp)
                    long now = System.currentTimeMillis();
                    long eventTimestamp = remoteEvent.getTimestamp();
                    if (eventTimestamp > now) {
                        Thread.sleep(eventTimestamp - now);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            KeyEvent event = remoteEvent.getKeyEvent();
                            int keyCode = event.getKeyCode();
                            switch (event.getAction()) {
                            case KeyEvent.ACTION_DOWN:
                                lunarView.onKeyDown(keyCode, event);
                                break;
                            case KeyEvent.ACTION_UP:
                                lunarView.onKeyUp(keyCode, event);
                                break;
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                // should never happen
            }
        }
    }

}
