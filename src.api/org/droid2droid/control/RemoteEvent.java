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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Remote event (timestamped event). Contains a timestamp and any event object. <br />
 * Internal use only.
 * 
 * @author rom
 * 
 */
class RemoteEvent implements Parcelable {

    /** Timestamp (in milliseconds since epoch). */
    private long timestamp;

    /** Event. */
    private Object event;

    private RemoteEvent() {}

    /**
     * Create a remote event.
     * 
     * @param timestamp
     *            Timestamp (in milliseconds since epoch).
     * @param event
     *            Event.
     */
    public RemoteEvent(long timestamp, Object event) {
        this.timestamp = timestamp;
        this.event = event;
    }

    /**
     * Return the timestamp.
     * 
     * @return Timestamp (in milliseconds since epoch).
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Change the timestamp.
     * 
     * @param timestamp
     *            Timestamp (in milliseconds since epoch);
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Return the event.
     * 
     * @return Event.
     */
    public Object getEvent() {
        return event;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeValue(event);
    }

    public static final Parcelable.Creator<RemoteEvent> CREATOR = new Parcelable.Creator<RemoteEvent>() {

        @Override
        public RemoteEvent createFromParcel(Parcel source) {
            RemoteEvent remoteEvent = new RemoteEvent();
            remoteEvent.timestamp = source.readLong();
            remoteEvent.event = source.readValue(RemoteEvent.class.getClassLoader());
            return remoteEvent;
        }

        @Override
        public RemoteEvent[] newArray(int size) {
            return new RemoteEvent[size];
        }

    };

}
