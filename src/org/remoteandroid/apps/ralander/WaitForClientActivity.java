package org.remoteandroid.apps.ralander;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class WaitForClientActivity extends Activity {

    private RemoteAndroidController remoteAndroidController;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            startActivity(new Intent(WaitForClientActivity.this, RemoteLunarLanderActivity.class));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wait_for_client);

        remoteAndroidController = ((RalanderApplication) getApplication())
                .getRemoteAndroidController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteAndroidController.bindRemoteService(serviceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteAndroidController.unbindRemoteService(serviceConnection);
    }

}
