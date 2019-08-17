package com.yulay.imagefetcher;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

public class MemoryLruCacheWrapper implements MemoryCache {

    private final LruCache<String, Bitmap> cache;

    public MemoryLruCacheWrapper(int memoryCacheSize) {
        cache = new LruCache<String, Bitmap>(memoryCacheSize) {
            /**
             * Measure item size in kilobytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return Utils.getBitmapSize(bitmap);
            }
        };
    }

    @Override
    public Bitmap get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, Bitmap bitmap) {
        cache.put(key, bitmap);
    }

    @Override
    public void clear() {
        cache.evictAll();
    }
}
