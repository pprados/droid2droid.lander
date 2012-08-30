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

import java.util.ArrayList;
import java.util.List;

import org.droid2droid.control.RemoteControl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

/**
 * Remote control service binder, executed on the control-host, called by the game-host.<br />
 * The game-host request to the control-host the list of events to apply. Each event is timestamped
 * (see {@link RemoteEvent}), for keeping the delay between events.<br />
 * The control-host must bind to this service locally and call {@link #sendEvent(Object)} for adding
 * an event. The game-host will periodically (as often as possible) retrieve the events added by the
 * control-host, calling remotely {@link #getEvents(boolean)}.
 * 
 * @author rom
 * 
 */
public class RemoteControlBinder extends RemoteControl.Stub {

    /** Pending events added by the control-host but not retrieved by the game-host yet. */
    private List<RemoteEvent> pendingEvents = new ArrayList<RemoteEvent>();

    /**
     * Flag indicating that the event retrieving is stopped (either by the game-host or by the
     * control-host).
     */
    private boolean stopped;

    /** Android service (used for {@link Context}) and {@link Service#stopSelf()}. */
    private RemoteControlService service;

    /**
     * Create the binder.
     * 
     * @param service
     *            Android service.
     */
    public RemoteControlBinder(RemoteControlService service) {
        this.service = service;
    }

    /**
     * Return the events from the control-host.
     * 
     * @param start
     *            Flag indicating whether the control-host must start the capture (the first time).
     *            It is given as parameter for avoiding to call in two steps start() then
     *            getEvents().
     * 
     */
    @Override
    public synchronized List<?> getEvents(boolean start) throws RemoteException {
        // If the game-host set start to true, then the control-host must start the capture (send a
        // broadcast which will typically start a capture view)
        if (start) {
            startCapture();
            stopped = false;
        }
        try {
            // Wait until at least one event is available or the event retrieving is stopped (either
            // by the game-host or by the control-host)
            while (pendingEvents.isEmpty() && !stopped) {
                wait();
            }

            // If it is stopped, then return null
            // The game host will consider a null result as an explicit stop by the client (if it
            // has not called stopCapture)
            if (stopped) {
                return null;
            }

            // Keep the current pending events for returning to the game-host
            List<RemoteEvent> result = pendingEvents;

            // Now, use a new empty pending events list
            pendingEvents = new ArrayList<RemoteEvent>();

            // Return the events to the game-host
            return result;

        } catch (InterruptedException e) {
            // should never happen
            return null;
        }
    }

    /**
     * Start capture (send a broadcast). <br />
     * Called when the game-host called {@link #getEvents(boolean) getEvents(true)}).
     */
    private void startCapture() {
        Intent intent = new Intent(RemoteControlActions.START_CAPTURE);
        service.sendBroadcast(intent);
    }

    /**
     * Stop capture. Calling this method will stop any blocking wait in {@link #getEvents(boolean)}
     * and make them return <code>null</code>.
     * 
     * @param byClient
     *            Indicates whether the stop is requested by the control-host (<code>true</code>) or
     *            by the game-host (<code>false</code>). If it is requested by the game-host, then
     *            also send a broadcast for notifying the control-host.
     */
    private synchronized void stopCapture(boolean byClient) {
        stopped = true;
        if (!byClient) {
            // If the stop come from the server, then we broadcast locally
            Intent intent = new Intent(RemoteControlActions.STOP_CAPTURE);
            service.sendBroadcast(intent);
        }
        notify();
    }

    /**
     * Stop capture by the server (called remotely by the game-host).
     */
    @Override
    public void stopCapture() {
        stopCapture(false);
    }

    /**
     * Stop capture by the client (called locally by the control-host).
     */
    public void stopCaptureByClient() {
        stopCapture(true);
    }

    /**
     * Send an event (put it in the pending list) from the control-host to the game-host.
     * 
     * @param event
     *            Event.
     */
    public synchronized void sendEvent(Object event) {
        if (!stopped) {
            // Get a timestamped event
            pendingEvents.add(toRemoteEvent(event));
            // Notify getEvents(boolean) blocking wait
            notify();
        }
    }

    /**
     * Create a timestamped {@link RemoteEvent}.
     * 
     * @param event
     *            Event
     * @return Remote event.
     */
    private static RemoteEvent toRemoteEvent(Object event) {
        return new RemoteEvent(System.currentTimeMillis(), event);
    }

}
