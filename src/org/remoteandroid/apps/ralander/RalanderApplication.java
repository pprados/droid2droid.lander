package org.remoteandroid.apps.ralander;

import android.app.Application;

public class RalanderApplication extends Application {

    private RemoteAndroidController remoteAndroidController = new RemoteAndroidController(this);

    @Override
    public void onCreate() {
        super.onCreate();
        remoteAndroidController.open();
    }

    public RemoteAndroidController getRemoteAndroidController() {
        return remoteAndroidController;
    }

}
