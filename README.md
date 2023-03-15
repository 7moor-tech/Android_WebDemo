# Android_WebDemo
## Android对接访客端H5 WebDemo
本项目为接入访客端h5页面的webview示例代码。主要作为演示功能，主要为参考作用。

### JS回调方法：
如果配置的访客端开启了js参数，详细注释参考demo页面
#### 1：会话关闭js回调

```
@JavascriptInterface
public void onCloseEvent() {
    Toast.makeText(this, "JS回调会话已关闭", Toast.LENGTH_SHORT).show();
}
```

#### 2：权限检查js交互
```
@JavascriptInterface
public void checkPermission(String type) {

    Toast.makeText(this, "JS回调权限检查" + type, Toast.LENGTH_SHORT).show();


    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            PermissionXUtil.checkPermission(WebActivity.this, new OnRequestCallback() {
                @Override
                public void requestSuccess() {

                    //权限存在以后，调用Js方法 响应Js上传事件， type传参为js checkPermission方法回传的值
                    webView.evaluateJavascript("javascript:initAllUpload('" + type + "')", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }
            }, PermissionConstants.STORE);
        }
    });
}
```
#### 3：视频文件url
```
@JavascriptInterface
public void onDownloadVideo(String videoUrl) {
    Toast.makeText(this, videoUrl, Toast.LENGTH_SHORT).show();
    Log.i("JS_onDownloadVideo", videoUrl);
}

