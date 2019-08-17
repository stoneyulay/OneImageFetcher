package com.yulay.imagefetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkImageProcessor extends StringImageProcessor {
    @Override
    public boolean canProcess(Scheme scheme) {
        return Scheme.HTTP.equals(scheme) || Scheme.HTTPS.equals(scheme);
    }

    @Override
    public Result process(String data, DisplayOptions options) throws IOException{
        return new Result(null, getStreamFromUrl(data, 0));
    }

    public static InputStream getStreamFromUrl(String imageUrl, int readTimeOutMillis) throws IOException {
        InputStream stream;
        URL url = new URL(imageUrl);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        if (readTimeOutMillis > 0) {
            con.setReadTimeout(readTimeOutMillis);
        }
        stream = con.getInputStream();
        return stream;
    }
}
