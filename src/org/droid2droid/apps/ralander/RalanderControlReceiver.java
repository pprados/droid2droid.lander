package org.droid2droid.apps.ralander;

import org.droid2droid.control.RemoteControlReceiver;

import android.content.Context;
import android.content.Intent;

public class RalanderControlReceiver extends RemoteControlReceiver {

    @Override
    protected void onStart(Context context) {
        Intent intent = new Intent(context, KeyCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
