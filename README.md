
# <a name="as-module"></a>Using as library module
#### 1. Clone the project and import as library module in Android Studio

#### 2. <a name="step-2"></a>Open project.gradle and add
```
ext{
    oauthScheme = "your_scheme"
    oauthHost = "your_host"
}
```
#### 3. <a name="step-3"></a>Create string resources
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
#### 4. <a name="step-4"></a>Create custom xml resource & name it ```oauth_uri_map.xml``` ( ```res/xml/oauth_uri_map.xml``` )
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
```oauth_uri_map.xml``` is default - you don't have to pass resId (i.e. ```R.xml.oauth_uri_map```) to instantiate TokenManager (see [TokenManager](#step-5)). You can use any name you want & then you have to pass resId (```R.xml.your_custom_uri_map_res_name```) to instantiate TokenManager (see [Method overloads of TokenManager](#step-7))

All entries in ```oauth_uri_map.xml``` will be parsed to ```Map<key, value>``` to construct Authorization Uri. Default entry-map name is ```uri_map```, you can name it whatever you want: 
```
<your_entry_map_name linked="true">
    ... ... ...
    ... ... ...
</your_entry_map_name >
```
Then you have to pass that map name to instantiate TokenManager (see [Method overloads of TokenManager](#step-7))

#### 5. <a name="step-5"></a>TokenManager
The main class, it's singleton. Getting instance:
- ```TokenManager.getInstance(<parameters>)```
- ```TokenManager.getDefaultInstance(<context>)```

To use ```getDefaultInstance(Context context)``` : 
- name xml resource file as ```oauth_uri_map.xml``` & entry-map as ```uri_map``` as mentioned in [step 4](#step-4)
- provide all required string resources as mentioned in [step 3](#step-3)

#### 6. <a name="step-6"></a>Using TokenManager in OAuthLoginActivity
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
#### 7. <a name="step-7"></a>Method overloads of TokenManager
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

## Using as Gradle Dependency
#### 1. <a name="step-2_1">Use Jitpack.io
- goto <a href="https://jitpack.io/">jitpack.io</a> and follow instructions
- get the gradle dependency
- open app.gradle and use that gradle dependency

#### 2. <a name="step-2_2">Open Manifest.xml & Add intent filter for RedirectUriReceiverActivity (this Activity belongs to library):
```
<activity
    android:name="com.hovermind.oauthacf.RedirectUriReceiverActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:scheme="you_scheme" 
            android:host="your_host"/>
    </intent-filter>
</activity>
```
#### 3. <a name="step-2_3">Create LoginActivity
Create Login Activity (i.e. OAuthLoginActivity) where you will use the libray to get access token from authorization server.

Follow [step 3](#step-3), [step 4](#step-4), [step 5](#step-5), [step 6](#step-6) & [step 7](#step-7) from [Using as library module](#as-module)




## License

   Copyright 2017 <a href="http://hovermind.com">Hovermind</a>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
