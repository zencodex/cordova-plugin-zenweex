// 插件注入脚本
// Author: 扣丁禅师 <v@yinqisen.cn>

var callable = {
  navigator: {
    push: function(options) {
      cordova.exec(function(o) {console.log(o)}, null, "ZenWeex", "openWindow", [options]);
    },
    pop: function() {
      cordova.exec(function(o) {console.log(o)}, null, "ZenWeex", "closeWindow");
    }
  }
};

window.weex = {
  requireModule: function(module) {
    const moduleProxy = new Proxy(
      {},
      {
        get: function(target, method) {
          if (module == 'navigator') {
            return callable[module][method];
          }

          return function(args, options = {}) {
            // console.log(module, method, args, options);
            return cordova.exec(function(o) {console.log(o)}, null, "ZenWeex", "callNativeModule", [module, method, args, options]);
          };
        }
      }
    );
    return moduleProxy;
  }
};

// inject weex first
cordova.exec(function() {
    console.log("weex inject success");
    // set WXEnvironment 
    cordova.exec(function(o) {window.WXEnvironment = o}, null, "ZenWeex", "getEnvironment");
}, null, "ZenWeex", "inject");