package com.hovermind.oauthacf.utils;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;

/**
 * Created by hassan on 2017/06/27.
 */

public class TokenUtil {
    private static final String TAG = "TokenUtil";

    public static JwtStructure parseEncodedIdToken(String encodedIdToken) {
        JwtStructure jwtStructure = new JwtStructure(); // to prevent NPE

        String[] tokenParts = encodedIdToken.split("\\."); // regex => "\\." instead of "."
        Log.d(TAG, "parseEncodedIdToken: Token Parts length => " + tokenParts.length);

        if (tokenParts.length > 2) {

            String header = decodeBase64(tokenParts[0]);
            Log.d(TAG, "parseEncodedIdToken: header => " + header);
            String payload = decodeBase64(tokenParts[1]);
            Log.d(TAG, "parseEncodedIdToken: payload => " + payload);
            String signature = decodeBase64(tokenParts[2]);
            Log.d(TAG, "parseEncodedIdToken: signature => " + signature);

            jwtStructure.setHeader(header);
            jwtStructure.setPayload(payload);
            jwtStructure.setSignature(signature);

        } else if (tokenParts.length > 0 && tokenParts.length <= 2) {

            // signature is not present
            String header = decodeBase64(tokenParts[0]);
            String payload = decodeBase64(tokenParts[1]);

            jwtStructure.setHeader(header);
            jwtStructure.setPayload(payload);

        }

        return jwtStructure;
    }

    private static String decodeBase64(String base64) {
        return new String(Base64.decode(base64, Base64.URL_SAFE), StandardCharsets.UTF_8);
    }

}
