package com.moor.webdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moor.webdemo.permission.OnRequestCallback;
import com.moor.webdemo.permission.PermissionConstants;
import com.moor.webdemo.permission.PermissionXUtil;
import com.moor.webdemo.utils.DownloadDataUtil;

import java.io.File;
import java.net.URLDecoder;

public class WebActivity extends AppCompatActivity {
    private final static String TAG = "WebDemo";
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private WebView webView;
    private String targetUrl;
    int keyBroadHeight = 0;
    boolean isVisiableForLast = false;
    private int currentHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        webView = findViewById(R.id.webview);

//        CookieManager.getInstance().setAcceptCookie(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
//        }

        targetUrl = getIntent().getStringExtra("targetUrl");
        setWebConfig();
        if (!TextUtils.isEmpty(targetUrl)) {
            loadUrl(targetUrl);
        }
        makeButtomInputAboveSoftBroad();
    }


    public void makeButtomInputAboveSoftBroad() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) webView.getLayoutParams();
        View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                float keyboardMinHeight = dpTopx(100f);
                int screenHeight = 0;
                int rectHeight = 0;
                if (hasNavigationBar(decorView)) {
                    screenHeight = getResources().getDisplayMetrics().heightPixels;
                } else {
                    screenHeight = decorView.getHeight();
                }
                if (hasNavigationBar(decorView)) {
                    rectHeight = rect.height();
                } else {
                    rectHeight = rect.bottom;
                }
                Rect rect2 = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect2);
                //计算出可见屏幕的高度
                int displayHight = rect2.bottom - rect2.top;
                //获得屏幕整体的高度
                int hight = decorView.getHeight();
                //获得键盘高度
                int keyboardHeight = hight - displayHight;
                if (keyboardHeight < 130) {
                    keyboardMinHeight = keyboardHeight;
                }
                boolean visible = (double) displayHight / hight < 0.8;
                if (visible != isVisiableForLast) {
                    // listener.onSoftKeyBoardVisible(visible,keyboardHeight );
                    keyBroadHeight = keyboardHeight;
                }
                isVisiableForLast = visible;
                int heightdiff = screenHeight - rectHeight;
                if (currentHeight != heightdiff && heightdiff > keyboardMinHeight) {
                    //键盘弹出
                    currentHeight = heightdiff;
                    // 如果是小130 都认为是没有弹出键盘
                    if (keyboardMinHeight <= 130) {
                        layoutParams.bottomMargin = 0;
                    } else {
                        layoutParams.bottomMargin = keyboardHeight;
                    }
                    webView.requestLayout();
                } else if (currentHeight != heightdiff && heightdiff < keyboardMinHeight) {
                    currentHeight = 0;
                    layoutParams.bottomMargin = currentHeight;
                    webView.requestLayout();
                }
            }
        });
    }

    public float dpTopx(Float value) {
        return (0.5f + value + getResources().getDisplayMetrics().density);
    }

    private Boolean hasNavigationBar(View view) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(view.findViewById(android.R.id.content));
        if (windowInsets != null) {
            return windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars()) &&
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom > 0;
        } else {
            return false;
        }
    }

    private void loadUrl(final String targetUrl) {
        webView.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                String[] types = fileChooserParams.getAcceptTypes();
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity(types);
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //直接同意即可     deny是拒绝
                    request.grant(request.getResources());
                }
            }
        });
        webView.loadUrl(targetUrl);


        //moorJsCallBack , 七陌js回调
        webView.addJavascriptInterface(this, "moorJsCallBack");


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
                webView.loadUrl(url);
                return true;
            }
        });
    }

    /**
     * 配置WebView
     */
    private void setWebConfig() {
        if (BuildConfig.DEBUG) {
            webView.setWebContentsDebuggingEnabled(true);
        }

        //webView.clearFocus();

        WebSettings webSettings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
//设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
//其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(false); //设置同源域关闭
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        webSettings.setDomStorageEnabled(true);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                String fileName = "";
                Log.e("tag", "url=" + url);
                Log.e("tag", "userAgent=" + userAgent);
                Log.e("tag", "contentDisposition=" + contentDisposition);
                String decoderString = "";
                try {
                    decoderString = URLDecoder.decode(url, "UTF-8");
                    String[] split = decoderString.split("/");
                    fileName = split[split.length - 1];
                    Log.e("tag", "mimetype=" + mimetype);
                    Log.e("tag", "contentLength=" + contentLength);
                    Toast.makeText(WebActivity.this, "正在下载", Toast.LENGTH_SHORT).show();
                    final DownloadDataUtil upDataUtil = new DownloadDataUtil(WebActivity.this);
                    final String newFile = getFilesDir() + File.separator + fileName;
                    if (Build.VERSION.SDK_INT >= 29) {
                        final String finalFileName = fileName;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                upDataUtil.downurl(url, finalFileName);
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //允许webview自动播放音频
        webSettings.setMediaPlaybackRequiresUserGesture(false);

    }


    /**
     * 打开本地图片
     */
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * 打开本地
     */
    private void openImageChooserActivity(String[] types) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        if(types != null && types.length > 0 && !TextUtils.isEmpty(types[0])) {
            i.putExtra(Intent.EXTRA_MIME_TYPES, types);
        }
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) {
                return;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 定义js回调接口
     * 会话关闭回调。注：功能暂未上线，可能不支持，视具体回调而定
     */
    @JavascriptInterface
    public void onCloseEvent() {
        Toast.makeText(this, "JS回调会话已关闭", Toast.LENGTH_SHORT).show();
    }


    /**
     * 定义js回调接口
     * H5页面上传 图片/文件 权限检查
     * 可用于自定义权限弹窗以及说明
     *
     * @param type js回调传参 image 上传图片，file 上传文件
     */
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


    /**
     * 定义js回调方法
     * 消息列表中视频下载按钮回调,用于客户处理视频文件url自定义后续操作
     *
     * @param videoUrl 回调回来的视频url
     */
    @JavascriptInterface
    public void onDownloadVideo(String videoUrl) {
        Toast.makeText(this, videoUrl, Toast.LENGTH_SHORT).show();
        Log.i("JS_onDownloadVideo", videoUrl);
    }


}

