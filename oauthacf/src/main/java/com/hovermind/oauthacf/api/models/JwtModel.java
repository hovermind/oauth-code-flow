package com.hovermind.oauthacf.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hassan on 2017/06/23.
 */

public class JwtModel extends BaseJwtModel implements Serializable{
    @SerializedName("sub")
    @Expose
    private String sub;
    @SerializedName("azp")
    @Expose
    private String azp;
    @SerializedName("auth_time")
    @Expose
    private Integer authTime;
    @SerializedName("realm")
    @Expose
    private String realm;
    @SerializedName("acr")
    @Expose
    private String acr;
    @SerializedName("amr")
    @Expose
    private String amr;
}
