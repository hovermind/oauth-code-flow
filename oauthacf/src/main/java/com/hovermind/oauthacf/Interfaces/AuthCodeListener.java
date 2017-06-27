package com.hovermind.oauthacf.Interfaces;

import android.net.Uri;

/**
 * Created by hassan on 2017/06/27.
 */

public interface AuthCodeListener {
    void onAuthCodeReceived(Uri authResponseUri);

    void onAuthCodeReceived(String authCode, String idToken);

    void onAuthCodeError(String errorMsg);
}
