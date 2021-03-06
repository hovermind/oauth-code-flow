package com.hovermind.oauthacf.utils;

import android.net.Uri;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Created by hassan on 2017/06/22.
 */

public class UriUtil {
    private static final String TAG = "UriUtil";

    public static Uri makeAuthUri(String authEndPoint, Map<String, String> oauthUriMap) {

        Request request = new Request.Builder().url(authEndPoint).build();
        HttpUrl.Builder builder = request.url().newBuilder();
        for (Map.Entry<String, String> param : oauthUriMap.entrySet()) {
            builder.addQueryParameter(param.getKey(), param.getValue());
        }
        HttpUrl httpUrl = builder.build();
        return Uri.parse(httpUrl.toString());
    }
}

