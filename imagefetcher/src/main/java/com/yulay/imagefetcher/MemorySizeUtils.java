package com.yulay.imagefetcher;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

public class MemorySizeUtils {
    private MemorySizeUtils() {};

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static boolean isLowMemoryDevice(Context context) {
        // Explicitly check with an if statement, on some devices both parts of boolean expressions
        // can be evaluated even if we'd normally expect a short circuit.
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return activityManager.isLowRamDevice();
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static boolean isLargeHeap(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static int getLargeMemoryClass(ActivityManager am) {
        return am.getLargeMemoryClass();
    }
}
