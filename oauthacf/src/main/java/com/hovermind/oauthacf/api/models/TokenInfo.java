package com.hovermind.oauthacf.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hassan on 2017/06/23.
 */

public class TokenInfo {
    @SerializedName("iss")
    private String iss;

    @SerializedName("sub")
    private String sub;

    @SerializedName("aud")
    private String aud;

    @SerializedName("exp")
    private Long exp;

    @SerializedName("iat")
    private Long iat;

    @SerializedName("nonce")
    private String nonce;

    @SerializedName("cHash")
    private String cHash;

    @SerializedName("acr")
    private String acr;

    @SerializedName("amr")
    private String amr;

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Long getIat() {
        return iat;
    }

    public void setIat(Long iat) {
        this.iat = iat;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getCHash() {
        return cHash;
    }

    public void setCHash(String cHash) {
        this.cHash = cHash;
    }

    public String getAcr() {
        return acr;
    }

    public void setAcr(String acr) {
        this.acr = acr;
    }

    public String getAmr() {
        return amr;
    }

    public void setAmr(String amr) {
        this.amr = amr;
    }


}
