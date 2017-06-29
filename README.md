
# Using as library module
Clone the project and import as library module in Android Studio

#### 1. Open project.gradle and add
```
ext{
    oauthScheme = "your_scheme"
    oauthHost = "your_host"
}
```

#### 2. Create string resources
```
<resources>
    <string name="client_id">test-client</string>
    <string name="client_secret">test-client</string>
    <string name="base_uri">http://hovermind.com/api/v2</string>
    <string name="redirect_uri">your_scheme://your_host</string>
    <string name="auth_endpoint">http://hovermind.com/oauth2/authorize</string>
    <string name="iss">http://hovermind.com/</string>
</resources>
``` 

#### 3. Create custom xml resource & name it ```oauth_uri_map.xml``` ( ```res/xml/oauth_uri_map.xml``` )
```
<?xml version="1.0" encoding="utf-8"?>
<uri_map linked="true">
    <entry key="client_id">@string/client_id</entry>
    <entry key="redirect_uri">@string/redirect_uri</entry>
    <entry key="scope">openid</entry>
    <entry key="response_type">code</entry>
    <entry key="prompt">consent</entry>
    <entry key="display">touch</entry>
    ... ... ...
</uri_map>
```
```oauth_uri_map.xml``` is default - you don't have to pass resId (i.e. ```R.xml.oauth_uri_map```) to instantiate TokenManager (`TokenManager.java` is the main class of this library). You can use any name you want & then you have to pass resId (```R.xml.your_custom_uri_map_res_name```) to instantiate TokenManager

All entries in ```oauth_uri_map.xml``` will be parsed to ```Map<key, value>``` to construct Authorization Uri. Default entry-map name is ```uri_map```, you can name it whatever you want: 
```
<your_entry_map_name linked="true">
    ... ... ...
    ... ... ...
</your_entry_map_name >
```
Then you have to pass that map name to instantiate TokenManager.

#### 4. TokenManager
getting instance: 
- ```TokenManager.getInstance(<parameters>)```
- ```TokenManager.getDefaultInstance(<context>)```

To use ```getDefaultInstance(Context context)``` : 
- name custom xml resource file as ```oauth_uri_map.xml``` & name entry-map as ```uri_map``` as mentioned in *step 3*
- provide all required string resources as mentioned in *step 2*

#### 5. Using TokenManager in OAuthLoginActivity
OAuthLoginActivity.java
```
private final String TAG = MainActivity.class.getSimpleName();
private TokenManager mTokenManger;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // using default instance
    mTokenManger = TokenManager.getDefaultInstance(MainActivity.this);
    fetchAuthCode();
}
```
Getting authorization code:
```
mTokenManger.getAuthCode(new AuthCodeListener() {
    @Override
    public void onAuthCodeReceived(Uri authResponseUri) {
        Log.d(TAG, "onAuthCodeReceived: authResponseUri => " + authResponseUri);
        // get token (AccessToken, RefreshToken ...) using authResponseUri
        // fetchToken(authResponseUri);
    }

    @Override
    public void onAuthCodeReceived(String authCode, String idToken) {
        Log.d(TAG, "onAuthCodeReceived: auth code => " + authCode);
    }

    @Override
    public void onAuthCodeError(String errorMsg) {
        Log.d(TAG, "onAuthCodeError: error => " + errorMsg);
    }
});
```
Getting Token (AccessToken, RefreshToken ...) using authResponseUri:
```
private void fetchToken(Uri authResponseUri) {
    // getting token using authResponseUri
    mTokenManger.getToken(authResponseUri, new TokenListener() {
        @Override
        public void onTokenReceived(final Token token) {
            Log.d(TAG, "onTokenReceived: access token => " + token.getAccessToken());
            // validate token either by idToken or by JWT
            // validateToken(token)
        }

        @Override
        public void onTokenError(String errorMsg) {
            Log.d(TAG, "onTokenError: error => " + errorMsg);
        }
    });
}
```
Validating token by idToken (or by JWT => Token validation end point of Authorization server):
```
private void validateToken(Token token){
    // token validation using IdToken
    mTokenManger.validateByIdToken(token, new TokenValidationListener() {
        @Override
        public void onValidationOk(boolean isTokenValid) {
            Log.d(TAG, "onValidationOk: isTokenValid => " + isTokenValid);
        }

        @Override
        public void onValidationFailed(String errorMsg) {
            Log.d(TAG, "onValidationFailed: error => " + errorMsg);
        }
    });
}
```

### 6. Method overloads of TokenManager

Getting Auth code:
```
public void getAuthCode(@StringRes int authEndpointResId, @XmlRes int authUriMapResId, String mapName, String nonce, String state, AuthCodeListener listener)
public void getAuthCode(@StringRes int authEndpointResId, @XmlRes int authUriMapResId, AuthCodeListener listener)
public void getAuthCode(AuthCodeListener listener)
public void getAuthCode(String mapName, String nonce, String state, AuthCodeListener listener)
public void getAuthCode(String mapName, String nonce, AuthCodeListener listener) 
public void getAuthCode(String mapName, AuthCodeListener listener)
```

Getting Token:
```
public void getToken(@NonNull Uri authResponseUri, final TokenListener listener)
public void getToken(@NonNull String authCode, final TokenListener listener) 
public void getTokenWithValidation(@NonNull Uri authResponseUri, final TokenListener listener)
public void getTokenWithValidation(@NonNull String authCode, @NonNull String idToken, final TokenListener listener)
```

Token Validation:
```
public void validateByIdToken(@NonNull final String idToken, @StringRes int issResId, final TokenValidationListener listener)
public void validateByIdToken(@NonNull final String idToken, final TokenValidationListener listener)
public void validateByIdToken(@NonNull final BaseToken token, @StringRes int issResId, final TokenValidationListener listener)
public void validateByIdToken(@NonNull final BaseToken token, final TokenValidationListener listener)
public void validateByJwt(@NonNull final String idToken, @StringRes int issResId, final TokenValidationListener listener)
public void validateByJwt(@NonNull final String idToken, final TokenValidationListener listener)
public void validateByJwt(@NonNull final BaseToken token, @StringRes int issResId, final TokenValidationListener listener)
public void validateByJwt(@NonNull final BaseToken token, final TokenValidationListener listener)
```

Refreshing Token:
```
public Token refreshToken(String refreshToken, @StringRes final int issResId) 
public Token refreshToken(String refreshToken)
public void refreshToken(String refreshToken, @StringRes final int issResId, final TokenRefreshListener listener) 
public void refreshToken(String refreshToken, final TokenRefreshListener listener)
```





