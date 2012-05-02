package org.remoteandroid.control;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Android service for remote control (only implements {@link Service#onBind(Intent)} returning an
 * instance of {@link RemoteControlBinder}). <br />
 * To be used on the control-host.
 * 
 * @author rom
 * 
 */
public class RemoteControlService extends Service {
    
    @Override
    public IBinder onBind(Intent intent) {
        return new RemoteControlBinder(this);
    }

}
