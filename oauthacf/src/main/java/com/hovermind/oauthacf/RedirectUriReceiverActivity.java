
package com.hovermind.oauthacf;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hovermind.oauthacf.Interfaces.AuthCodeListener;


public class RedirectUriReceiverActivity extends AppCompatActivity {
    private final String TAG = RedirectUriReceiverActivity.class.getSimpleName();

    private static AuthCodeListener mListener;

    public static void setAuthCodeListener(AuthCodeListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        Log.d(TAG, "onCreate: intent data => " + getIntent().getDataString());

        Uri uri = Uri.parse(getIntent().getDataString());
        if (mListener != null) {
            mListener.onAuthCodeReceived(uri);
        }
        finish();
    }

}
