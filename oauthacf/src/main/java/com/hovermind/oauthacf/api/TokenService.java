package com.hovermind.oauthacf.api;

import com.hovermind.oauthacf.api.models.BaseJwtModel;
import com.hovermind.oauthacf.api.models.Token;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by hassan on 2017/06/23.
 */

public interface TokenService {
    @POST("access_token")
    Call<Token> fetchToken(@QueryMap Map<String, String> options);

    @POST("access_token")
    Call<Token> refreshToken(@QueryMap Map<String, String> options);

    @GET("token")
    Call<BaseJwtModel> checkIdToken(@Query("id_token") String idToken);

}
