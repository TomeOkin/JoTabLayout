/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.common.picture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PictureUtil {
  public static Bitmap getScaledBitmapFromResource(Resources resources, int resId, int destWidth,
      int destHeight) {
    if (destWidth <= 0 || destHeight <= 0) {
      return getBitmapFromResource(resources, resId);
    }

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(resources, resId, options);

    // Calculate inSampleSize
    final float srcWidth = options.outWidth;
    final float srcHeight = options.outHeight;

    int inSampleSize = 1;
    if (srcHeight > destHeight || srcWidth > destWidth) {
      if (srcWidth > srcHeight) {
        inSampleSize = Math.round(srcHeight / destHeight);
      } else {
        inSampleSize = Math.round(srcWidth / destWidth);
      }
    }

    options.inSampleSize = inSampleSize;

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(resources, resId, options);
  }

  public static Bitmap getBitmapFromResource(Resources resources, int resId) {
    return BitmapFactory.decodeResource(resources, resId);
  }
}
