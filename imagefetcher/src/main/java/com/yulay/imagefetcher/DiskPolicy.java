package com.yulay.imagefetcher;

public enum DiskPolicy {

    /** Skips disk cache lookup when processing a request. */
    NO_CACHE(1 << 0),
    /**
     * Skips storing the final result into disk cache. Useful for one-off requests
     * to avoid evicting other bitmaps from the cache.
     */
    NO_STORE(1 << 1);

    static boolean shouldReadFromDiskCache(int diskPolicy) {
        return (diskPolicy & DiskPolicy.NO_CACHE.index) == 0;
    }

    static boolean shouldWriteToDiskCache(int diskPolicy) {
        return (diskPolicy & DiskPolicy.NO_STORE.index) == 0;
    }

    final int index;

    private DiskPolicy(int index) {
        this.index = index;
    }
}
