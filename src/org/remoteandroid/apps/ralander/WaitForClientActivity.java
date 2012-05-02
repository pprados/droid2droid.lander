package org.remoteandroid.apps.ralander;

import java.io.IOException;

import org.remoteandroid.apps.ralander.RemoteAndroidController.RemoteAndroidDefaultListener;
import org.remoteandroid.apps.ralander.RemoteAndroidController.RemoteAndroidListener;
import org.remoteandroid.util.RAUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class WaitForClientActivity extends Activity {

    private RemoteAndroidController remoteAndroidController;
    private boolean discovered;
    private boolean connected;
    private boolean installed;

    private Button button;
    private boolean bound;
    private ImageView qrCode;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private RemoteAndroidListener remoteAndroidListener = new RemoteAndroidDefaultListener() {

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

        @Override
        public void pushStarted() {
            AlertDialog.Builder builder = new AlertDialog.Builder(WaitForClientActivity.this);
            alertDialog = builder.setTitle("Waiting for client").setMessage("Please wait...")
                    .create();
            alertDialog.show();
        }

		@Override
        public void pushProgress(int progress) {
            if (progressDialog == null) {
                alertDialog.hide();
                progressDialog = new ProgressDialog(WaitForClientActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                // progress is between 0 and 10000, see AbstractRemoteAndroidImpl.pushMe(...)
                progressDialog.setMax(10000);
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                	progressDialog.setProgressNumberFormat(null);
                progressDialog.setMessage("Sending APK...");
                progressDialog.show();
            }
            progressDialog.setProgress(progress);
            if (progress == 10000) {
                progressDialog.dismiss();
                progressDialog = null;
                alertDialog.show();
            }
        }

        @Override
        public void pushFinished(int status) {
            alertDialog.dismiss();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            if (status >= 0) {
                installed = true;
            }
        }

        private void startIfNeeded() {
            if (discovered && installed && connected) {
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
        connected = false;
        installed = false;
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
