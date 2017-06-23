package com.hovermind.oauthacf;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;

import com.hovermind.oauthacf.api.TokenService;
import com.hovermind.oauthacf.api.models.Token;
import com.hovermind.oauthacf.api.models.TokenInfo;

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

    private String mBaseUri;
    private String mRedirectUri;
    private String mClientId;
    private String mClientSecret;

    private String mAuthCode;
    private String mIdToken;
    private String nonce;
    private String mIss;
    String mHeaderBasic;

    // request params & headers
    private final String CLIENT_ID = "client_id";
    private final String RESPONSE_TYPE = "response_type";
    private final String REDIRECT_URI = "redirect_uri";
    private final String SCOPE = "scope";
    private final String PROMPT = "prompt";
    private final String NONCE = "nonce";
    private final String DISPLAY = "display";
    private final String TOKEN = "token";
    private final String TOKEN_TYPE_HINT = "token_type_hint";
    private final String GRANT_TYPE = "grant_type";
    private final String REFRESH_TOKEN = "refresh_token";
    private final String CODE = "code";

    private static TokenManager instance = null;
    private static TokenManager defaultInstance = null;

    // singleton
    public static TokenManager getInstance(Context context, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId, @StringRes int issResId) {
        if (instance == null) {
            instance = new TokenManager(context, clientIdResId, clientSecretResId, baseUriResId, redirectUriResId, issResId);
        }
        return instance;
    }

    // singleton
    public static TokenManager getDefaultInstance(Context context) {
        if (defaultInstance == null) {
            defaultInstance = new TokenManager(context, R.string.client_id, R.string.client_secret, R.string.base_uri, R.string.redirect_uri, R.string.iss);
        }
        return defaultInstance;
    }

    // private constructor
    private TokenManager(Context mContext, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId, @StringRes int issResId) {
        this.mContext = mContext;

        // res
        mClientId = mContext.getResources().getString(clientIdResId);
        mClientSecret = mContext.getResources().getString(clientSecretResId);
        mBaseUri = mContext.getResources().getString(baseUriResId);
        mRedirectUri = mContext.getResources().getString(redirectUriResId);
        mIss = mContext.getResources().getString(issResId);

        mHeaderBasic = Base64.encodeToString(String.format("%s:%s", mClientId, mClientSecret).getBytes(), Base64.NO_WRAP);

        // okhttp interceptor
        mLoggingInterceptor = new HttpLoggingInterceptor();
        mLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    // fetch token for authorization uri
    public void fetchToken(@NonNull Uri authResponseUri, final TokenListener listener) {

        mAuthCode = authResponseUri.getQueryParameter("code");
        mIdToken = authResponseUri.getQueryParameter("id_token") != null ? authResponseUri.getQueryParameter("id_token") : "-1";

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "authorization_code");
        params.put(CODE, mAuthCode);
        params.put(REDIRECT_URI, mRedirectUri);

        // Client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.followRedirects(true);
        httpClient.addInterceptor(new Interceptor() {
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

        // okhttp logging
        httpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // api call
        TokenService tokenService = retrofit.create(TokenService.class);
        Call<Token> tokenCall = tokenService.fetchToken(params);
        tokenCall.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    Token token = response.body();
                    // checking whether validation needed or not
                    if (!"-1".equals(mIdToken)) {
                        Log.d(TAG, "fetchToken: onResponse() => id token is present, need for validation");

                        token.setIdToken(mIdToken);
                        Log.d(TAG, "fetchToken: onResponse() => id token == " + mIdToken);

                        validateToken(token, listener);
                    } else {
                        Log.d(TAG, "fetchToken: onResponse() => id token is not present, no need for validation");
                        if (listener != null) {
                            listener.onTokenReceived(response.body());
                        }
                    }
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

    // refresh token synchronous call
    public Token refreshToken(String refreshToken) {

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "refresh_token");
        params.put(REFRESH_TOKEN, refreshToken);

        // Client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.followRedirects(true);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Host", mIss)
                        .addHeader("Authorization", "Basic " + mHeaderBasic)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                return chain.proceed(request);
            }
        });

        httpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Request
        Token token = null;
        try {
            TokenService tokenService = retrofit.create(TokenService.class);
            Call<Token> refreshCall = tokenService.refreshToken(params);
            token = refreshCall.execute().body();
            Log.d(TAG, "refreshToken: token refreshed. new access token => " + token.getAccessToken());
        } catch (IOException e) {
            Log.d(TAG, "refreshToken: exception occurred for synchronous call. error => " + e.getMessage());
        }
        return token;
    }

    // refresh token with callback
    public void refreshToken(String refreshToken, final TokenRefreshListener listener) {

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "refresh_token");
        params.put(REFRESH_TOKEN, refreshToken);

        // Client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.followRedirects(true);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Host", mIss)
                        .addHeader("Authorization", "Basic " + mHeaderBasic)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                return chain.proceed(request);
            }
        });

        httpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Request
        TokenService tokenService = retrofit.create(TokenService.class);
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

    private void validateToken(final Token token, final TokenListener listener) {
        Log.d(TAG, "validateToken => ");

        String idToken = token.getIdToken();
        String refreshToken = token.getRefreshToken();
        String accessToken = token.getAccessToken();

        Log.d(TAG, "IdToken     : " + idToken);
        Log.d(TAG, "RefreshToken: " + refreshToken);
        Log.d(TAG, "AccessToken : " + accessToken);

        // Client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(mLoggingInterceptor);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TokenService tokenService = retrofit.create(TokenService.class);
        Call<TokenInfo> checkingCall = tokenService.checkIdToken(idToken);
        checkingCall.enqueue(new Callback<TokenInfo>() {
            @Override
            public void onResponse(Call<TokenInfo> call, Response<TokenInfo> response) {
                if (response.isSuccessful()) {
                    TokenInfo tokenInfo = response.body();

                    long oneDay = 24 * 60 * 60;
                    long fiveMinutes = 5 * 60;
                    Long now = new Date().getTime() / 1000;
                    nonce = RandomStringUtils.randomAlphanumeric(12);


                    Log.d(TAG, "TokenInfo => ");
                    Log.d(TAG, "iss   : " + tokenInfo.getIss());
                    Log.d(TAG, "sub   : " + tokenInfo.getSub());
                    Log.d(TAG, "aud   : " + tokenInfo.getAud());
                    Log.d(TAG, "exp   : " + tokenInfo.getExp());
                    Log.d(TAG, "iat   : " + tokenInfo.getIat());
                    Log.d(TAG, "nonce : " + tokenInfo.getNonce());
                    Log.d(TAG, "cHash: " + tokenInfo.getCHash());
                    Log.d(TAG, "acr   : " + tokenInfo.getAcr());
                    Log.d(TAG, "amr   : " + tokenInfo.getAmr());

                    if (tokenInfo.getIss().equals(mIss)
                            && tokenInfo.getAud().equals(mClientId)
                            && (tokenInfo.getExp() - now) < (oneDay + fiveMinutes)
                            && (tokenInfo.getIat() - now) < fiveMinutes
                            && tokenInfo.getNonce().equals(nonce)) {

                        Log.i(TAG, "validateToken: token is valid");
                        if (listener != null) {
                            listener.onTokenReceived(token);
                        }

                    } else {
                        Log.e(TAG, "validateToken: token is not valid");
                        if (listener != null) {
                            listener.onTokenError("");
                        }
                    }


                } else {
                    Log.d(TAG, "validateToken: api call is unsuccessful");
                    if (listener != null) {
                        listener.onTokenError("TokenValidation: unsuccessful response (Code:" + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenInfo> call, Throwable t) {
                Log.d(TAG, "validateToken: onFailure()");
                if (listener != null) {
                    listener.onTokenError("TokenValidation: Failure response. error => " + t.getMessage());
                }
            }
        });

    }


    public interface TokenListener {
        void onTokenReceived(Token token);

        void onTokenError(String errorMsg);
    }

    public interface TokenRefreshListener {
        void onTokenRefreshed(Token token);

        void onTokenError(String errorMsg);
    }
}
