package org.droid2droid.control;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.remoteandroid.control.RemoteControl;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

/**
 * Remote event receiver, to be used on the game-host. <br />
 * It will retrieve the events from the control-host and dispatch them on the game-host. <br />
 * Two threads are used:
 * <ul>
 * <li>{@link RemoteReceiver} for retrieving the events lists from the control-host;</li>
 * <li>{@link RemoteEventPlayer} for dispatching the events received keeping the right
 * synchronization (the time between events).</li>
 * </ul>
 * 
 * @author rom
 * 
 */
public class RemoteEventReceiver {

    /**
     * Remote event listener.
     * 
     * @author rom
     * 
     */
    public interface RemoteEventListener {

        /**
         * Called when a remote event must be applied on the game-host.
         * 
         * @param event
         *            Event sent by the control-host (in
         *            {@link RemoteControlBinder#sendEvent(Object)}).
         */
        void onRemoteEvent(Object event);

        /**
         * Called when the control-host explicitly stopped (by returning <code>null</code> in
         * {@link RemoteControlBinder#getEvents(boolean)}.
         */
        void onStoppedByClient();
    }

    /** Event queue. Producer is {@link RemoteReceiver}, consumer is {@link RemoteEventPlayer}. */
    private BlockingQueue<RemoteEvent> eventQueue;

    /** Remote service to use for retrieving events from the control-host. */
    private RemoteControl remoteService;

    /**
     * Listener to use for dispatching events to the control-host, or notifying the control-host has
     * stopped.
     */
    private RemoteEventListener listener;

    /** Handler used for dispatching events. */
    private Handler handler;

    /**
     * Flag indicating the receiver is started (the threads are running).<br />
     * This receiver can be started and stopped any number of times.
     **/
    private boolean started;

    /**
     * Flag indicating the control-host explicitly stopped (returning <code>null</code> in
     * {@link RemoteControlBinder#getEvents(boolean)}.<br />
     * Once it is set to <code>true</code>, it is definitive (it cannot be set to <code>false</code>
     * anymore).
     */
    private boolean stoppedByClient;

    /** Receiver thread instance. */
    private RemoteReceiver remoteReceiver;

    /** Event player thread instance. */
    private RemoteEventPlayer remoteEventPlayer;

    /**
     * Create a new remote event receiver.
     * 
     * @param remoteService
     *            Remote service to use for retrieving events from the control-host
     * @param listener
     *            Listener to use for dispatching events to the control-host, or notifying the
     *            control-host has stopped.
     * @param handler
     *            Handler used for dispatching events.
     */
    public RemoteEventReceiver(RemoteControl remoteService, RemoteEventListener listener,
            Handler handler) {
        this.remoteService = remoteService;
        this.listener = listener;
        this.handler = handler;
    }

    /**
     * Start the receiver.<br />
     * If it is already started, or if the client explicitly stopped, then do nothing.
     */
    public synchronized void start() {
        // If the control-host (client) explicitly stopped, we must not start anymore
        if (!started && !stoppedByClient) {
            started = true;

            // Create a blocking queue having a capacity of 16 remote events
            eventQueue = new ArrayBlockingQueue<RemoteEvent>(16);

            remoteReceiver = new RemoteReceiver();
            remoteEventPlayer = new RemoteEventPlayer();

            remoteReceiver.start();
            remoteEventPlayer.start();
        }
    }

    /**
     * Stop the receiver, interrupting any blocking request to the control-host.<br />
     * If it is already stopped, do nothing.
     */
    public synchronized void stop() {
        if (started) {
            started = false;

            remoteReceiver.fakeInterrupt();
            remoteEventPlayer.fakeInterrupt();

            remoteReceiver = null;
            remoteEventPlayer = null;

            eventQueue = null;
        }
    }

    /**
     * Stop, set the <code>stoppedByClient</code> flag to <code>true</code> and notify it has
     * stopped by client.
     */
    private synchronized void stopByClient() {
        stop();
        stoppedByClient = true;
        dispatchStoppedByClient();
    }

    /**
     * Dispatch remote event in the handler.
     * 
     * @param event
     *            Event.
     */
    protected void dispatchRemoteEvent(final Object event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onRemoteEvent(event);
            }
        });
    }

    /**
     * Notify a "stopped by client" event in the handler.
     */
    protected void dispatchStoppedByClient() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onStoppedByClient();
            }
        });
    }
    
    private class FakeInterruptibleThread extends Thread {
    	public FakeInterruptibleThread(String threadName)
		{
			super(threadName);
		}

		private boolean fakeInterrupted;
    	public synchronized void fakeInterrupt() {
    		fakeInterrupted = true;
    	}
    	
    	public synchronized boolean isFakeInterrupted() {
            return fakeInterrupted;
    	}
    }

    /**
     * Thread for retrieving the events lists from the control-host.
     * 
     * @author rom
     */
    private class RemoteReceiver extends FakeInterruptibleThread {

        RemoteReceiver() {
            super("RemoteReceiver");
        }

        @Override
        public void run() {
            // We want to call getEvents(true) only the first time, to start capture on the
            // control-host
            boolean first = true;
            // Stop when the thread is interrupted
            while (!isFakeInterrupted()) {
                try {
                    // Retrieve the events from the control-host
                    @SuppressWarnings("unchecked")
                    List<RemoteEvent> events = remoteService.getEvents(first);

                    // If events is null, then the client explicitly stopped
                    if (events == null) {
                        stopByClient();
                        return;
                    }

                    // First will always be false from now
                    first = false;

                    // The server return either null (stopped), either a non-empty list
                    // (if it is empty when RemoteControlBinder.getEvents(boolean) is called, then
                    // it waits until an item is added)

                    // We consider the timestamp of the first event received in a list of events
                    // must be executed now
                    long delta = System.currentTimeMillis() - events.get(0).getTimestamp();

                    // Next events must be delayed by the same duration between the events occurred
                    // on the control-host
                    for (RemoteEvent event : events) {
                        event.setTimestamp(event.getTimestamp() + delta);
                        eventQueue.put(event);
                    }
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                } catch (InterruptedException e) {
                    // terminate the thread
                }
            }
        }
    }

    /**
     * Thread for dispatching the events received keeping the right synchronization (the time
     * between events).
     * 
     * @author rom
     */
    private class RemoteEventPlayer extends FakeInterruptibleThread {

        RemoteEventPlayer() {
            super("RemoteEventPlayer");
        }

        @Override
        public void run() {
            try {
                // Stop when the thread is interrupted
                while (!isFakeInterrupted()) {
                    // Get an event from the queue (pushed by RemoteReceiver)
                    // Blocking wait until a remote event is available
                    final RemoteEvent remoteEvent = eventQueue.take();

                    // If events is null, then the client explicitly stopped
                    if (isFakeInterrupted()) {
                        return;
                    }

                    // Use the new timestamps set by the RemoteReceiver
                    long now = System.currentTimeMillis();
                    long eventTimestamp = remoteEvent.getTimestamp();
                    // If the timestamp is in the future, we must wait until this date
                    if (eventTimestamp > now) {
                        Thread.sleep(eventTimestamp - now);
                    }

                    // Dispatch the remote event to the listener
                    dispatchRemoteEvent(remoteEvent.getEvent());
                }
            } catch (InterruptedException e) {
                // terminate the thread
            }
        }
    }

}
