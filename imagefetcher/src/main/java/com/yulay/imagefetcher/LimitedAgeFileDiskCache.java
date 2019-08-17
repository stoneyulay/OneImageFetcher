package com.yulay.imagefetcher;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LimitedAgeFileDiskCache extends FileDiskCache {
    private final long maxFileAge;

    private final Map<File, Long> loadingDates = Collections.synchronizedMap(new HashMap<File, Long>());

    public LimitedAgeFileDiskCache(File cacheDir, int maxAge) {
        super(cacheDir);
        this.maxFileAge = maxAge * 1000; // to milliseconds
    }

    @Override
    public File get(String imageUri) {
        File file = super.get(imageUri);
        if (file != null && file.exists()) {
            boolean cached;
            Long loadingDate = loadingDates.get(file);
            if (loadingDate == null) {
                cached = false;
                loadingDate = file.lastModified();
            } else {
                cached = true;
            }

            if (System.currentTimeMillis() - loadingDate > maxFileAge) {
                file.delete();
                loadingDates.remove(file);
            } else if (!cached) {
                loadingDates.put(file, loadingDate);
            }
        }
        return file;
    }

    @Override
    public void put(String data, Bitmap bitmap) {
        super.put(data, bitmap);
        rememberUsage(data);
    }

    @Override
    public void clear() {
        super.clear();
        loadingDates.clear();
    }

    private void rememberUsage(String imageUri) {
        File file = getFile(imageUri);
        long currentTime = System.currentTimeMillis();
        file.setLastModified(currentTime);
        loadingDates.put(file, currentTime);
    }
}
