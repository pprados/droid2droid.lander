package org.droid2droid.apps.ralander;

import android.app.Application;

public class RalanderApplication extends Application {

    private Droid2DroidController remoteAndroidController = new Droid2DroidController(this);

    @Override
    public void onCreate() {
        super.onCreate();
        remoteAndroidController.open();
    }

    public Droid2DroidController getRemoteAndroidController() {
        return remoteAndroidController;
    }

}
