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
package org.droid2droid.apps.ralander;

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
