package com.yulay.imagefetcher;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class FileDiskCache implements DiskCache {
    private static final String TAG = "FileDiskCache";

    /** {@value} */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 Kb
    /** {@value} */
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    /** {@value} */
    public static final int DEFAULT_COMPRESS_QUALITY = 100;

    private static final String TEMP_IMAGE_POSTFIX = ".tmp";

    protected final File cacheDir;

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    protected Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = DEFAULT_COMPRESS_QUALITY;

    public FileDiskCache(File cacheDir) {
        this.cacheDir = cacheDir;
        Log.e(TAG, "FileDiskCache - " + cacheDir);
    }

    @Override
    public void open() {
        // Nothing to do
    }

    @Override
    public File get(String data) {
        return getFile(data);
    }

    @Override
    public void put(String data, Bitmap bitmap) {
        File imageFile = getFile(data);
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        OutputStream os = null;
        boolean savedSuccessfully = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(tmpFile), bufferSize);
            savedSuccessfully = bitmap.compress(compressFormat, compressQuality, os);
        } catch (IOException e) {
            Log.e(TAG, "put - " + e);
        } finally {
            Utils.closeQuietly(os);
            if (savedSuccessfully && !tmpFile.renameTo(imageFile)) {
                savedSuccessfully = false;
            }
            if (!savedSuccessfully) {
                tmpFile.delete();
            }
        }
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Override
    public void flush() {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to do
    }

    /** Returns file object (not null) for incoming image URI. File object can reference to non-existing file. */
    protected File getFile(String imageUri) {
        String fileName = DiskCacheUtils.hashKeyForDisk(imageUri);
        File dir = cacheDir;
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
        }
        return new File(dir, fileName);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }
}
