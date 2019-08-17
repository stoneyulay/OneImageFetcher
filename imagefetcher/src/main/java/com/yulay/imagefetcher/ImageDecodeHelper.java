/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yulay.imagefetcher;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageDecodeHelper {
    private static final String TAG = "ImageDecodeHelper";

    private ImageDecodeHelper() {}

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param imageOptions The requested display options of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         DisplayOptions imageOptions) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = createBitmapOptions(imageOptions);
        final boolean calculateSize = requiresInSampleSize(options);

        if (calculateSize) {
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            calculateInSampleSize(imageOptions.getWidth(), imageOptions.getHeight(), options, imageOptions);
            // END_INCLUDE (read_bitmap_dimensions)
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param imageOptions The requested display options of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     DisplayOptions imageOptions) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = createBitmapOptions(imageOptions);
        final boolean calculateSize = requiresInSampleSize(options);

        if (calculateSize) {
            BitmapFactory.decodeFile(filename, options);

            // Calculate inSampleSize
            calculateInSampleSize(imageOptions.getWidth(), imageOptions.getHeight(), options, imageOptions);
            // END_INCLUDE (read_bitmap_dimensions)
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param imageOptions The requested display options of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, DisplayOptions imageOptions) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = createBitmapOptions(imageOptions);
        final boolean calculateSize = requiresInSampleSize(options);

        if (calculateSize) {
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            // Calculate inSampleSize
            calculateInSampleSize(imageOptions.getWidth(), imageOptions.getHeight(), options, imageOptions);
            // END_INCLUDE (read_bitmap_dimensions)
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static Bitmap decodeSampledBitmapFromStream(
            InputStream stream, DisplayOptions imageOptions) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = createBitmapOptions(imageOptions);
        final boolean calculateSize = requiresInSampleSize(options);

        if (calculateSize) {
            BitmapFactory.decodeStream(stream, null ,options);

            // Calculate inSampleSize
            calculateInSampleSize(imageOptions.getWidth(), imageOptions.getHeight(), options, imageOptions);
            // END_INCLUDE (read_bitmap_dimensions)
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeStream(stream, null, options);
    }

    static BitmapFactory.Options createBitmapOptions(DisplayOptions data) {
        final boolean justBounds = data.hasSize();
        final boolean hasConfig = data.getBitmapConfig() != null;
        BitmapFactory.Options options = null;
        if (justBounds || hasConfig) {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = justBounds;
            if (hasConfig) {
                options.inPreferredConfig = data.getBitmapConfig();
            }
        }
        return options;
    }

    static boolean requiresInSampleSize(BitmapFactory.Options options) {
        return options != null && options.inJustDecodeBounds;
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options,
                                      DisplayOptions imageOptions) {
        calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options,
                imageOptions);
    }

    static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
                                      BitmapFactory.Options options, DisplayOptions imageOptions) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }
}
