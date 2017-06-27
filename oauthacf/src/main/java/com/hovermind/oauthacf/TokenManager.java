package com.hovermind.oauthacf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.hovermind.oauthacf.Interfaces.AuthCodeListener;
import com.hovermind.oauthacf.Interfaces.TokenListener;
import com.hovermind.oauthacf.Interfaces.TokenRefreshListener;
import com.hovermind.oauthacf.Interfaces.TokenValidationListener;
import com.hovermind.oauthacf.api.TokenService;
import com.hovermind.oauthacf.api.models.IdTokenPayload;
import com.hovermind.oauthacf.api.models.Token;
import com.hovermind.oauthacf.utils.JwtStructure;
import com.hovermind.oauthacf.utils.ResourceUtil;
import com.hovermind.oauthacf.utils.TokenUtil;
import com.hovermind.oauthacf.utils.UriUtil;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hassan on 2017/06/23.
 */

public class TokenManager {
    private static String TAG = TokenManager.class.getName();
    private HttpLoggingInterceptor mLoggingInterceptor;
    private Context mContext;

    private OkHttpClient.Builder mHttpClient;
    private Retrofit mRetrofit;

    private String mBaseUri;
    private String mRedirectUri;
    private String mClientId;
    private String mClientSecret;

    private String mAuthCode;
    private IdTokenPayload mIdTokenPayload;
    private String mEncodedIdToken;
    private String nonce;
    private String state;
    String mHeaderBasic;

    // request params & headers
    private final String CLIENT_ID = "client_id";
    private final String RESPONSE_TYPE = "response_type";
    private final String REDIRECT_URI = "redirect_uri";
    private final String SCOPE = "scope";
    private final String PROMPT = "prompt";
    private final String NONCE = "nonce";
    private final String STATE = "state";
    private final String DISPLAY = "display";
    private final String TOKEN = "token";
    private final String TOKEN_TYPE_HINT = "token_type_hint";
    private final String GRANT_TYPE = "grant_type";
    private final String REFRESH_TOKEN = "refresh_token";
    private final String CODE = "code";
    private final String CODE_ID_TOKEN = "code id_token";

    private static TokenManager instance = null;
    private static TokenManager defaultInstance = null;

    // singleton
    public static TokenManager getInstance(Context context, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        if (instance == null) {
            instance = new TokenManager(context, clientIdResId, clientSecretResId, baseUriResId, redirectUriResId);
        }
        return instance;
    }

    // singleton
    public static TokenManager getDefaultInstance(Context context) {
        if (defaultInstance == null) {
            defaultInstance = new TokenManager(context, R.string.client_id, R.string.client_secret, R.string.base_uri, R.string.redirect_uri);
        }
        return defaultInstance;
    }

    // private constructor
    private TokenManager(Context mContext, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        this.mContext = mContext;

        // res
        mClientId = mContext.getResources().getString(clientIdResId);
        mClientSecret = mContext.getResources().getString(clientSecretResId);
        mBaseUri = mContext.getResources().getString(baseUriResId);
        mRedirectUri = mContext.getResources().getString(redirectUriResId);

        mHeaderBasic = Base64.encodeToString(String.format("%s:%s", mClientId, mClientSecret).getBytes(), Base64.NO_WRAP);

        // okhttp3 logging interceptor
        mLoggingInterceptor = new HttpLoggingInterceptor();
        mLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    public void getAuthCode(@StringRes int authEndpointResId, @XmlRes int authUriMapResId, AuthCodeListener listener) {
        nonce = RandomStringUtils.randomAlphanumeric(12);
        state = RandomStringUtils.randomAlphanumeric(12);

        String mAuthEndpoint = mContext.getResources().getString(authEndpointResId);
        Map<String, String> mAuthUriMap = ResourceUtil.getAuthUriMap(mContext, "auth_uri_map", authUriMapResId);
        mAuthUriMap.put(NONCE, nonce);
        mAuthUriMap.put(STATE, state);

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

    public void getAuthCode(AuthCodeListener listener) {
        getAuthCode(R.string.auth_endpoint, R.xml.uri_map, listener);
    }

    // fetch token for authorization response uri
    public void fetchToken(@NonNull Uri authResponseUri, final TokenListener listener) {

        mAuthCode = authResponseUri.getQueryParameter("code");
        Log.d(TAG, "fetchToken:  AuthCode => " + mAuthCode);

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "authorization_code");
        params.put(CODE, mAuthCode);
        params.put(REDIRECT_URI, mRedirectUri);

        // Client
        mHttpClient = new OkHttpClient.Builder();
        mHttpClient.followRedirects(true);
        mHttpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Authorization", "Basic " + mHeaderBasic)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                return chain.proceed(request);
            }
        });

        // okHttp3 logging interceptor
        mHttpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(mHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // api call
        TokenService tokenService = mRetrofit.create(TokenService.class);
        Call<Token> tokenCall = tokenService.fetchToken(params);
        tokenCall.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {

                    Token token = response.body();
                    listener.onTokenReceived(token);
                    //validateEncodedIdToken(token, listener);

                } else {
                    if (listener != null) {
                        listener.onTokenError("Failed to get token. Response unsuccessful(Code:" + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                if (listener != null) {
                    listener.onTokenError("Failure: " + t.getMessage());
                }
            }
        });

    }

    // fetch token for auth code
    public void fetchToken(@NonNull String authCode, final TokenListener listener) {
        Uri uri = Uri.parse("hm://hovermind.com?code=" + authCode);
        fetchToken(uri, listener);
    }

    // token validation
    public void validateEncodedIdToken(@NonNull final Token token, @StringRes int issResId, final TokenValidationListener listener) {
        String iss = mContext.getResources().getString(issResId);

        mEncodedIdToken = token.getIdToken();
        Log.d(TAG, "Encoded Id Token => " + mEncodedIdToken);

        JwtStructure jwtStructure = TokenUtil.parseEncodedIdToken(mEncodedIdToken);
        String payloadString = jwtStructure.getPayload();

        if (payloadString != null && payloadString != "") {
            Log.d(TAG, "validateEncodedIdToken: PayloadString => " + payloadString);

            mIdTokenPayload = new Gson().fromJson(payloadString, IdTokenPayload.class);
            Log.d(TAG, "iss   : " + mIdTokenPayload.getIss());
            Log.d(TAG, "exp   : " + mIdTokenPayload.getExp());
            Log.d(TAG, "iat   : " + mIdTokenPayload.getIat());
            Log.d(TAG, "nonce : " + mIdTokenPayload.getNonce());
            Log.d(TAG, "azp : " + mIdTokenPayload.getAzp());

            long oneDay = 24 * 60 * 60;
            long fiveMinutes = 5 * 60;
            Long now = new Date().getTime() / 1000;

            if (iss.equals(mIdTokenPayload.getIss())
                    && mIdTokenPayload.getAzp().equals(mClientId)
                    && (mIdTokenPayload.getExp() - now) < (oneDay + fiveMinutes)
                    && (mIdTokenPayload.getIat() - now) < fiveMinutes
                    && nonce.equals(mIdTokenPayload.getNonce())) {

                Log.d(TAG, "validateEncodedIdToken => token is valid");
                if (listener != null) {
                    listener.onValidationOk(token);
                }

            } else {

                Log.e(TAG, "validateEncodedIdToken => token is not valid");
                if (listener != null) {
                    listener.onValidationFailed("Token is not valid");
                }
            }

        } else {
            Log.d(TAG, "validateEncodedIdToken:  PayloadString is null");
            if (listener != null) {
                listener.onValidationFailed("Payload of id token is null/empty");
            }
        }
    }

    public void validateEncodedIdToken(@NonNull final Token token, final TokenValidationListener listener) {
        validateEncodedIdToken(token, R.string.iss, listener);
    }

    // refresh token: synchronous call
    public Token refreshToken(String refreshToken, @StringRes final int issResId) {

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "refresh_token");
        params.put(REFRESH_TOKEN, refreshToken);

        // Client
        mHttpClient = new OkHttpClient.Builder();
        mHttpClient.followRedirects(true);
        mHttpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Host", mContext.getResources().getString(issResId))
                        .addHeader("Authorization", "Basic " + mHeaderBasic)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                return chain.proceed(request);
            }
        });

        // okHttp3 logging interceptor
        mHttpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(mHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Request
        Token token = null;

        try {
            TokenService tokenService = mRetrofit.create(TokenService.class);
            Call<Token> refreshCall = tokenService.refreshToken(params);
            token = refreshCall.execute().body();
            Log.d(TAG, "refreshToken: token refreshed. new access token => " + token.getAccessToken());
        } catch (IOException e) {
            Log.d(TAG, "refreshToken: exception occurred for synchronous call. error => " + e.getMessage());
        }

        return token;
    }

    public Token refreshToken(String refreshToken) {
        return refreshToken(refreshToken, R.string.iss);
    }

    // refresh token: asynchronous call, with callback
    public void refreshToken(String refreshToken, @StringRes final int issResId, final TokenRefreshListener listener) {

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "refresh_token");
        params.put(REFRESH_TOKEN, refreshToken);

        // Client
        mHttpClient = new OkHttpClient.Builder();
        mHttpClient.followRedirects(true);
        mHttpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Host", mContext.getResources().getString(issResId))
                        .addHeader("Authorization", "Basic " + mHeaderBasic)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                return chain.proceed(request);
            }
        });

        // okHttp3 logging interceptor
        mHttpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(mHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Request
        TokenService tokenService = mRetrofit.create(TokenService.class);
        Call<Token> rCall = tokenService.refreshToken(params);
        rCall.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "refreshTokenWithCallback: new token received. newAccessToken => " + response.body().getAccessToken());
                    if (listener != null) {
                        listener.onTokenRefreshed(response.body());
                    }
                } else {
                    Log.d(TAG, "refreshTokenWithCallback: response unsuccessful (Code:" + response.code() + ")");
                    if (listener != null) {
                        listener.onTokenError("Response unsuccessful (Code:" + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.d(TAG, "refreshTokenWithCallback: Failure response. error => " + t.getMessage());
                if (listener != null) {
                    listener.onTokenError("Failure response. error => " + t.getMessage());
                }
            }
        });
    }

    public void refreshToken(String refreshToken, final TokenRefreshListener listener) {
        refreshToken(refreshToken, R.string.iss, listener);
    }

}
