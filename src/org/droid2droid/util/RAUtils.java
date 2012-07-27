package org.droid2droid.util;

import static org.droid2droid.Droid2DroidManager.QRCODE_URI;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
public class RAUtils {

    private RAUtils() {}

	@TargetApi(11)
	public static Bitmap createQRCodeBitmap(Context context) throws IOException {
        InputStream in = null;
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                in = context
                        .getContentResolver()
                        .openTypedAssetFileDescriptor(QRCODE_URI, "image/png",
                                null).createInputStream();
            } else {
                in = context.getContentResolver().openInputStream(QRCODE_URI);
            }
            return BitmapFactory.decodeStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
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
