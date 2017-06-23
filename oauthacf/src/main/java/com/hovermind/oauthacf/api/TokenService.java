package com.hovermind.oauthacf.api;

import com.hovermind.oauthacf.api.models.Token;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by hassan on 2017/06/23.
 */

public interface TokenService {
    @POST("access_token")
    Call<Token> fetchToken(@QueryMap Map<String, String> options);

    @POST("access_token")
    Call<Token> refreshToken(@QueryMap Map<String, String> options);

    @POST("tokeninfo")
    Call<Token> fetchTokenInfo(@QueryMap Map<String, String> options);

}
