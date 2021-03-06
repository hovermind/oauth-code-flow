package com.hovermind.oauthacf.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hassan on 2017/06/22.
 */

public class ResourceUtil {
    private static final String TAG = "ResourceUtil";

    public static Map<String, String> getAuthUriMap(Context ctx, final String mapName, int mapResId) {
        Map<String, String> map = null;
        String key = null;
        String value = null;

        XmlResourceParser parser = ctx.getResources().getXml(mapResId);
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(TAG, "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals(mapName)) {
                        boolean isLinked = parser.getAttributeBooleanValue(null, "linked", false);
                        map = isLinked ? new LinkedHashMap<String, String>() : new HashMap<String, String>();
                    } else if (parser.getName().equals("entry")) {
                        key = parser.getAttributeValue(null, "key");
                        if (null == key) {
                            parser.close();
                            return null;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("entry")) {
                        map.put(key, value);
                        key = null;
                        value = null;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (null != key) {
                        int resId = ctx.getResources().getIdentifier(parser.getText(), "string", ctx.getPackageName());
                        if(resId > 0){
                            value = ctx.getResources().getString(resId);
                            Log.d(TAG, "getAuthUriMap: resId => " + resId + " | resValue => " + value);
                        }else{
                            Log.d(TAG, "getAuthUriMap: entry does refer to string resource");
                            value = parser.getText();
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }


    public static Map<String, String> getAuthUriMap(Context ctx, int mapResId) {
        return getAuthUriMap(ctx, "uri_map", mapResId);
    }
}
