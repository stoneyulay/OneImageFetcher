package com.yulay.oneimagefetcher.sample;

import android.app.Application;

import com.yulay.imagefetcher.OneImageFetcher;

public class ImageApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // init OneImageFetcher
        OneImageFetcher fetcher = new OneImageFetcher.Builder(this)
                .diskCacheSize(50 * 1024 * 1024)
                .loggingEnabled(true)
                .build();
        OneImageFetcher.setSingletonInstance(fetcher);
    }
}
