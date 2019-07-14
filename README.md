## Contact me

禅师微信号: zencodex，可以聊(撩)

> remove all native code, just install cordova plugin js-module. if you want native code, please checkout branch has-native. I think use native code in main project is more convenient.

## Install plugin

### For Cordova Project

    cordova plugin add cordova-plugin-zenweex

### For Ionic Capacitor Project

    npm install cordova-plugin-zenweex
    npx cap sync

## How to use

```js
// navigator is a object which used by capacitor
// so don't use navigator as variable
// var navigator = weex.requireModule('navigator');   // TOO BAD
var router = weex.requireModule('navigator');         // It's OK

// .push wrapped in ZenWeex.js
// parameters => 
//   url: weex 加载的地址，如果app安装包里的资源，可以使用 file:///
//   orientation: landscape or portrait, default is portrait
//   animated: true or false, default is true
//   args: json object arguments passed to weex page, called by weex.config.args 

router.push({
    url: 'http://editor.weex.io/compiled/7bac45e9ed54b54bf9b42411183fe124/bundle.weex.js', 
    orientation:"landscape", 
    args:{p1: 1 , p2: 2}
});
```

## live debug (live reload) for capacitor project

`vim capacitor.config.json`，change server url to yours,
the server maybe run by `npm run serve` 

```json
{
  "appId": "com.zcomposer.appcapt",
  "appName": "AppCapt",
  "bundledWebRuntime": false,
  "server": {
    "url": "http://192.168.31.100:8080/"
  },
  "webDir": "www",
  "ios": {
    "cordovaSwiftVersion": "4.2"
  }
}
```


## Knowledges

- [cordova plugin developement tutorial](https://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/)
- [plugin.xml reference specification](https://cordova.apache.org/docs/en/latest/plugin_ref/spec.html)
- [weex guide](https://weex.apache.org/guide/introduction.html)
- [Ionic Capacitor](https://capacitor.ionicframework.com/docs/)
