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
