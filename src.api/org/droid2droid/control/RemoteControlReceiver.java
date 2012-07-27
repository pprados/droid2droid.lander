package org.droid2droid.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver, on the control-host, for the {@link RemoteControlActions#START_CAPTURE start} action
 * from the game-host (typically, when the game-host starts, it want to start the remote control
 * view on the control-host).
 * 
 * @author rom
 * 
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (RemoteControlActions.START_CAPTURE.equals(action)) {
            onStart(context);
        }
    }

    /**
     * Called when the receiver a broadcast {@link RemoteControlActions#START_CAPTURE}.
     * 
     * @param context
     *            Context.
     */
    protected void onStart(Context context) {}

}
