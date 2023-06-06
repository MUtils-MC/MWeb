# MWeb
Don't want to read everything? Click on the spoiler below for a compact feature overview.<br>
For developers that are interested into the MWeb API, scroll down to the bottom.
<details><summary>MWeb Feature Summary</summary>
MWeb can be controlled completely by GUI or command.<br>
You can...

- ...browse through your files ingame (GUI only)
- ...create public links to any file or folder on your server
- ...create restricted links to any file or folder on your server (user, passphrase, timed, amount)
- ...edit and toggle all created links
- ...send folders or archives directly to players as resource pack
- ...zip & unzip folders/archives ingame
- ...delete & rename files ingame
- ...define folders for uploading files (can be restricted like downloads)

And for the more technical features, you can also...

- ...respond custom html/css/js on error pages
- ...define a proxy domain (like mutils.net/<id> instead of 1.2.3.4:1234/<id>)
- ...restrict every single action to a permission

</details>

---

The detailed description & guide is sorted into the following categories

- Creating download link
- Manage files
- Resource Pack integration
- Technical information & setup

To view all permissions and further help, visit mutils.net/mweb


## Create download link
Creating a simple download link isn't more then one click!

1. Open the whitelist GUI (`/ws whitelist` or `/ws` -> ``manage download access``)
2. Navigate to your file or folder
3. Press a displayed button to create a link

You can create a global link or personal link (only usable by you) directly from the navigator by using your 1 & 2 hotkey. After processing (bigger folders take some time to zip) a new link is generated for you. Click on copy to copy the link in your clipboard and share it with anyone or paste it in your browser!

You can create multiple links for the same file. Press your 4 hotkey on a file or folder to manage all links. Here you can toggle, remove or even edit existing links.<br>
[Click here to see preview (80mb video to large for GitHub)](https://i.imgur.com/kcUB1VT.gif)<br>
(the preview use `mweb.mutils.net` as proxy. This is not a default behaviour)


## Manage files
MWeb also adds helpful and simple file editing tools to make publishing easier! Open the mange GUI with `/ws`

- Rename files or folders (Hotkey 1)
- Zip folders (Hotkey 2)
- Unzip archives (Hotkey 4)
- Delete files or folders (Hotkey 3)

Additionally you can directly see file information like file size, file type (highlighted with item color), last edited and the path.
![Preview is loading...](https://i.imgur.com/8u9bRRm.gifv)


## Resource pack integration
Send resource pack folders or archives directly to other players with one click. That allows you to modify your resource pack and update players instantly without the need of packing & uploading.

1. Copy the path of your resource pack folder/archive
2. Enter `/ws texturepack "<path>" <target>`
3. Targets must accept resource packs at the first time

You can also force players to use the resource pack by adding `true` at the end of the command. <br>
E.g. `/ws texturepack "rp/test" @a true`
![Preview is loading...](https://i.imgur.com/4mYczui.gif)


## Technical information & setup
MWeb is completely customizable and adapt to your needs. To make everything possible, MWeb starts a web server in the background to manage all requests. That also means you need a second open port. If you don't know what that is or have troubles to set it up ask us at [our discord server](https://dc.mutils.net).


**Important** - Every action is restricted to a certain permission ([docs](https://mutils.net/mweb)) to secure your files. OP bypasses all permissions! We highly recommend you to use permission manager like [luckperms](https://luckperms.net/)!


You don't want the number ip in your link? Use a proxy manager like nginx and enter it in the mweb config. Need help? Like before you can [ask us](https://dc.mutils.net).


## API
You want to use features from MWeb in your plugin/mod? With our API you can create and manage* links
and even use our resource pack feature to dynamically create and send resource packs! Get the best experience by using Kotlin (using our api in java works too but is less enjoyable)

```kotlin
dependencies {
    compileOnly("de.miraculixx:mweb:1.1.0")
}
```
The API is located at maven central. Do **not** shade it into your project!
