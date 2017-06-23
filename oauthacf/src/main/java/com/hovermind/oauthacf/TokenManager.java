package com.hovermind.oauthacf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Base64;

import com.hovermind.oauthacf.api.TokenService;
import com.hovermind.oauthacf.api.models.Token;

import java.io.IOException;
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
    private Map<String, String> mTokenUriMap = null;

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

    // singleton
    public static TokenManager getInstance(Context context, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        if (instance == null) {
            instance = new TokenManager(context, clientIdResId, clientSecretResId, baseUriResId, redirectUriResId);
        }
        return instance;
    }

    // private constructor
    private TokenManager(Context mContext, @StringRes int clientIdResId, @StringRes int clientSecretResId, @StringRes int baseUriResId, @StringRes int redirectUriResId) {
        this.mContext = mContext;
        mClientId = mContext.getResources().getString(clientIdResId);
        mClientSecret = mContext.getResources().getString(clientSecretResId);
        mBaseUri = mContext.getResources().getString(baseUriResId);
        mRedirectUri = mContext.getResources().getString(redirectUriResId);
        mLoggingInterceptor = new HttpLoggingInterceptor();
    }

    public void fetchToken(@NonNull String authCode, final TokenListener listener) {
        final String basic = Base64.encodeToString(String.format("%s:%s", mClientId, mClientSecret).getBytes(), Base64.NO_WRAP);

        Map<String, String> params = new HashMap<>();
        params.put(GRANT_TYPE, "authorization_code");
        params.put(CODE, authCode);
        params.put(REDIRECT_URI, mRedirectUri);

        // Client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.followRedirects(true);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                // add custom headers
                Request request = chain.request().newBuilder()
                        .addHeader("Authorization", "Basic " + basic)
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
                    if (listener != null) {
                        listener.onTokenReceived(response.body());
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

    public void refreshToken(@NonNull String refreshCode, TokenListener listener) {


    }


    public interface TokenListener {
        void onTokenReceived(Token token);

        void onTokenError(String errorMsg);
    }

}
