package com.alibaba.weex;

import android.os.Bundle;
import android.view.View;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import capacitor.android.plugins.R;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.databinding.DataBindingUtil;
//import capacitor.android.plugins.databinding.ActBaseBinding;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.utils.WXFileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import android.net.Uri;
import android.content.Intent;

public class WXPageActivity extends AppCompatActivity implements IWXRenderListener {

    protected WXSDKInstance mWXSDKInstance;
    private boolean firstCreate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpage);

//        actBinding = DataBindingUtil.setContentView(WXPageActivity.this, R.layout.activity_wxpage);
//        actBinding.loading.setVisibility(View.VISIBLE);

        mWXSDKInstance = new WXSDKInstance(this);
        mWXSDKInstance.registerRenderListener(this);

        Intent intent = this.getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            Map<String, Object> options = new HashMap<>();
            try {
                String jsonString = intent.getStringExtra("json");
                JSONArray jsonArray = new JSONArray(jsonString);
                JSONObject argObj = jsonArray.getJSONObject(0);
                JSONObject inArgObj = argObj.optJSONObject("args");
                boolean animated = argObj.optBoolean("animated", true);
                String orientation = argObj.optString("orientation", "portrait");
                options = this.toMap(inArgObj);
            } catch (JSONException e) {
                Log.e(this.getClass().getSimpleName(), e.toString());
            }

            options.put(WXSDKInstance.BUNDLE_URL, uri.toString());
            renderByUrl(uri.toString(), options);
        }
    }

    public static Map<String, Object> toMap(JSONObject jsonobj)  throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }   return list;
    }

    protected void render(String path, Map options) {

      /**
       * pageName: 自定义字符串标识
       * url: 渲染的页面Url，本地用 file:// 开头
       * options: 初始化时传入WEEX的参数，比如 bundleUrl
       *
       * WXRenderStrategy:
       * WXRenderStrategy.APPEND_ASYNC 异步策略先返回外层View，其他View渲染完成后调用 onRenderSuccess。
       * WXRenderStrategy.APPEND_ONCE  所有控件渲染完后后一次性返回。
       */

        mWXSDKInstance.render(this.getClass().getSimpleName(), WXFileUtils.loadAsset(path, this), options, null, WXRenderStrategy.APPEND_ASYNC);
    }

    protected void renderByUrl(final String url, Map options) {
        mWXSDKInstance.renderByUrl(this.getClass().getSimpleName(), url, options, null, WXRenderStrategy.APPEND_ASYNC);
    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
//        if (actBinding.container.getChildCount() > 0) {
//            actBinding.container.removeAllViews();
//        }
//        actBinding.container.addView(view);
        if (view == null) {
            return;
        }
        setContentView(view);
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
        Log.i(this.getClass().getSimpleName(), "onRenderSuccess.");
//        actBinding.loading.setVisibility(View.GONE);
    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {
        Log.e(this.getClass().getSimpleName(), "onRenderSuccess.");
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        Log.e(this.getClass().getSimpleName(), "render exception: errrCode = " + errCode + ", msg = " + msg);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firstCreate) {
            firstCreate = false;
        } else {
            mWXSDKInstance.fireGlobalEventCallback("return", null);
            Log.i(this.getClass().getSimpleName(), "fire a global event, return.");
        }
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityDestroy();
        }
    }
}
