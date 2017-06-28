package com.hovermind.oauthacf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.util.Log;

import com.hovermind.oauthacf.Interfaces.AuthCodeListener;
import com.hovermind.oauthacf.utils.ResourceUtil;
import com.hovermind.oauthacf.utils.UriUtil;

import java.util.Map;

import static com.hovermind.oauthacf.utils.Constants.NONCE;
import static com.hovermind.oauthacf.utils.Constants.STATE;

/**
 * Created by hassan on 2017/06/22.
 */

public class AuthCodeFetcher {
    private final String TAG = AuthCodeFetcher.class.getSimpleName();

    private Context mContext;
    private String mNonce = null;
    private String mState = null;

    private String mAuthEndpoint = "";
    private Map<String, String> mAuthUriMap = null;

    public AuthCodeFetcher(Context mContext, @StringRes int authEndpointResId, @XmlRes int uriMapResId, String mapName, String mNonce, String mState) {
        this.mContext = mContext;
        mAuthEndpoint = mContext.getResources().getString(authEndpointResId);

        if (mapName != null) {
            mAuthUriMap = ResourceUtil.getAuthUriMap(mContext, mapName, uriMapResId);
        } else {
            mAuthUriMap = ResourceUtil.getAuthUriMap(mContext, uriMapResId);
        }

        this.mNonce = mNonce;
        this.mState = mState;
    }

    public void getAuthCode(final AuthCodeListener listener) {
        if (mAuthEndpoint == null || mAuthUriMap == null) {
            Log.d(TAG, "getAuthCode: authorization end point or Uri map is null, did you create xml? default is oauth_uri_map");
            listener.onAuthCodeError("authorization end point or Uri map is null");
            return;
        }

        if (mNonce != null) mAuthUriMap.put(NONCE, mNonce);
        if (mState != null) mAuthUriMap.put(STATE, mState);

        // get authorization uri
        Uri authorizationUri = UriUtil.makeAuthUri(mAuthEndpoint, mAuthUriMap);
        Log.d(TAG, "getAuthCode:  authorizationUri => " + authorizationUri);

        // set callback
        RedirectUriReceiverActivity.setAuthCodeListener(listener);

        // Sending Intent to Browser
        Intent intent = new Intent(Intent.ACTION_VIEW, authorizationUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(intent);
    }
}
