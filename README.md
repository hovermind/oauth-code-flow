
## 1. Using as library module
Clone the project and import as library module.

## Open project.gradle and add
```
ext{
    oauthScheme = "your_scheme"
    oauthHost = "your_host"
}
```

### Create string resources
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

### Create custom xml resource & name it 'oauth_uri_map.xml' (res/xml/oauth_uri_map.xml)
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
All data in 'oauth_uri_map.xml' will be parsed to Map<key, value> to construct Authorization Uri. Default map name is 'uri_map', you can name the entry map name whatever you want: 
```
<your_map linked="true">
    ... ... ...
    ... ... ...
</your_map>
```
Then you have to pass that map name to instantiate TokenManager (TokenManager.java is the main class of this library)
