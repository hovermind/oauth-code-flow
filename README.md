
# Using as library module
Clone the project and import as library module.

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
1. ```TokenManager.getInstance()```
2. ```TokenManager.getDefaultInstance()```

If you use ```getDefaultInstance(Context context)```
- name custom xml resource file ```oauth_uri_map.xml``` & name entry-map ```uri_map``` as mentioned in step 3
- provide all required string resources as mentioned in step 2








