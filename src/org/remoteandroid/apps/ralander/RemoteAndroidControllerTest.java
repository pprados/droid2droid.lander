package org.remoteandroid.apps.ralander;

import org.remoteandroid.RemoteAndroidManager;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class RemoteAndroidControllerTest {
    
    private Context context;
    
    private RemoteAndroidManager remoteAndroidManager;
    
    private RemoteAndroidControllerTest(Context context) {
        this.context = context;
    }
    
    public void bind(String uri, Intent intent, ServiceConnection conn) {
        
    }

}
