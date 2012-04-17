package org.remoteandroid.util;

import java.io.IOException;
import java.io.InputStream;

import org.remoteandroid.RemoteAndroidManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

public class RAUtils {

    private RAUtils() {}

    public static Bitmap createQRCodeBitmap(Context context) throws IOException {
        InputStream in = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                in = context
                        .getContentResolver()
                        .openTypedAssetFileDescriptor(RemoteAndroidManager.QRCODE_URI, "image/png",
                                null).createInputStream();
            } else {
                in = context.getContentResolver().openInputStream(RemoteAndroidManager.QRCODE_URI);
            }
            return BitmapFactory.decodeStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        // Bitmap scaBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        // bitmap.recycle();
        // mQrCode.setImageBitmap(scaBitmap);
        // mQrCode.getImageMatrix().setScale(0.3f, 0.3f);
        // mQrCode.invalidate();
    }

    public static Bitmap createQRCodeScaledBitmap(Context context, int size) throws IOException {
        Bitmap bitmap = createQRCodeBitmap(context);
        try {
            return Bitmap.createScaledBitmap(bitmap, size, size, false);
        } finally {
            bitmap.recycle();
        }
    }

}
