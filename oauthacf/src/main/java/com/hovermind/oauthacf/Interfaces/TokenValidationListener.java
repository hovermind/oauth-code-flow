package com.hovermind.oauthacf.Interfaces;

/**
 * Created by hassan on 2017/06/27.
 */

public interface TokenValidationListener {

    void onValidationOk(boolean isTokenValid);

    void onValidationFailed(String errorMsg);
}
