package com.bobo.boboapplication;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.bobo.boboapplication.databinding.ActivityFullscreenBinding;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private  static final String HOOK_URL = "http://192.168.1.6:8080/hook.js";
//    private static final String URL = "http://m.310win.cn/Trade/";

    private static final String URL = "https://m.cp567.fun/";
//    private static final String URL = "http://192.168.1.6:8080/";

    private Handler mHandler;
    private ValueCallback<Uri> uploadMessage;//单选文件回调
    private ValueCallback<Uri[]> uploadMessageAboveL;

    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    private ActivityFullscreenBinding binding;


    public String getHook(){
        return "var s = document.createElement('script');s.src='"+HOOK_URL+"?t="+ System.currentTimeMillis() +"';(document.querySelector('head') || document.body).appendChild(s);";
    }


    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
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
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
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
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());

        Log.e("TAG", getCacheDir().toString());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.webView.loadUrl(URL);

        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });

        binding.webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                String url = request.toString();
//                if (url.indexOf("http") != -1) {
//                    Toast.makeText(getApplicationContext(), "成功拦截http开头的网址", Toast.LENGTH_SHORT).show();
//                }
//                return true;
//            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e("TAG", "onPageStarted: " + url);
                view.evaluateJavascript("document.addEventListener('touchstart',function(ev){\n" +
                        "            ev.preventDefault() \n" +
                        "         })",new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.e("TAG", "onReceiveValue value=" + value);
                    }
                });
            }


            //            public WebResourceResponse shouldInterceptRequest(WebView view,
//                                                              WebResourceRequest request) {
//                try {
//                    java.net.URL url = new URL(request.getUrl().toString());
//                    HttpClient client;
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
////                new WebResourceResponse(request.getRequestHeaders().get("content-type"),request.getRequestHeaders().get());
//                return null;
//            }
//
        });
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        binding.webView.getSettings().setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        binding.webView.addJavascriptInterface(this, "native");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.

    }


    private boolean hitTwice = false;


    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (KeyEvent.KEYCODE_BACK == keyCode ) {
             if(binding.webView.canGoBack()){
                 binding.webView.evaluateJavascript("history.go(-1)",new ValueCallback<String>() {
                     @Override
                     public void onReceiveValue(String value) {
                         Log.e("TAG", "execute history.go(-1)" );
                     }
                 });
             }else{
                 String url = binding.webView.getUrl();
                 Log.e("TAG",url);
                 String currUri = url.replace(URL,"");
                 Log.e("TAG",currUri);
                 if(currUri.equals("")||currUri.equals("Trade/")||currUri.equals("Trade/Index.aspx")){
                     if(!hitTwice){
                         hitTwice = true;
                         Toast.makeText(getApplicationContext(), "再按一次返回退出程序",
                                 Toast.LENGTH_SHORT).show();
                         mHandler.postDelayed(()->{
                             Log.e("TAG","过了2秒，切换hitTwice值");
                             this.hitTwice = false;
                         }, 2000);
                     }else{
                         System.exit(0);
                     }
                 }else{
                     binding.webView.evaluateJavascript("history.go(-1)",new ValueCallback<String>() {
                         @Override
                         public void onReceiveValue(String value) {
                             Log.e("TAG", "execute history.go(-1)" );
                         }
                     });
                 }
             }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onStop() {
        super.onStop();
    }


}