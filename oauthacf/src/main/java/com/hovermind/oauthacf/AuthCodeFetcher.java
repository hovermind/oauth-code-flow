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

    private String mAuthEndPointResId;
    private Map<String, String> mAuthUriMapResId = null;
    private Uri mAuthorizationUri;


    public AuthCodeFetcher(Context mContext, @StringRes int authEndPointResId, @XmlRes int uriMapResId) {
        this.mContext = mContext;
        mAuthEndPointResId = mContext.getResources().getString(authEndPointResId);
        mAuthUriMapResId = ResourceUtil.getAuthUriMap(mContext, uriMapResId);
    }

    public void getAuthCode(AuthCodeListener listener){
        if(mAuthEndPointResId == null || mAuthUriMapResId == null){
            Log.d(TAG, "getAuthCode: Authorization end point or Uri map is null, did you create xml => uri_map?");
            listener.onAuthCodeError("authorization end point or Uri map is null");
            return;
        }

        // set callback
        RedirectUriReceiverActivity.setAuthCodeListener(listener);

        // get authorization uri
        mAuthorizationUri = UriUtil.getAuthorizationUri(mAuthEndPointResId, mAuthUriMapResId);

        // WebView
        Intent intent = new Intent(Intent.ACTION_VIEW, mAuthorizationUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(intent);
    }

    public interface AuthCodeListener{
        void onAuthCodeReceived(String authCode);
        void onAuthCodeError(String errorMsg);
    }
}
