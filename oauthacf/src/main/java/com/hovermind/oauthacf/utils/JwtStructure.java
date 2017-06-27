package com.hovermind.oauthacf.utils;

/**
 * Created by hassan on 2017/06/27.
 */

public class JwtStructure {
    private String header;
    private String payload;
    private String signature;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
