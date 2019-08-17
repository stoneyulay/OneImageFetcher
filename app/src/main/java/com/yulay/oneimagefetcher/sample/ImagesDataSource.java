package com.yulay.oneimagefetcher.sample;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ImagesDataSource {
    private static final String TAG = "ImagesDataSource";

    static volatile ImagesDataSource singleton;
    private final Executor executor;
    private final Handler mainHandler;

    private ImagesDataSource() {
        this.executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static ImagesDataSource get() {
        if (singleton == null) {
            synchronized (ImagesDataSource.class) {
                if (singleton == null) {
                    singleton = new ImagesDataSource();
                }
            }
        }
        return singleton;
    }

    public void loadImages(final LoadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<String> images = getImages();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (images != null && !images.isEmpty()) {
                            callback.onSuccess(images);
                        } else {
                            callback.onFail();
                        }
                    }
                });
            }
        });
    }

    private List<String> getImages() {
        List<String> images = new ArrayList<String>();
        String directoryPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "OneImageFetcher";
        File directory = new File(directoryPath);
        Log.d(TAG, "getImages directoryPath:" + directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.exists() && file.isFile()) {
                        images.add("file://" + file.getAbsolutePath());
                    }
                }
            }
        }
        Log.d(TAG, "getImages images:" + images);
        return images;
    }

    public interface LoadCallback {
        void onSuccess(List<String> images);
        void onFail();
    }
}
