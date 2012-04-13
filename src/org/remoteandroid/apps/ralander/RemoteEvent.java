package org.remoteandroid.apps.ralander;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;

public class RemoteEvent implements Parcelable {

    private long timestamp;
    private KeyEvent keyEvent;

    private RemoteEvent() {}

    public RemoteEvent(long timestamp, KeyEvent keyEvent) {
        this.timestamp = timestamp;
        this.keyEvent = keyEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeParcelable(keyEvent, flags);
    }

    public static final Parcelable.Creator<RemoteEvent> CREATOR = new Parcelable.Creator<RemoteEvent>() {

        @Override
        public RemoteEvent createFromParcel(Parcel source) {
            RemoteEvent remoteEvent = new RemoteEvent();
            remoteEvent.timestamp = source.readLong();
            remoteEvent.keyEvent = source.readParcelable(RemoteEvent.class.getClassLoader());
            return remoteEvent;
        }

        @Override
        public RemoteEvent[] newArray(int size) {
            return new RemoteEvent[size];
        }

    };

}
