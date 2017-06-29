
## 1. Using as library module
Clone the project and import as library module (search google : how to import a project as library module in Android Studio).

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

Then you have to pass that map name to instantiate TokenManager (TokenManager.java is the main class of this library)




You can use the [editor on GitHub](https://github.com/hovermind/oauth-code-flow/edit/master/README.md) to maintain and preview the content for your website in Markdown files.

Whenever you commit to this repository, GitHub Pages will run [Jekyll](https://jekyllrb.com/) to rebuild the pages in your site, from the content in your Markdown files.

### Markdown

Markdown is a lightweight and easy-to-use syntax for styling your writing. It includes conventions for

```markdown
Syntax highlighted code block

# Header 1
## Header 2
### Header 3

- Bulleted
- List

1. Numbered
2. List

**Bold** and _Italic_ and `Code` text

[Link](url) and ![Image](src)
```

For more details see [GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/).

### Jekyll Themes

Your Pages site will use the layout and styles from the Jekyll theme you have selected in your [repository settings](https://github.com/hovermind/oauth-code-flow/settings). The name of this theme is saved in the Jekyll `_config.yml` configuration file.

### Support or Contact

Having trouble with Pages? Check out our [documentation](https://help.github.com/categories/github-pages-basics/) or [contact support](https://github.com/contact) and we’ll help you sort it out.
