package com.yulay.imagefetcher;

import java.io.File;

public class UnlimitedFileDiskCache extends FileDiskCache {
    public UnlimitedFileDiskCache(File cacheDir) {
        super(cacheDir);
    }
}
