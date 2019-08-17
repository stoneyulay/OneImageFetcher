package com.yulay.imagefetcher;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneImageFetcher extends ImageWorker {
    private static final String TAG = "OneImageFetcher";
    private static final String URI_AND_SIZE_SEPARATOR = "_";
    private static final String WIDTH_AND_HEIGHT_SEPARATOR = "x";
    static volatile OneImageFetcher singleton = null;
    private final List<ImageProcessor> imageProcessors;

    private OneImageFetcher(Context context, List<ImageProcessor> extraImageProcessors,
                            ExecutorService loadExecutor, ExecutorService cacheExecutor,
                            MemoryCache memoryCache, DiskCache diskCache,
                            DisplayOptions defaultDisplayOptions, boolean loggingEnabled) {
        super(context, loadExecutor, cacheExecutor, memoryCache, diskCache, defaultDisplayOptions, loggingEnabled);

        int builtInProcessors = 3; // Adjust this as internal processors are added or removed.
        int extraCount = (extraImageProcessors != null ? extraImageProcessors.size() : 0);
        List<ImageProcessor> allImageProcessors =
                new ArrayList<ImageProcessor>(builtInProcessors + extraCount);

        allImageProcessors.add(new ResourceImageProcessor(context.getResources()));
        if (extraImageProcessors != null) {
            allImageProcessors.addAll(extraImageProcessors);
        }
        allImageProcessors.add(new FileStringImageProcessor());
        allImageProcessors.add(new NetworkImageProcessor());
        imageProcessors = Collections.unmodifiableList(allImageProcessors);
    }

    public static OneImageFetcher with(Context context) {
        if (singleton == null) {
            synchronized (OneImageFetcher.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    public static void setSingletonInstance(OneImageFetcher oneImageFetcher) {
        synchronized (OneImageFetcher.class) {
            if (singleton != null) {
                Log.d(TAG, "Singleton instance already exists.");
                return;
                /*throw new IllegalStateException("Singleton instance already exists.");*/
            }
            singleton = oneImageFetcher;
        }
    }

    static DisplayOptions createDefualtImageOptions(Context context) {
        boolean isLowMemory = MemorySizeUtils.isLowMemoryDevice(context);
        return new DisplayOptions.Builder()
                .bitmapConfig(isLowMemory ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888)
                .fadeIn(true)
                .build();
    }

    static MemoryCache createDefaultMemoryCache(Context context, int memoryCacheSize) {
        if (memoryCacheSize == 0) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = am.getMemoryClass();
            if (Utils.hasHoneycomb() && MemorySizeUtils.isLargeHeap(context)) {
                memoryClass = MemorySizeUtils.getLargeMemoryClass(am);
            }
            memoryCacheSize = 1024 * 1024 * memoryClass / 8;
        }
        return new MemoryLruCacheWrapper(memoryCacheSize);
    }

    static DiskCache createDefaultDiskCache(Context context, long diskCacheSize) {
        if (diskCacheSize > 0) {
            File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
            return new DiskLruCacheWrapper(individualCacheDir, diskCacheSize);
        }
        File cacheDir = StorageUtils.getCacheDirectory(context);
        return new UnlimitedFileDiskCache(cacheDir);
    }

    @Override
    protected Bitmap processBitmap(Object data, DisplayOptions options) {
        if (mLoggingEnabled) {
            Log.d(TAG, "processBitmap  " + data + " " + options);
        }
        Bitmap bitmap = null;
        try {
            ImageProcessor imageProcessor = findImageProcessor(data);

            if (imageProcessor != null) {
                ImageProcessor.Result result;
                result = imageProcessor.process(data, options);
                if (result != null) {
                    bitmap = result.getBitmap();
                    if (bitmap == null) {
                        InputStream is = result.getStream();
                        try {
                            bitmap = ImageDecodeHelper.decodeSampledBitmapFromStream(is, options);
                        } finally {
                            Utils.closeQuietly(is);
                        }
                    }
                }
            } else {
                Log.e(TAG, "processBitmap can not process " + data);
            }
        } catch (Exception e) {
            Log.e(TAG, "processBitmap exception " + e);
        }

        return bitmap;
    }

    private ImageProcessor findImageProcessor(Object data) {
        for (int i = 0, count = imageProcessors.size(); i < count; i++) {
            ImageProcessor imageProcessor = imageProcessors.get(i);
            if (imageProcessor.canProcess(data)) {
                return imageProcessor;
            }
        }

        return null;
    }

    public DisplayOptionsCreator load(Object data) {
        return new DisplayOptionsCreator(this, data);
    }

    @Override
    protected String getCachedKey(Object data, DisplayOptions options) {
        String dataString = String.valueOf(data);
        return new StringBuilder(dataString).append(URI_AND_SIZE_SEPARATOR).append(options.getWidth()).append(WIDTH_AND_HEIGHT_SEPARATOR).append(options.getHeight()).toString();
    }

    public static class Builder {
        private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls overlap each other";
        private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other";

        private final Context context;
        private List<ImageProcessor> imageProcessors;
        private int memoryCacheSize = 0;
        private long diskCacheSize = 0;

        private ExecutorService loadExecutor;
        private ExecutorService cacheExecutor;
        private MemoryCache memoryCache = null;
        private DiskCache diskCache = null;

        private DisplayOptions defaultDisplayOptions;
        private boolean loggingEnabled;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

        public Builder addImageProcessor(ImageProcessor imageProcessor) {
            if (imageProcessor == null) {
                throw new IllegalArgumentException("ImageProcessor must not be null.");
            }
            if (imageProcessors == null) {
                imageProcessors = new ArrayList<ImageProcessor>();
            }
            if (imageProcessors.contains(imageProcessor)) {
                Log.w(TAG, "RequestHandler already registered.");
                return this;
                /*throw new IllegalStateException("RequestHandler already registered.");*/
            }
            imageProcessors.add(imageProcessor);
            return this;
        }

        public Builder loadExecutor(ExecutorService loadExecutor) {
            this.loadExecutor = loadExecutor;
            return this;
        }

        public Builder cacheExecutor(ExecutorService cacheExecutor) {
            this.cacheExecutor = cacheExecutor;
            return this;
        }

        public Builder memoryCacheSize(int memoryCacheSize) {
            if (memoryCacheSize <= 0) throw new IllegalArgumentException("memoryCacheSize must be a positive number");

            if (memoryCache != null) {
                Log.w(TAG, WARNING_OVERLAP_MEMORY_CACHE);
            }

            this.memoryCacheSize = memoryCacheSize;
            return this;
        }

        public Builder memoryCacheSizePercentage(int availableMemoryPercent) {
            if (availableMemoryPercent <= 0 || availableMemoryPercent >= 100) {
                throw new IllegalArgumentException("availableMemoryPercent must be in range (0 < % < 100)");
            }

            if (memoryCache != null) {
                Log.w(TAG, WARNING_OVERLAP_MEMORY_CACHE);
            }

            long availableMemory = Runtime.getRuntime().maxMemory();
            memoryCacheSize = (int) (availableMemory * (availableMemoryPercent / 100f));
            return this;
        }

        public Builder diskCacheSize(int maxCacheSize) {
            if (maxCacheSize <= 0) throw new IllegalArgumentException("maxCacheSize must be a positive number");

            if (diskCache != null) {
                Log.w(TAG, WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }

            this.diskCacheSize = maxCacheSize;
            return this;
        }

        public Builder diskCache(DiskCache diskCache) {
            if (diskCacheSize > 0) {
                Log.w(TAG, WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }

            this.diskCache = diskCache;
            return this;
        }

        public Builder defaultDisplayOptions(DisplayOptions defaultDisplayOptions) {
            this.defaultDisplayOptions = defaultDisplayOptions;
            return this;
        }

        public Builder loggingEnabled(boolean enabled) {
            this.loggingEnabled = enabled;
            return this;
        }

        public OneImageFetcher build() {
            Context context = this.context;

            if (loadExecutor == null) {
                loadExecutor = (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;
            }

            if (cacheExecutor == null) {
                cacheExecutor = Executors.newSingleThreadExecutor();
            }

            if (memoryCache == null) {
                memoryCache = createDefaultMemoryCache(context, memoryCacheSize);
            }

            if (diskCache == null) {
                diskCache = createDefaultDiskCache(context, diskCacheSize);
            }

            if (defaultDisplayOptions == null) {
                defaultDisplayOptions = createDefualtImageOptions(context);
            }

            return new OneImageFetcher(context, imageProcessors,
                    loadExecutor, cacheExecutor,
                    memoryCache, diskCache,
                    defaultDisplayOptions, loggingEnabled);
        }
    }
}
