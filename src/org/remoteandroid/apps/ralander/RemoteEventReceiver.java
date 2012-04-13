package org.remoteandroid.apps.ralander;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteEventReceiver {

    public interface EventListener {
        void eventReceived(RemoteEvent event);

        void stoppedByClient();
    }

    private final BlockingQueue<RemoteEvent> queue = new ArrayBlockingQueue<RemoteEvent>(16, true);
    private RemoteEventService remoteService;
    private Handler handler;
    private boolean started;
    private boolean canceled;
    private EventListener eventListener;
    
    public RemoteEventReceiver(RemoteEventService remoteService, Handler handler, EventListener eventListener) {
        this.remoteService = remoteService;
        this.handler = handler;
        this.eventListener = eventListener;
    }
    
    public synchronized void start() {
        if (!started) {
            started = true;
            new Thread(new Receiver()).start();
            new Thread(new Dispatcher()).start();
        }
    }

    public synchronized void cancel() {
        canceled = true;
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    private class Receiver implements Runnable {

        @Override
        public void run() {
            while (!isCanceled()) {
                try {
                    boolean first = true;
                    @SuppressWarnings("unchecked")
                    List<RemoteEvent> events = (List<RemoteEvent>) remoteService.getEvents(first);
                    if (isCanceled()) {
                        return;
                    }
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
                        queue.put(event);
                        for (int i = 1; i < events.size(); i++) {
                            event = events.get(i);
                            event.setTimestamp(event.getTimestamp() + delta);
                            queue.put(event);
                        }
                    } else {
                        cancel();
                        return;
                    }
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                } catch (InterruptedException e) {
                    // should never happen
                }
            }
        }
    }
    
    private class Dispatcher implements Runnable {
        
        @Override
        public void run() {
            try {
                while (!isCanceled()) {
                    final RemoteEvent remoteEvent = queue.take();
                    if (isCanceled()) {
                        return;
                    }
                    // Events are ordered (naturally, by timestamp)
                    long now = System.currentTimeMillis();
                    long eventTimestamp = remoteEvent.getTimestamp();
                    if (eventTimestamp > now) {
                        Thread.sleep(eventTimestamp - now);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eventListener.eventReceived(remoteEvent);
                        }
                    });
                }
            } catch (InterruptedException e) {
                // should never happen
            }
        }
    }

}
