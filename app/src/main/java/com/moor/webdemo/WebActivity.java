package com.moor.webdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Rect;
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
                //??????????????????????????????
                int displayHight = rect2.bottom - rect2.top;
                //???????????????????????????
                int hight = decorView.getHeight();
                //??????????????????
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
                    //????????????
                    currentHeight = heightdiff;
                    // ????????????130 ??????????????????????????????
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
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Intent intent = fileChooserParams.createIntent();
                String[] a = fileChooserParams.getAcceptTypes();

                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity(intent.getType());
                return true;
            }
        });
        webView.loadUrl(targetUrl);


        //moorJsCallBack , ??????js??????
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
     * ??????WebView
     */
    private void setWebConfig() {
        webView.setWebContentsDebuggingEnabled(true);
        //webView.clearFocus();


        WebSettings webSettings = webView.getSettings();
        //??????????????????????????????Javascript????????????webview??????????????????Javascript
        webSettings.setJavaScriptEnabled(true);
        // ???????????? html ??????JS ???????????????????????????????????????????????????CPU????????????
        // ??? onStop ??? onResume ???????????? setJavaScriptEnabled() ???????????? false ??? true ??????
//????????????????????????????????????
        webSettings.setUseWideViewPort(true); //????????????????????????webview?????????
        webSettings.setLoadWithOverviewMode(true); // ????????????????????????
        //????????????
        webSettings.setSupportZoom(true); //????????????????????????true??????????????????????????????
        webSettings.setBuiltInZoomControls(true); //????????????????????????????????????false?????????WebView????????????
        webSettings.setDisplayZoomControls(false); //???????????????????????????
//??????????????????
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //??????webview?????????
        webSettings.setAllowFileAccess(false); //?????????????????????
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //????????????JS???????????????
        webSettings.setLoadsImagesAutomatically(true); //????????????????????????
        webSettings.setDefaultTextEncodingName("utf-8");//??????????????????

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
                    Toast.makeText(WebActivity.this, "????????????", Toast.LENGTH_SHORT).show();
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

    }


    /**
     * ??????????????????
     */
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * ????????????
     */
    private void openImageChooserActivity(String type) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if ("image/*".equals(type)) {
            i.setType("image/*");
        } else {
            i.setType("*/*");
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
            webView.goBack();// ?????????????????????
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * ??????js????????????
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    @JavascriptInterface
    public void onCloseEvent() {
        Toast.makeText(this, "JS?????????????????????", Toast.LENGTH_SHORT).show();
    }


    /**
     * ??????js????????????
     * H5???????????? ??????/?????? ????????????
     * ??????????????????????????????????????????
     *
     * @param type js???????????? image ???????????????file ????????????
     */
    @JavascriptInterface
    public void checkPermission(String type) {

        Toast.makeText(this, "JS??????????????????" + type, Toast.LENGTH_SHORT).show();


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PermissionXUtil.checkPermission(WebActivity.this, new OnRequestCallback() {
                    @Override
                    public void requestSuccess() {

                        //???????????????????????????Js?????? ??????Js??????????????? type?????????js checkPermission??????????????????
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
     * ??????js????????????
     * ???????????????????????????????????????,??????????????????????????????url?????????????????????
     *
     * @param videoUrl ?????????????????????url
     */
    @JavascriptInterface
    public void onDownloadVideo(String videoUrl) {
        Toast.makeText(this, videoUrl, Toast.LENGTH_SHORT).show();
        Log.i("JS_onDownloadVideo", videoUrl);
    }
}
