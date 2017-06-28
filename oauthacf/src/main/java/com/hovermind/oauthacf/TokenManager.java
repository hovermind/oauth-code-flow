package com.hovermind.oauthacf;

import android.content.Context;
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
import com.hovermind.oauthacf.api.models.BaseJwtModel;
import com.hovermind.oauthacf.api.models.BaseToken;
import com.hovermind.oauthacf.api.models.JwtModel;
import com.hovermind.oauthacf.api.models.Token;
import com.hovermind.oauthacf.utils.JwtStructure;
import com.hovermind.oauthacf.utils.TokenUtil;

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

import static com.hovermind.oauthacf.R.string.iss;
import static com.hovermind.oauthacf.utils.Constants.CODE;
import static com.hovermind.oauthacf.utils.Constants.GRANT_TYPE;
import static com.hovermind.oauthacf.utils.Constants.ID_TOKEN;
import static com.hovermind.oauthacf.utils.Constants.REDIRECT_URI;
import static com.hovermind.oauthacf.utils.Constants.REFRESH_TOKEN;

/**
 * Created by hassan on 2017/06/23.
 */

public class TokenManager {
    private static String TAG = TokenManager.class.getName();
    private HttpLoggingInterceptor mLoggingInterceptor;
    private Context mContext = null;
    AuthCodeFetcher mAuthCodeFetcher = null;

    private OkHttpClient.Builder mHttpClient = null;
    private Retrofit mRetrofit = null;

    private String mBaseUri;
    private String mRedirectUri;
    private String mClientId;
    private String mClientSecret;

    private String mAuthCode = null;
    private String mNonce = null;
    private String mState = null;
    String mHeaderBasic = null;


    private static TokenManager instance = null;
    private static TokenManager defaultInstance = null;

    // instance
    public static TokenManager getInstance(Context context, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        if (instance == null) {
            instance = new TokenManager(context, clientIdResId, clientSecretResId, baseUriResId, redirectUriResId);
        }
        return instance;
    }

    // default instance
    public static TokenManager getDefaultInstance(Context context) {
        if (defaultInstance == null) {
            defaultInstance = new TokenManager(context, R.string.client_id, R.string.client_secret, R.string.base_uri, R.string.redirect_uri);
        }
        return defaultInstance;
    }

    // private constructor for singleton
    private TokenManager(Context mContext, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        this.mContext = mContext;

        // res
        mClientId = mContext.getResources().getString(clientIdResId);
        mClientSecret = mContext.getResources().getString(clientSecretResId);
        mBaseUri = mContext.getResources().getString(baseUriResId);
        mRedirectUri = mContext.getResources().getString(redirectUriResId);

        // header
        mHeaderBasic = Base64.encodeToString(String.format("%s:%s", mClientId, mClientSecret).getBytes(), Base64.NO_WRAP);

        // okHttp3 logging interceptor
        mLoggingInterceptor = new HttpLoggingInterceptor();
        mLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }


    // getting authentication code
    public void getAuthCode(@StringRes int authEndpointResId, @XmlRes int authUriMapResId, String mapName, String nonce, String state, AuthCodeListener listener) {
        mNonce = RandomStringUtils.randomAlphanumeric(25);
        if (nonce != null && !"".equals(nonce)) mNonce = nonce;

        mState = RandomStringUtils.randomAlphanumeric(25);
        if (state != null && !"".equals(state)) mState = state;

        mAuthCodeFetcher = new AuthCodeFetcher(mContext, authEndpointResId, authUriMapResId, mapName, mNonce, mState);
        mAuthCodeFetcher.getAuthCode(listener);
    }

    public void getAuthCode(@StringRes int authEndpointResId, @XmlRes int authUriMapResId, AuthCodeListener listener) {
        getAuthCode(authEndpointResId, authUriMapResId, null, null, null, listener);
    }

    public void getAuthCode(AuthCodeListener listener) {
        getAuthCode(R.string.auth_endpoint, R.xml.oauth_uri_map, null, null, null, listener);
    }

    public void getAuthCode(String mapName, String nonce, String state, AuthCodeListener listener) {
        getAuthCode(R.string.auth_endpoint, R.xml.oauth_uri_map, mapName, nonce, state, listener);
    }

    public void getAuthCode(String mapName, String nonce, AuthCodeListener listener) {
        getAuthCode(R.string.auth_endpoint, R.xml.oauth_uri_map, mapName, nonce, null, listener);
    }

    public void getAuthCode(String mapName, AuthCodeListener listener) {
        getAuthCode(R.string.auth_endpoint, R.xml.oauth_uri_map, mapName, null, null, listener);
    }

    // fetch token for authorization response uri
    public void getToken(@NonNull Uri authResponseUri, final TokenListener listener) {

        mAuthCode = authResponseUri.getQueryParameter("code");
        Log.d(TAG, "getToken:  AuthCode => " + mAuthCode);

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
                    //validateByIdToken(token, listener);

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
    public void getToken(@NonNull String authCode, final TokenListener listener) {
        Uri uri = Uri.parse("hm://hovermind.com?code=" + authCode);
        getToken(uri, listener);
    }

    // requires that idToken is in the uri
    public void getTokenWithValidation(@NonNull Uri authResponseUri, final TokenListener listener) {

        mAuthCode = authResponseUri.getQueryParameter(CODE);
        Log.d(TAG, "getTokenWithValidation:  AuthCode => " + mAuthCode);

        final String idToken = authResponseUri.getQueryParameter(ID_TOKEN);
        Log.d(TAG, "getTokenWithValidation: idToken => " + idToken);

        if (idToken == null || idToken == "") {
            Log.w(TAG, "getTokenWithValidation: Uri must contain id_token");
            listener.onTokenError("authResponseUri does not contain id_token");
            return;
        }

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

                    final Token token = response.body();
                    validateByIdToken(idToken, new TokenValidationListener() {
                        @Override
                        public void onValidationOk(boolean isTokenValid) {
                            Log.d(TAG, "onValidationOk: isTokenValid => " + isTokenValid);

                            if (isTokenValid && listener != null)
                                listener.onTokenReceived(token);
                        }

                        @Override
                        public void onValidationFailed(String errorMsg) {
                            Log.d(TAG, "onValidationFailed: Error => " + errorMsg);
                            if (listener != null)
                                listener.onTokenError("Token is not valid");
                        }
                    });
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

    public void getTokenWithValidation(@NonNull String authCode, @NonNull String idToken, final TokenListener listener) {
        Uri uri = Uri.parse("hm://hovermind.com?code=" + authCode + "&id_token=" + idToken);
        getTokenWithValidation(uri, listener);
    }

    // token validation
    public void validateByIdToken(@NonNull final String idToken, @StringRes int issResId, final TokenValidationListener listener) {
        Log.d(TAG, "Encoded Id Token => " + idToken);

        if (R.string.iss == issResId)
            Log.w(TAG, "validateByIdToken: did not create string resource for ISS");

        final String iss = mContext.getResources().getString(issResId);
        Log.d(TAG, "ISS from resource => " + iss);

        JwtStructure jwtStructure = TokenUtil.parseEncodedIdToken(idToken);
        String payloadString = jwtStructure.getPayload();

        if (payloadString != null && payloadString != "") {
            Log.d(TAG, "validateByIdToken: Payload String => " + payloadString);

            BaseJwtModel payload = new Gson().fromJson(payloadString, JwtModel.class);
            Log.d(TAG, "iss   : " + payload.getIss());
            Log.d(TAG, "exp   : " + payload.getExp());
            Log.d(TAG, "iat   : " + payload.getIat());
            Log.d(TAG, "nonce : " + payload.getNonce());
            Log.d(TAG, "aud : " + payload.getAud().get(0));

            long oneDay = 24 * 60 * 60;
            long fiveMinutes = 5 * 60;
            Long now = new Date().getTime() / 1000;

            if (iss.equals(payload.getIss())
                    && payload.getAud().get(0).equals(mClientId)
                    && (payload.getExp() - now) < (oneDay + fiveMinutes)
                    && (payload.getIat() - now) < fiveMinutes
                    && mNonce.equals(payload.getNonce())) {

                Log.d(TAG, "validateByIdToken => token is valid");
                if (listener != null) {
                    listener.onValidationOk(true);
                }

            } else {

                Log.e(TAG, "validateByIdToken => token is not valid");
                if (listener != null) {
                    listener.onValidationFailed("Token is not valid");
                }
            }

        } else {
            Log.d(TAG, "validateByIdToken:  PayloadString is null");
            if (listener != null) {
                listener.onValidationFailed("Payload of id token is null/empty");
            }
        }
    }

    public void validateByIdToken(@NonNull final String idToken, final TokenValidationListener listener) {
        validateByIdToken(idToken, iss, listener);
    }

    public void validateByIdToken(@NonNull final BaseToken token, @StringRes int issResId, final TokenValidationListener listener) {
        validateByIdToken(token.getIdToken(), issResId, listener);
    }

    public void validateByIdToken(@NonNull final BaseToken token, final TokenValidationListener listener) {
        validateByIdToken(token.getIdToken(), listener);
    }

    public void validateByJwt(@NonNull final String idToken, @StringRes int issResId, final TokenValidationListener listener) {

        if (R.string.iss == issResId)
            Log.w(TAG, "validateByJwt: did not create string resource for ISS");
        final String iss = mContext.getResources().getString(issResId);
        Log.d(TAG, "ISS from resource => " + iss);

        // client
        mHttpClient = new OkHttpClient.Builder();
        mHttpClient.addInterceptor(mLoggingInterceptor);

        // retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(mBaseUri)
                .client(mHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TokenService tokenService = mRetrofit.create(TokenService.class);
        Call<BaseJwtModel> validationCall = tokenService.checkIdToken(idToken);
        validationCall.enqueue(new Callback<BaseJwtModel>() {
            @Override
            public void onResponse(Call<BaseJwtModel> call, Response<BaseJwtModel> response) {
                if (response.isSuccessful()) {

                    BaseJwtModel jwtModel = response.body();

                    Log.d(TAG, "iss   : " + jwtModel.getIss());
                    Log.d(TAG, "exp   : " + jwtModel.getExp());
                    Log.d(TAG, "iat   : " + jwtModel.getIat());
                    Log.d(TAG, "mNonce : " + jwtModel.getNonce());
                    Log.d(TAG, "aud   : " + jwtModel.getAud().get(0));

                    long oneDay = 24 * 60 * 60;
                    long fiveMinutes = 5 * 60;
                    Long now = new Date().getTime() / 1000;

                    if (iss.equals(jwtModel.getIss())
                            && jwtModel.getAud().get(0).equals(mClientId)
                            && (jwtModel.getExp() - now) < (oneDay + fiveMinutes)
                            && (jwtModel.getIat() - now) < fiveMinutes
                            && mNonce.equals(jwtModel.getNonce())) {

                        Log.d(TAG, "validateByIdToken => token is valid");
                        if (listener != null) {
                            listener.onValidationOk(true);
                        }

                    } else {

                        Log.e(TAG, "validateByIdToken => token is not valid");
                        if (listener != null) {
                            listener.onValidationFailed("Token is not valid");
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse: unsuccessful response. error code => " + response.code());
                    listener.onValidationFailed("unsuccessful response.");
                }
            }

            @Override
            public void onFailure(Call<BaseJwtModel> call, Throwable t) {
                Log.d(TAG, "onFailure: api call failed => " + t.getMessage());
                if (listener != null) {
                    listener.onValidationFailed("api call failed, error => " + t.getMessage());
                }
            }
        });

    }

    public void validateByJwt(@NonNull final String idToken, final TokenValidationListener listener) {
        validateByJwt(idToken, iss, listener);
    }

    public void validateByJwt(@NonNull final BaseToken token, @StringRes int issResId, final TokenValidationListener listener) {
        validateByJwt(token.getIdToken(), issResId, listener);
    }

    public void validateByJwt(@NonNull final BaseToken token, final TokenValidationListener listener) {
        validateByJwt(token.getIdToken(), iss, listener);
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
        return refreshToken(refreshToken, iss);
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
        refreshToken(refreshToken, iss, listener);
    }

}
