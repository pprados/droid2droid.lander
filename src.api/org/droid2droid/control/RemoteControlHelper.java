/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.control;

import org.droid2droid.control.RemoteControl;
import org.droid2droid.control.RemoteEventReceiver.RemoteEventListener;

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
