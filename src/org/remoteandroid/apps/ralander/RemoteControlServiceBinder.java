package org.remoteandroid.apps.ralander;

import org.remoteandroid.control.AbstractRemoteControlServiceBinder;

import android.content.Context;
import android.content.Intent;

public class RemoteControlServiceBinder extends AbstractRemoteControlServiceBinder {

    private Context context;

    public RemoteControlServiceBinder(Context context) {
        this.context = context;
    }

    @Override
    protected void startView() {
        Intent intent = new Intent(context, KeyCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
