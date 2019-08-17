package com.yulay.imagefetcher;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class DiskLruCacheWrapper implements DiskCache {
    private static final String TAG = "DiskLruCacheWrapper";
    /** {@value */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 Kb
    /** {@value */
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    /** {@value */
    public static final int DEFAULT_COMPRESS_QUALITY = 100;

    protected DiskLruCache cache;
    protected File cacheDir;
    protected long cacheMaxSize;

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    protected Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = DEFAULT_COMPRESS_QUALITY;

    public DiskLruCacheWrapper(File cacheDir, long cacheMaxSize) {
        this.cacheDir = cacheDir;
        this.cacheMaxSize = cacheMaxSize;
        /*initCache(cacheDir, cacheMaxSize);*/
    }

    private void initCache(File cacheDir, long cacheMaxSize) {
        try {
            cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize);
            Log.e(TAG, "initCache - " + cacheDir + " " + cacheMaxSize);
        } catch (IOException e) {
            Log.e(TAG, "initCache - " + e);
        }
    }

    @Override
    public void open() {
        initCache(cacheDir, cacheMaxSize);
    }

    @Override
    public File get(String data) {
        if (cache != null) {
            DiskLruCache.Snapshot snapshot = null;
            try {
                snapshot = cache.get(DiskCacheUtils.hashKeyForDisk(data));
                return snapshot == null ? null : snapshot.getFile(0);
            } catch (IOException e) {
                Log.e(TAG, "initCache - " + e);
                return null;
            } finally {
                if (snapshot != null) {
                    snapshot.close();
                }
            }
        }
        return null;
    }

    @Override
    public void put(String data, Bitmap bitmap){
        if (cache != null) {
            OutputStream os = null;
            boolean savedSuccessfully = false;
            try {
                DiskLruCache.Editor editor = cache.edit(DiskCacheUtils.hashKeyForDisk(data));
                if (editor == null) {
                    return;
                }

                os = new BufferedOutputStream(editor.newOutputStream(0), bufferSize);
                savedSuccessfully = bitmap.compress(compressFormat, compressQuality, os);
                if (savedSuccessfully) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            } catch (IOException e) {
                Log.e(TAG, "put - " + e);
            } finally {
                Utils.closeQuietly(os);
            }
        }
    }

    @Override
    public void clear() {
        if (cache != null) {
            try {
                cache.delete();
            } catch (IOException e) {
                Log.e(TAG, "clear - " + e);
            }

            open();
        }
    }

    @Override
    public void flush() {
        if (cache != null) {
            try {
                cache.flush();
            } catch (IOException e) {
                Log.e(TAG, "flush - " + e);
            }
        }
    }

    @Override
    public void close() {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException e) {
                Log.e(TAG, "close - " + e);
            }
            cache = null;
        }
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
