package org.remoteandroid.apps.ralander;

import java.io.IOException;

import org.remoteandroid.apps.ralander.RemoteAndroidController.RemoteAndroidListener;
import org.remoteandroid.util.RAUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class WaitForClientActivity extends Activity {

    private RemoteAndroidController remoteAndroidController;
    private boolean discovered;
    private boolean connected;

    private Button button;
    private boolean bound;
    private ImageView qrCode;

    private RemoteAndroidListener remoteAndroidListener = new RemoteAndroidListener() {

        @Override
        public void discovered() {
            discovered = true;
            startIfNeeded();
        }

        @Override
        public void disconnected() {
            connected = false;
        }

        @Override
        public void connected(IBinder service) {
            connected = true;
            startIfNeeded();
        }

        private void startIfNeeded() {
            if (discovered && connected) {
                startActivity(new Intent(WaitForClientActivity.this,
                        RemoteLunarLanderActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wait_for_client);

        remoteAndroidController = ((RalanderApplication) getApplication())
                .getRemoteAndroidController();

        button = (Button) findViewById(R.id.button);
        qrCode = (ImageView) findViewById(R.id.qrCode);

        try {
            Bitmap bitmap = RAUtils.createQRCodeScaledBitmap(this, 300);
            qrCode.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        // remoteAndroidController.bindRemoteService(serviceConnection);
        // bound = true;
        discovered = true;
        remoteAndroidController.connectHardcoded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        discovered = false;
        remoteAndroidController.addRemoteAndroidListener(remoteAndroidListener);
        bound = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            remoteAndroidController.removeRemoteAndroidListener(remoteAndroidListener);
            bound = false;
        }
    }

}
