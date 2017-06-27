package com.hovermind.oauthacf.Interfaces;

import com.hovermind.oauthacf.api.models.Token;

/**
 * Created by hassan on 2017/06/27.
 */

public interface TokenListener {
    void onTokenReceived(Token token);

    void onTokenError(String errorMsg);
}