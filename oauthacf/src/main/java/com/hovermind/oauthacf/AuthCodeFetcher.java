package com.hovermind.oauthacf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.util.Log;

import com.hovermind.oauthacf.utils.ResourceUtil;
import com.hovermind.oauthacf.utils.UriUtil;

import java.util.Map;

/**
 * Created by hassan on 2017/06/22.
 */

public class AuthCodeFetcher{
    private final String TAG = AuthCodeFetcher.class.getSimpleName();

    private Context mContext;

    private String mAuthEndpoint;
    private Uri mAuthorizationUri;
    private Map<String, String> mAuthUriMap = null;



    public AuthCodeFetcher(Context mContext, @StringRes int authEndpointResId, @XmlRes int uriMapResId) {
        this.mContext = mContext;
        mAuthEndpoint = mContext.getResources().getString(authEndpointResId);
        mAuthUriMap = ResourceUtil.getAuthUriMap(mContext, "uri-map", uriMapResId);
    }

    public AuthCodeFetcher(Context mContext, @StringRes int authEndpointResId, String mapName, @XmlRes int uriMapResId) {
        this.mContext = mContext;
        mAuthEndpoint = mContext.getResources().getString(authEndpointResId);
        mAuthUriMap = ResourceUtil.getAuthUriMap(mContext, mapName, uriMapResId);
    }

    public void getAuthCode(AuthCodeListener listener){
        if(mAuthEndpoint == null || mAuthUriMap == null){
            Log.d(TAG, "getAuthCode: Authorization end point or Uri map is null, did you create xml => uri_map?");
            listener.onAuthCodeError("authorization end point or Uri map is null");
            return;
        }

        // set callback
        RedirectUriReceiverActivity.setAuthCodeListener(listener);

        // get authorization uri
        mAuthorizationUri = UriUtil.makeAuthUri(mAuthEndpoint, mAuthUriMap);

        // WebView
        Intent intent = new Intent(Intent.ACTION_VIEW, mAuthorizationUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(intent);
    }

    public interface AuthCodeListener{
        void onAuthCodeReceived(String authCode, String idToken);
        void onAuthCodeError(String errorMsg);
    }
}
