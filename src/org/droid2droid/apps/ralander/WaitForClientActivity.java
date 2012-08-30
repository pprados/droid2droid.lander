/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.apps.ralander;

import java.io.IOException;

import org.droid2droid.apps.ralander.Droid2DroidController.RemoteAndroidDefaultListener;
import org.droid2droid.apps.ralander.Droid2DroidController.RemoteAndroidListener;
import org.droid2droid.util.RAUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class WaitForClientActivity extends Activity {

    private Droid2DroidController remoteAndroidController;
    private boolean discovered;
    private boolean connected;
    private boolean installed;

    private Button button;
    private boolean bound;
    private ImageView qrCode;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private final RemoteAndroidListener remoteAndroidListener = new RemoteAndroidDefaultListener() {

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

		@TargetApi(11)
		@Override
        public void pushProgress(int progress) {
            if (progressDialog == null) {
                alertDialog.hide();
                progressDialog = new ProgressDialog(WaitForClientActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                // progress is between 0 and 10000, see AbstractRemoteAndroidImpl.pushMe(...)
                progressDialog.setMax(10000);
                if (VERSION.SDK_INT>=VERSION_CODES.HONEYCOMB)
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
