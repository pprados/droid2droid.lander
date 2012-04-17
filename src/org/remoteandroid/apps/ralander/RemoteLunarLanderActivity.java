package org.remoteandroid.apps.ralander;

import org.remoteandroid.control.RemoteControlHelper;
import org.remoteandroid.control.RemoteEventReceiver.RemoteEventListener;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.lunarlander.LunarLander;

public class RemoteLunarLanderActivity extends LunarLander {

    private View lunarView;

    private RemoteAndroidController remoteAndroidController;
    
    private RemoteEventListener remoteEventListener = new RemoteEventListener() {

        @Override
        public void onStoppedByClient() {
            Toast.makeText(RemoteLunarLanderActivity.this, "Client disconnected",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onRemoteEvent(Object event) {
            KeyEvent keyEvent = (KeyEvent) event;
            int keyCode = keyEvent.getKeyCode();
            switch (keyEvent.getAction()) {
            case KeyEvent.ACTION_DOWN:
                lunarView.onKeyDown(keyCode, keyEvent);
                break;
            case KeyEvent.ACTION_UP:
                lunarView.onKeyUp(keyCode, keyEvent);
                break;
            }
        }
    };
    
    private RemoteControlHelper remoteControlHelper = new RemoteControlHelper(remoteEventListener);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lunarView = (View) findViewById(R.id.lunar);

        remoteAndroidController = ((RalanderApplication) getApplication())
                .getRemoteAndroidController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteAndroidController.bindRemoteService(remoteControlHelper.getServiceConnection());
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteAndroidController.unbindRemoteService(remoteControlHelper.getServiceConnection());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        remoteControlHelper.asyncStopClient();
    }

}
