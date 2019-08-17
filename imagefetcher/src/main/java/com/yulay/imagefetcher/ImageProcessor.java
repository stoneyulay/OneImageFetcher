package com.yulay.imagefetcher;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

public abstract class ImageProcessor {
    public abstract boolean canProcess(Object data);
    public abstract Result process(Object data, DisplayOptions options) throws IOException;

    public static final class Result {
        private final Bitmap bitmap;
        private final InputStream stream;

        public Result(Bitmap bitmap, InputStream stream) {
            this.bitmap = bitmap;
            this.stream = stream;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public InputStream getStream() {
            return stream;
        }
    }
}
