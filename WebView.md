# WebView

> 使得用户不离开当前客户端也能访问网页，对访问操作有辅助、监控作用，提高了安全性
> 加载链接，别忘了检查上网权限

## 使用
### 代码动态创建
可作为组件在xml定义，为防止内存泄露，建议代码动态创建
```java
WebView view = new WebView(Context) // context使用AppContext.以防链接加载中，关闭页面。释放不了资源
setContentView(view)
```
### 使用开关注意漏洞
开启了JS与客户端的代码交互，就可能存在js引入的代码攻击。
在不需要的情况下，可以将相关开关关闭。
如：
```
// 禁用 file 协议；
攻击可利用file://使用应用的私有路径通过创建intent唤醒你应用的WebViewActivity.无法加载时会自动下载在SDcard
sdcard上被所有应用都能访问
setAllowFileAccess(false); 
setAllowFileAccessFromFileURLs(false);
setAllowUniversalAccessFromFileURLs(false);
//js攻击，会利用jsInterface 获取app Object利用反射等获取各种系统class
setJavaScriptEnabled(false)
//4.2后使用@javaInterface注释 限制js访问范围 4.2前设置要求js调用本地js，传递js的相关信息进行识别
```
### 销毁
webView不再使用，销毁规范：
```
@Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }
```
## 辅助类
### WebViewClient
> 要实现链接能在当前客户端（的webView）打开，必须配置此类
> - _作用：导航，监控Url_

常用方法:
#### _boolean shouldOverrideUrlLoading *_
_#访问有效网址，且不为post请求，系统引发调用此方法_
#_Give the host application__ a chance to__ __take c__ontrol when a URL is about to be loaded__ __in the current WebView. _
```java
   public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
       String url =  request.getUrl().toString();
       Log.i("webView","current loading url is " + url);//可直接得知当前请求
       //方法拦截
       if(url.startWith("tel:")){
           Intent intent = new Intent(Intent.ACTION_DIAL)
               ..
               return true;
       }
       // 拦截转向
       if(url.contain("test")){
         mWebView.loadUrl("file://");
           return true;
       }
       return false;
   }
```
true : _causes the current WebView to abort loading the URL，to cancel the current load。停止加载当前url_
_false : to continue loading the URL as usual.继续当前webview加载_
_此方法内，常使用WebView.loadUrl转向加载其他页面。_
_若想要继续当前url加载，则直接return false即可，无需loadUrl。_
### _WebSetting_
> _访问的相关_**_设置_**
> WebView created ,it obtains a set of defult settings.getObject throught webview.getSettings()

_常用方法：_
#### _setUserAgentString(String)_
用户代理字段，无设置时，webView会使用默认字段，表明当前客户端使用的web相关信息，如引擎版本等
此字段常被当前客户端添加自定义字段，让web端识别并确认为具体某一端。
#### _setJavaScriptEnable(boolean)_
是否允许android和页面JavaScript之间代码调用
### WebChromeClient
> _辅助 WebView 处理 Javascript 的对话框,网站图标,网站标题,全屏等窗口操作
_

_常用方法:_
#### onProgressChanged(Webview view,int newProgress)
加载过程中，回调告知app当前进度，int 0 ->100。通过此方法内部实现加载进度的展示
```java
     public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                RelativeLayout.LayoutParams lpProgress = 
                    (RelativeLayout.LayoutParams) progressTv.getLayoutParams();
                lpProgress.width = (屏幕宽度)sWidth * (newProgress / 100);
                progressTv.setLayoutParams(lpProgress);
                if (newProgress > 0 && newProgress < 100) {
                    progressTv.setVisibility(VISIBLE);
                } else {
                    progressTv.setVisibility(GONE);
                }
            }
```
#### onReceivedTitle(WebView view,String title)
页面加载或改变，回调告知app当前改变后正在加载的页面标题，可在内部获取并setText展示在菜单栏。
#### onJsAlert、onJsConfirm、onJsPrompt
与javaScript交互相关，javaScript调用相关特定方法，引发系统调用对应方法。由于altert 等操作在WebView是不允许的，因此具体弹框实现还是由客户端回调创建。
## 加载
### loadUrl
```
  webView.loadUrl(String);
  // 加载网页 ： "http://www.google.com/"
  // 加载apk包中的html页面 : "file:///android_asset/test.html"
  // 加载手机本地的html页面 : "content://com.android.htmlfileprovider/sdcard/test.html"
```
### loadData
```
  WebView.loadData(String data, String mimeType, String encoding)
// 参数说明：
// 参数1：html data
// 内容里不能出现 ’#’, ‘%’, ‘\’ , ‘?’ 这四个字符
   若出现了需用 %23, %25, %27, %3f 对应来替代，否则会出现异常
   因此参数一传递前需要遍历检查替换
// 参数2：内容的数据类型
// 参数3：编码方法
```
### loadDataWithUrl
```
loadDataWithBaseURL(String baseUrl, String data,
            String mimeType, String encoding, String failUrl) 
//baseUrl : 作为data内部资源的相对路径
// failUrl name historyUrl
```


## 客户端与JS间交互
> 前提，webSettings.setJavaScriptEnabled(true)

### android端调用Js
#### 方式一 loadUrl("javascript:method()")


example : 
_step 1:_
在项目的src/main目录下创建assets文件夹 —— 右键new- Directory
_step 2:_
创建本地JS代码测试使用。assets右键new-File - 输入文件名.html
```html
<!DOCTYPE html>
<html>

<head>
    <meta charset="utf-8">
    <title>mt</title>
    //在JS中定义方法
    <script>
   function callJS(){
     window.open("https://baidu.com")//访问链接
   }

</script>
</head>
<body>
//方便判断加载为当前html
<H1>hello</H1>
</body>
</html>
```
_step 3 :_
```java
//准备阶段
mWebView.getSettings().setJavaScriptEnabled(true);
mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//This applies to the JavaScript function {@code window.open()} 是否支持jS打开新窗口

//加载链接，需要setWebViewClient使得使用当前mWebView加载，否则会跳出action选择页面
   mWebView.setWebViewClient(new WebViewClient() {
            //简单实现例子
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i(TAG, "request url is " + request.getUrl().toString());
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinish");
            }
        });
//配置完成后，正式使用WebView加载
  mWebView.loadUrl("file:///android_asset/js_temple.html");
// view布局放置button.监听点击，实现调用js代码

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl("javascript:callJS()");
            }
        });
```
效果图：
![Screenshot_20201012-151647.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602487235258-2f8b7dc6-3fae-491c-9558-23ed3b07ffad.jpeg#align=left&display=inline&height=337&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-151647.jpg&originHeight=700&originWidth=527&size=19002&status=done&style=none&width=254)                      ![Screenshot_20201012-151657.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602487234562-649e11c8-f706-43e7-b0d3-c3e082c5e398.jpeg#align=left&display=inline&height=337&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-151657.jpg&originHeight=673&originWidth=533&size=100524&status=done&style=none&width=267)
通过WebViewClient设置tag log:
2020-10-12 15:16:28.307 7886-7886/com.ting.webview I/mtTest: onPageFinish //加载本地页面完成
2020-10-12 15:16:51.287 7886-7886/com.ting.webview I/mtTest: request url is [https://baidu.com/](https://baidu.com/)
2020-10-12 15:16:51.526 7886-7886/com.ting.webview I/mtTest: request url is [https://www.baidu.com/](https://www.baidu.com/)
2020-10-12 15:16:52.975 7886-7886/com.ting.webview I/mtTest: onPageFinish


例子概述：
完成对WebView初始、配置 —— loadUrl —— 连接两端调用场景
注意：
① 在assets目录下文件路径使用：file:///android_asset/文件.xxx
②  调用JS代码，loadUrl("javascript:callJS()")，必须在load JS页面加载完成后。即onPageFinished后才可调用，否则抛出异常。注意调用格式为**javascript:方法名()**
#### 方式二 evaluateJavascript("javascript:method()",ValutCallBack callback)
> api>18，才可使用此方法



example :
在上述定义的html文件中添加定义带返回值的方法
```html
  <script>   
    ... 
function callJSValue(str){
    return str
   }
 </script>
```
准备阶段与方式一无差异
```java
    mWebView.evaluateJavascript("javascript:callJSValue('" + "hello world" + "')"
                                , new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //调用Js代码后得到的返回值
                Toast toast = Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

//"javascript:callJSValue('" + 20200202 + "'"
//"javascript:callJSValue('" + JsonObject+ "'"
```
![Screenshot_20201012-155338.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602489295922-3d2183b2-3e36-4322-8001-61e0b121a47b.jpeg#align=left&display=inline&height=315&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-155338.jpg&originHeight=682&originWidth=531&size=15565&status=done&style=none&width=245)![Screenshot_20201012-155730.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602489665502-5859cec1-14cb-47c2-93a2-ed760b77fdbd.jpeg#align=left&display=inline&height=316&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-155730.jpg&originHeight=682&originWidth=528&size=15656&status=done&style=none&width=245)![Screenshot_20201012-155701.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602489699941-98c3ae3a-7094-4d82-b43c-ad8eb5e1f1aa.jpeg#align=left&display=inline&height=322&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-155701.jpg&originHeight=669&originWidth=528&size=17926&status=done&style=none&width=255)
例子概述：
* js语法为弱类型，因此调用方法的传入参数不限制，但由回调方法限制，所有类型都被转为String，因此当传递较复杂数据时，推荐使用json
*注意调用JS方法传参时，参数由一对"'"单引号标注


- 由于方式二在4.4后加入，通常情况下，android调用JS添加判断api逻辑，两种方式都存在
```java
// 因为该方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
if (Build.VERSION.SDK_INT < 18) {
    mWebView.loadUrl("javascript:methods()");
} else {
    mWebView.evaluateJavascript（"javascript:methods()", new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            //此处为 js 返回的结果
        }
    });
}

```
### JS调用android
#### 直接调用
```java
//准备阶段 step 1:
public class appObject {
    
   @JavascriptInterface //必须要加此注释
    public void hello(){
        Toast toast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -500);
        toast.show();
    }
    
}
//step 2:传递
mWebView.addJavaScriptInterface(开放方法封装实例 : new appObject()，ObjectName : "mApp")
//此种方法存在漏洞
```
```html
<script>
   function toastApp(){
    mApp.hello("js调用android代码")//通过映射得到客户端开放实例使用
   }
  </script>
  ...
<body>
  ...
  <button id="bt" onclick="toastApp()">js调用android代码</button>
  
</body>
```
点击js，触发js调用android方法：
![Screenshot_20201012-164548__01__01.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/2315637/1602492432341-189a3930-05fb-4a5d-90df-f78bd4aa57ad.jpeg#align=left&display=inline&height=322&margin=%5Bobject%20Object%5D&name=Screenshot_20201012-164548__01__01.jpg&originHeight=644&originWidth=540&size=30990&status=done&style=none&width=270)
#### 利用客户端拦截url调用
对特定JS页面，识别其Url确认拦截，特殊处理。
```html
<script>   
function changeUrl(){
   document.location = "js://web?arg=mt&arg1=2020" //修改此属性改变当前js的url
   }
 </script>
//例子中，此方法由js按钮引发调用
//当前页面改变url引发调用shouldOverrideUrlLoading
```
```java
//webViewClient中 
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
```
Log:
2020-10-12 17:23:32.077 15219-15219/com.ting.webview I/mtTest: onPageFinish
2020-10-12 17:23:34.149 15219-15219/com.ting.webview I/mtTest: request url is js://web?arg=mt&arg1=2020
2020-10-12 17:23:34.149 15219-15219/com.ting.webview I/mtTest: url 拦截成功
2020-10-12 17:23:34.150 15219-15219/com.ting.webview I/mtTest: get query value is mt
2020-10-12 17:23:34.150 15219-15219/com.ting.webview I/mtTest: get query value is 2020
例子概述：

- 此处JS相当于间接调用android代码，且无法直接获取返回值，若要取得返回值，需要android完成js调用处理后，将处理结果作为参数通过调用js方法传递 (看上节)
#### 利用系统特定方法回调
example : 
> 当js调用 alert()—警告框、comfirm() — 确认框、promt() — 输入框 会对应触发webView onJsAlert()、onJsConfirm()、onJsPrompt()；利用这些回调方法实现我们自定义的需求。
> 前提：settings: setJavaScriptEnabled(true)
>            webView.setWebChromeClient

```java
<script>   
function toPrompt(){
     var result = prompt("js://web?arg=mt&arg1=2020")//为了传递识别当前js
     console.log(result)
   }
</script>
```
通过复写WebChromeClient中对应方法，重造对话框，实现客户端适配。否则super即显示js原有弹框效果
```java

    public void setChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            
           @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                Log.i(TAG, "android onJsPrompt");
                if (handleJSUrl(message)) {
                    Log.i(TAG, "is the special url");
                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                    b.setTitle("Prompt");
                    final EditText editText = new EditText(MainActivity.this);
                    b.setView(editText);
                    b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm(editText.getText().toString());
      //关闭弹框-需要提供按钮并处理结果告知caller.否则，事件没结束，还不能唤起下一次弹框
                        }
                    });
                    b.setCancelable(false);
                    b.create().show();
                    return true;
                }
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
            
            @Override 
            public boolean onJsAlert(WebView view, String url, String message
                                     , final JsResult result) {
                Log.i("mtTest", "js弹框，引发android实现");
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });
    }
```
说明：
按需求使用，实际可不创建对话框。最后直接通过JsPromptResult.confirm将数据值传递到js，return true表明当前client 拦截处理。完成js-android-js流程。
js常用的这三种回调中，仅有onJsPrompt的JsPromptResult.confirm方法传递为String（其余都为JsResult 传递boolean），因此引发onPrompt能传递兼容更多类型的数据
