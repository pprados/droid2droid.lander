package org.remoteandroid.apps.ralander;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public class RemoteEventReceiver {

    public interface RemoteEventListener {
        void onRemoteEvent(RemoteEvent remoteEvent);

        void onStoppedByClient();
    }

    private BlockingQueue<RemoteEvent> eventQueue;

    private RemoteEventService remoteService;
    private RemoteEventListener listener;
    private Handler handler;

    private boolean started;
    private boolean stoppedByClient;

    private RemoteReceiver remoteReceiver;
    private RemoteEventPlayer remoteEventPlayer;

    public RemoteEventReceiver(RemoteEventService remoteService, RemoteEventListener listener,
            Handler handler) {
        this.remoteService = remoteService;
        this.listener = listener;
        this.handler = handler;
    }

    public synchronized void start() {
        if (!isStarted() && !stoppedByClient) {
            started = true;

            eventQueue = new ArrayBlockingQueue<RemoteEvent>(16, true);

            remoteReceiver = new RemoteReceiver();
            remoteEventPlayer = new RemoteEventPlayer();

            remoteReceiver.start();
            remoteEventPlayer.start();
        }
    }

    public synchronized void stop() {
        if (isStarted()) {
            started = false;

            remoteReceiver.interrupt();
            remoteEventPlayer.interrupt();

            remoteReceiver = null;
            remoteEventPlayer = null;

            eventQueue = null;
        }
    }

    public synchronized boolean isStarted() {
        return started;
    }

    private synchronized void stopByClient() {
        stop();
        this.stoppedByClient = true;
        dispatchStoppedByClient();
    }

    public synchronized boolean isStoppedByClient() {
        return stoppedByClient;
    }

    protected void dispatchRemoteEvent(final RemoteEvent remoteEvent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onRemoteEvent(remoteEvent);
            }
        });
    }

    protected void dispatchStoppedByClient() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onStoppedByClient();
            }
        });
    }

    // public RemoteEvent nextEvent() throws InterruptedException {
    // return queue.take();
    // }

    private class RemoteReceiver extends Thread {

        @Override
        public void run() {
            boolean first = true;
            while (!isInterrupted()) {
                try {
                    @SuppressWarnings("unchecked")
                    List<RemoteEvent> events = (List<RemoteEvent>) remoteService.getEvents(first);
                    first = false;
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
                        stopByClient();
                    }
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                } catch (InterruptedException e) {
                    // terminate the thread
                }
            }
        }
    }

    private class RemoteEventPlayer extends Thread {

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    final RemoteEvent remoteEvent = eventQueue.take();

                    if (isInterrupted()) {
                        return;
                    }

                    // Events are ordered (naturally, by timestamp)
                    long now = System.currentTimeMillis();
                    long eventTimestamp = remoteEvent.getTimestamp();
                    if (eventTimestamp > now) {
                        Thread.sleep(eventTimestamp - now);
                    }

                    dispatchRemoteEvent(remoteEvent);
                }
            } catch (InterruptedException e) {
                // terminate the thread
            }
        }
    }

}
