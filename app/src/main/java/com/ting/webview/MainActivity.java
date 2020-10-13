package com.ting.webview;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    WebView mWebView;
    final static String TAG = "mtTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_main, null, false);
        setContentView(view);

        mWebView = view.findViewById(R.id.webView);

        settings();
        setClient();
        setChromeClient();

        mWebView.loadUrl("file:///android_asset/js_temple.html");//* 本地的html文件加载，文件放在src/main/assets下。访问时使用file:///android_asset/文件.xxx
        initBtToJS();
    }


    public void settings() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.addJavascriptInterface(new JsInterface(), "mApp");
    }


    public void setClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i(TAG, "request url is " + request.getUrl().toString());
                Uri uri = Uri.parse(request.getUrl().toString());
                if (uri == null) return false;
                if (uri.getScheme() != null && uri.getScheme().equals("js")) {
                    if (uri.getAuthority() != null && uri.getAuthority().equals("web")) {
                        //确定为约定的js页面
                        Log.i(TAG, "url 拦截成功");
                        Set<String> parameterNames = uri.getQueryParameterNames();
                        if (parameterNames != null) {
                            for (String str : parameterNames) {
                                Log.i(TAG, "get query value is " + uri.getQueryParameter(str));
                            }
                        }
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinish");
            }
        });

    }

    public boolean handleJSUrl(String url) {
        Uri uri = Uri.parse(url);
        if (uri == null) return false;
        if (uri.getScheme() != null && uri.getScheme().equals("js")) {
            if (uri.getAuthority() != null && uri.getAuthority().equals("web")) {
                //确定为约定的js页面
                Log.i(TAG, "url 拦截成功");
                Set<String> parameterNames = uri.getQueryParameterNames();
                if (parameterNames != null) {
                    for (String str : parameterNames) {
                        Log.i(TAG, "get query value is " + uri.getQueryParameter(str));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void setChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                Log.i("mtTest", "onJsAlert");
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();//关闭弹框-需要提供按钮并处理结果告知caller.否则，事件没结束，还不能唤起下一次弹框
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                Log.i(TAG, "android onJsPrompt");
                if (handleJSUrl(message)) {
//                    Log.i(TAG, "is the special url");
//                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
//                    b.setTitle("Prompt");
//                    final EditText editText = new EditText(MainActivity.this);
////                    editText.setId(R.id.edit_id);
//                    b.setView(editText);
//                    b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            result.confirm(editText.getText().toString());//关闭弹框-需要提供按钮并处理结果告知caller.否则，事件没结束，还不能唤起下一次弹框
//                        }
//                    });
//                    b.setCancelable(false);
//                    b.create().show();
                    result.confirm("something");
                    return true;
                }
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
    }

    public void initBtToJS() {
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
//                mWebView.loadUrl("javascript:callJS()");
            }
        });
    }

    public void call() {
        JSONObject object = new JSONObject();
        try {
            object.put("hello", "world");
            object.put("test", "keep");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mWebView.evaluateJavascript("javascript:callJSValue('" + 20200202 + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //调用Js代码后得到的返回值
                Toast toast = Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    public class JsInterface {

        @JavascriptInterface
        public void toastContent(String str) {
            Log.i(TAG, str);
            Toast toast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, -500);
            toast.show();
        }
    }
}
