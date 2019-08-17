package com.yulay.imagefetcher;

import android.content.res.Resources;

public class ResourceImageProcessor extends ImageProcessor {

    private final Resources resources;

    public ResourceImageProcessor(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean canProcess(Object data) {
        return data instanceof Integer;
    }

    @Override
    public Result process(Object data, DisplayOptions options) {
        int resourceId = (Integer) data;
        return new Result(ImageDecodeHelper.decodeSampledBitmapFromResource(resources, resourceId, options), null);
    }
}
