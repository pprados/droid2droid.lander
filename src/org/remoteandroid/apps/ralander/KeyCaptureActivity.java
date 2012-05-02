package org.remoteandroid.apps.ralander;

import org.remoteandroid.control.RemoteControlActions;
import org.remoteandroid.control.RemoteControlBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class KeyCaptureActivity extends Activity {

    private RemoteControlBinder remoteControl;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteControl = (RemoteControlBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteControl = null;
        }

    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(KeyCaptureActivity.this, "Server disconnected", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    };

    private Button left;
    private Button right;
    private Button up;
    private Button fire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_view);

        left = (Button) findViewById(R.id.leftButton);
        right = (Button) findViewById(R.id.rightButton);
        up = (Button) findViewById(R.id.upButton);
        fire = (Button) findViewById(R.id.fireButton);

        OnTouchListener touchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(v, event);
                return false;
            }
        };

        left.setOnTouchListener(touchListener);
        right.setOnTouchListener(touchListener);
        up.setOnTouchListener(touchListener);
        fire.setOnTouchListener(touchListener);

    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(RalanderActions.REMOTE_CONTROL_SERVICE), serviceConnection, 0);
        registerReceiver(stopReceiver, new IntentFilter(RemoteControlActions.STOP_CAPTURE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        unregisterReceiver(stopReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ((keyCode==KeyEvent.KEYCODE_HOME) || (keyCode==KeyEvent.KEYCODE_BACK))
    	{
    		onBackPressed();
    		return super.onKeyDown(keyCode, event);
    	}
        onKeyEvent(event);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if ((keyCode==KeyEvent.KEYCODE_HOME) || (keyCode==KeyEvent.KEYCODE_BACK))
//    	{
//    		onBackPressed();
//    		return super.onKeyDown(keyCode, event);
//    	}
        onKeyEvent(event);
        return true;
    }

    private void onKeyEvent(KeyEvent event) {
        if (remoteControl != null) {
            ControlEvent controlEvent = toControlEvent(event);
            if (controlEvent != null) {
                remoteControl.sendEvent(controlEvent);
            }
        }
    }

    private void onTouchEvent(View v, MotionEvent event) {
        if (remoteControl != null) {
            ControlEvent controlEvent = toControlEvent(v, event);
            if (controlEvent != null) {
                remoteControl.sendEvent(controlEvent);
            }
        }
    }

    private ControlEvent toControlEvent(View v, MotionEvent event) {
        ControlEvent.Key key;
        ControlEvent.Action action;
        if (v == left) {
            key = ControlEvent.Key.LEFT;
        } else if (v == right) {
            key = ControlEvent.Key.RIGHT;
        } else if (v == up) {
            key = ControlEvent.Key.UP;
        } else if (v == fire) {
            key = ControlEvent.Key.FIRE;
        } else {
            return null;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            action = ControlEvent.Action.UP;
            break;
        case MotionEvent.ACTION_DOWN:
            action = ControlEvent.Action.DOWN;
            break;
        default:
            return null;
        }
        return new ControlEvent(key, action);
    }

    private static ControlEvent toControlEvent(KeyEvent event) {
        ControlEvent.Key key;
        ControlEvent.Action action;
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            key = ControlEvent.Key.LEFT;
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            key = ControlEvent.Key.RIGHT;
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
            key = ControlEvent.Key.UP;
            break;
        case KeyEvent.KEYCODE_SPACE:
            key = ControlEvent.Key.FIRE;
            break;
        default:
            return null;
        }
        switch (event.getAction()) {
        case KeyEvent.ACTION_UP:
            action = ControlEvent.Action.UP;
            break;
        case KeyEvent.ACTION_DOWN:
            action = ControlEvent.Action.DOWN;
            break;
        default:
            return null;
        }
        return new ControlEvent(key, action);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (remoteControl != null) {
            remoteControl.stopCaptureByClient();
        }
    }

}
