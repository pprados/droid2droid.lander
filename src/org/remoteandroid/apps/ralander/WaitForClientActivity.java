package org.remoteandroid.apps.ralander;

import java.io.IOException;

import org.remoteandroid.util.RAUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class WaitForClientActivity extends Activity {

    private RemoteAndroidController remoteAndroidController;

    private Button button;
    private boolean bound;
    private ImageView qrCode;

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
        qrCode = (ImageView) findViewById(R.id.qrCode);

        try {
            Bitmap bitmap = RAUtils.createQRCodeScaledBitmap(this, 300);
            qrCode.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
//        remoteAndroidController.bindRemoteService(serviceConnection);
//        bound = true;
        remoteAndroidController.connectHardcoded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteAndroidController.bindRemoteService(serviceConnection);
        bound = true;
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
