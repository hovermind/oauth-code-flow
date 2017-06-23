
package com.hovermind.oauthacf;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class RedirectUriReceiverActivity extends AppCompatActivity {
    private final String TAG = RedirectUriReceiverActivity.class.getSimpleName();

    private static AuthCodeFetcher.AuthCodeListener mListener;

    public static void setAuthCodeListener(AuthCodeFetcher.AuthCodeListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        Log.d(TAG, "onCreate: intent data => " + getIntent().getDataString());

        Uri uri = Uri.parse(getIntent().getDataString());
        String authCode = uri.getQueryParameter("code");
        String idToken = uri.getQueryParameter("id_token") != null ? uri.getQueryParameter("id_token") : "-1";

        if (mListener != null) {
            mListener.onAuthCodeReceived(authCode, idToken);
        }
        finish();
    }

}
