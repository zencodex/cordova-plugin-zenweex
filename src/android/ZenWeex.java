package cordova.plugin;

import android.net.Uri;
import android.app.Activity;
import android.content.Intent;
import android.app.Application;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.InitConfig;
import com.alibaba.weex.WXPageActivity;
import com.alibaba.weex.commons.adapter.ImageAdapter;
import com.alibaba.weex.commons.adapter.JSExceptionAdapter;
// import com.alibaba.android.bindingx.plugin.weex.BindingX;

import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class ZenWeex extends CordovaPlugin {

    private static final String WEEX_CATEGORY = "com.taobao.android.intent.category.WEEX";
    private static final String WEEX_ACTION = "com.taobao.android.intent.action.WEEX";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("inject")) {
            this.inject(callbackContext);
        } else if (action.equals("getEnvironment")) {
            this.getEnvironment(callbackContext);
        } else if (action.equals("openWindow")) {
            this.openWindow(args, callbackContext);
        } else if (action.equals("closeWindow")) {
            this.closeWindow(callbackContext);
        } else if (action.equals("callNativeModule")) {
            this.callNativeModule(args, callbackContext);
        }

        return true;
    }

    private void inject(CallbackContext callbackContext) {
        WXSDKEngine.initialize((Application)webView.getContext().getApplicationContext(),
                new InitConfig.Builder()
                        .setImgAdapter(new ImageAdapter())
//                        .setImgAdapter(new FrescoImageAdapter())// use fresco adapter
                       .setJSExceptionAdapter(new JSExceptionAdapter())
                        .build()
        );

        callbackContext.success();
    }

    private void getEnvironment(CallbackContext callbackContext) {
        Map<String, String> config = WXEnvironment.getConfig();
        JSONObject object = new JSONObject(config);
        callbackContext.success(object);
    }

    private void openWindow(JSONArray args, CallbackContext callbackContext) {
        // 没有参数直接返回
        if (args.length() < 1) return;

        try {
            JSONObject argObj = args.getJSONObject(0);
            Uri uri = Uri.parse(argObj.getString("url"));

            Application appContext = (Application)webView.getContext().getApplicationContext();
            Intent intent = new Intent(appContext, WXPageActivity.class);
            intent.setAction(WEEX_ACTION);
            intent.setData(uri);
            intent.putExtra("json", args.toString());
            intent.addCategory(WEEX_CATEGORY);
            appContext.startActivity(intent);
        } catch (JSONException e) {
            callbackContext.error(e.toString());
        }
    }

    private void closeWindow(CallbackContext callbackContext) {
        // ios 有实现，用处不大，可忽略
        // 如果需要关闭当前页面，在 weex 页面中调用 navigator.pop
    }

    private void callNativeModule(JSONArray args, CallbackContext callbackContext) {
        // 保留函数
        // iOS 中已实现，使用注入很复杂，且效果不好
        // 建议用 openWindow 代替打开weex窗口
    }
}
