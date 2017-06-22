package com.hovermind.oauthacf.utils;

import android.net.Uri;
import android.util.Log;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Created by hassan on 2017/06/22.
 */

public class UriUtil {
    public static Uri getAuthorizationUri(String authEndPoint, Map<String, String> oauthUriMap) {

        Request request = new Request.Builder().url(authEndPoint).build();
        HttpUrl.Builder builder = request.url().newBuilder();
        for (Map.Entry<String, String> param : oauthUriMap.entrySet()) {
            builder.addQueryParameter(param.getKey(), param.getValue());
        }
        HttpUrl httpUrl = builder.build();
        Log.d("UriUtil", "getAuthorizationUri => " + httpUrl.toString());
        return Uri.parse(httpUrl.toString());
    }
}

