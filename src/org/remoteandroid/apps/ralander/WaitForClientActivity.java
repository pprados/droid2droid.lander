package org.remoteandroid.apps.ralander;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

public class WaitForClientActivity extends Activity {

    private RemoteAndroidController remoteAndroidController;

    private Button button;
    private boolean bound;
    
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

        button = (Button) findViewById(R.id.button);
    }

    public void onClick(View v) {
        remoteAndroidController.bindRemoteService(serviceConnection);
        bound = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            remoteAndroidController.unbindRemoteService(serviceConnection);
            bound = false;
        }
    }

}
