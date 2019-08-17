package com.yulay.imagefetcher;

import android.graphics.Bitmap;

public interface MemoryCache {
    Bitmap get(String key);
    void put(String key, Bitmap bitmap);
    void clear();
}
