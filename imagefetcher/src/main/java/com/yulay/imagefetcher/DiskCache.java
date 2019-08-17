package com.yulay.imagefetcher;

import android.graphics.Bitmap;

import java.io.File;

public interface DiskCache {
    void open();
    File get(String data);
    void put(String data, Bitmap bitmap);
    void clear();
    void flush();
    void close();
}
