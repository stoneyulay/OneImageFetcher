package com.yulay.imagefetcher;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class DisplayOptionsCreator {
    private final OneImageFetcher fetcher;
    private final Object data;
    private final DisplayOptions.Builder optionsBuilder;

    public DisplayOptionsCreator(OneImageFetcher fetcher, Object data) {
        this.fetcher = fetcher;
        this.data = data;
        this.optionsBuilder = new DisplayOptions.Builder().copyFrom(fetcher.getDefaultDisplayOptions());
    }

    public DisplayOptionsCreator size(int width, int height) {
        optionsBuilder.size(width, height);
        return this;
    }

    public DisplayOptionsCreator bitmapConfig(Bitmap.Config bitmapConfig) {
        optionsBuilder.bitmapConfig(bitmapConfig);
        return this;
    }

    public DisplayOptionsCreator loadingBitmap(Bitmap loadingBitmap) {
        optionsBuilder.loadingBitmap(loadingBitmap);
        return this;
    }

    public DisplayOptionsCreator skipMemoryCache() {
        return memoryPolicy(MemoryPolicy.NO_CACHE.index | MemoryPolicy.NO_STORE.index);
    }

    public DisplayOptionsCreator memoryPolicy(int memoryPolicy) {
        optionsBuilder.memoryPolicy(memoryPolicy);
        return this;
    }

    public DisplayOptionsCreator skipDiskCache() {
        return diskPolicy(DiskPolicy.NO_CACHE.index | DiskPolicy.NO_STORE.index);
    }

    public DisplayOptionsCreator diskPolicy(int diskPolicy) {
        optionsBuilder.diskPolicy(diskPolicy);
        return this;
    }
    public DisplayOptionsCreator fadeIn(boolean fadeIn) {
        optionsBuilder.fadeIn(fadeIn);
        return this;
    }

    public void into(ImageView imageView) {
        fetcher.loadImage(data, imageView, optionsBuilder.build(), null);
    }
}
