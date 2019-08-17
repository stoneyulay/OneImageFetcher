package com.yulay.imagefetcher;

public class FileStringImageProcessor extends StringImageProcessor {
    @Override
    public boolean canProcess(Scheme scheme) {
        return Scheme.FILE.equals(scheme);
    }

    @Override
    public Result process(String data, DisplayOptions options) {
        String filePath = Scheme.FILE.crop(data);
        return new Result(ImageDecodeHelper.decodeSampledBitmapFromFile(filePath, options), null);
    }
}
