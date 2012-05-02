package org.remoteandroid.control;

import org.remoteandroid.control.RemoteEventReceiver.RemoteEventListener;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Remote control helper for the game-host.
 * 
 * @author rom
 * 
 */
public class RemoteControlHelper {

    /**
     * Remote control (initialized in
     * {@link ServiceConnection#onServiceConnected(ComponentName, IBinder)}).
     */
    private RemoteControl remoteControl;

    /**
     * Remote event receiver (initialized in
     * {@link ServiceConnection#onServiceConnected(ComponentName, IBinder)}).
     */
    private RemoteEventReceiver remoteEventReceiver;

    /** Remote event listener (given as parameter). */
    private RemoteEventListener remoteEventListener;

    /**
     * ServiceConnection to use for binding the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // This method is called in the UI Thread
            remoteControl = RemoteControl.Stub.asInterface(service);
            remoteEventReceiver = new RemoteEventReceiver(remoteControl, remoteEventListener,
                    new Handler());
            remoteEventReceiver.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (remoteEventReceiver != null) {
                remoteEventReceiver.stop();
                remoteEventReceiver = null;
                remoteControl = null;
            }
        }

    };

    /**
     * Create a new helper.
     * 
     * @param remoteEventListener
     *            Listener.
     */
    public RemoteControlHelper(RemoteEventListener remoteEventListener) {
        this.remoteEventListener = remoteEventListener;
    }

    /**
     * Return the {@link ServiceConnection} to use for binding the service.
     * 
     * @return Service connection.
     */
    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    /**
     * Send a stop request to the control-host, asynchronously (in a new thread), for indicating
     * that the game-host has stopped. <br />
     * Call this method when you stop the game-host.
     */
    public void asyncStopClient() {
        if (remoteControl == null) {
            return;
        }
        final RemoteEventReceiver remoteEventReceiver = this.remoteEventReceiver;
        final RemoteControl remoteControl = this.remoteControl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Stop the receiver locally, for avoiding it to believe that the client stopped
                    // explicitly (by returning null, due to stopCapture() next line)
                    remoteEventReceiver.stop();
                    // Send the stop request
                    remoteControl.stopCapture();
                } catch (RemoteException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }
        }).start();
    }

}
