package com.hovermind.oauthacf.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hassan on 2017/06/23.
 */

public class IdTokenPayload {
    @SerializedName("sub")
    @Expose
    private String sub;
    @SerializedName("iss")
    @Expose
    private String iss;
    @SerializedName("nonce")
    @Expose
    private String nonce;
    @SerializedName("aud")
    @Expose
    private List<String> aud = null;
    @SerializedName("azp")
    @Expose
    private String azp;
    @SerializedName("auth_time")
    @Expose
    private Integer authTime;
    @SerializedName("realm")
    @Expose
    private String realm;
    @SerializedName("exp")
    @Expose
    private Integer exp;
    @SerializedName("iat")
    @Expose
    private Integer iat;

    @SerializedName("c_hash")
    private String cHash;

    @SerializedName("acr")
    private String acr;

    @SerializedName("amr")
    private String amr;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public List<String> getAud() {
        return aud;
    }

    public void setAud(List<String> aud) {
        this.aud = aud;
    }

    public String getAzp() {
        return azp;
    }

    public void setAzp(String azp) {
        this.azp = azp;
    }

    public Integer getAuthTime() {
        return authTime;
    }

    public void setAuthTime(Integer authTime) {
        this.authTime = authTime;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public Integer getIat() {
        return iat;
    }

    public void setIat(Integer iat) {
        this.iat = iat;
    }

    public String getcHash() {
        return cHash;
    }

    public void setcHash(String cHash) {
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
