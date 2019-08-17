package com.yulay.imagefetcher;

import android.graphics.Bitmap;

public class DisplayOptions {
    final int width;
    final int height;
    final Bitmap.Config bitmapConfig;
    final Bitmap loadingBitmap;
    final int memoryPolicy;
    final int diskPolicy;
    final boolean fadeIn;

    public DisplayOptions(int width, int height, Bitmap.Config bitmapConfig, Bitmap loadingBitmap, int memoryPolicy, int diskPolicy, boolean fadeIn) {
        this.width = width;
        this.height = height;
        this.bitmapConfig = bitmapConfig;
        this.loadingBitmap = loadingBitmap;
        this.memoryPolicy = memoryPolicy;
        this.diskPolicy = diskPolicy;
        this.fadeIn = fadeIn;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasSize() {
        return width != 0 || height != 0;
    }

    public Bitmap.Config getBitmapConfig() {
        return bitmapConfig;
    }

    public Bitmap getLoadingBitmap() {
        return loadingBitmap;
    }

    public int getMemoryPolicy() {
        return memoryPolicy;
    }

    public int getDiskPolicy() {
        return diskPolicy;
    }

    public boolean fadeIn() {
        return fadeIn;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DisplayOptions{");
        if (hasSize()) {
            sb.append(" size(").append(width).append(',').append(height).append(')');
        }
        if (bitmapConfig != null) {
            sb.append(' ').append(bitmapConfig);
        }
        if (loadingBitmap != null) {
            sb.append(' ').append(loadingBitmap);
        }
        sb.append(" memoryPolicy(").append(memoryPolicy).append(')');
        sb.append(" diskPolicy(").append(diskPolicy).append(')');
        sb.append(" fadeIn(").append(fadeIn).append(')');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        private int width;
        private int height;
        private Bitmap.Config bitmapConfig;
        private Bitmap loadingBitmap;
        private int memoryPolicy;
        private int diskPolicy;
        private boolean fadeIn;

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bitmapConfig(Bitmap.Config bitmapConfig) {
            this.bitmapConfig = bitmapConfig;
            return this;
        }

        public Builder loadingBitmap(Bitmap loadingBitmap) {
            this.loadingBitmap = loadingBitmap;
            return this;
        }

        public Builder memoryPolicy(int memoryPolicy) {
            this.memoryPolicy = memoryPolicy;
            return this;
        }

        public Builder diskPolicy(int diskPolicy) {
            this.diskPolicy = diskPolicy;
            return this;
        }

        public Builder fadeIn(boolean fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public Builder copyFrom(DisplayOptions options) {
            width = options.width;
            height = options.height;
            bitmapConfig = options.bitmapConfig;
            loadingBitmap = options.loadingBitmap;
            memoryPolicy = options.memoryPolicy;
            diskPolicy = options.diskPolicy;
            fadeIn = options.fadeIn;
            return this;
        }

        public DisplayOptions build() {
            return new DisplayOptions(width, height, bitmapConfig, loadingBitmap, memoryPolicy, diskPolicy, fadeIn);
        }
    }
}
