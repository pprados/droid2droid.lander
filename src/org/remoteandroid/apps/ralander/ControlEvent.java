package org.remoteandroid.apps.ralander;

import android.os.Parcel;
import android.os.Parcelable;

public class ControlEvent implements Parcelable {

    public enum Key {
        LEFT, RIGHT, UP, FIRE
    };

    public enum Action {
        UP, DOWN
    };

    private Key key;
    private Action action;

    private ControlEvent() {}

    public ControlEvent(Key key, Action action) {
        this.key = key;
        this.action = action;
    }

    public Key getKey() {
        return key;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(key.ordinal());
        dest.writeInt(action.ordinal());
    }

    public static final Parcelable.Creator<ControlEvent> CREATOR = new Parcelable.Creator<ControlEvent>() {

        @Override
        public ControlEvent createFromParcel(Parcel source) {
            ControlEvent controlEvent = new ControlEvent();
            controlEvent.key = Key.values()[source.readInt()];
            controlEvent.action = Action.values()[source.readInt()];
            return controlEvent;
        }

        @Override
        public ControlEvent[] newArray(int size) {
            return new ControlEvent[size];
        }

    };

}
