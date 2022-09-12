package com.bobo.boboapplication;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bobo.boboapplication.databinding.ActivityFullscreenBinding;

import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private  static final String HOOK_URL = "http://192.168.3.17/hook.js";
//    private static final String URL = "http://m.310win.cn/Trade/";

    private static final String URL = "http://192.168.3.17:8080";

    private Handler mHandler;


    private ActivityFullscreenBinding binding;


    public String getHook(){
        return "var s = document.createElement('script');s.src='"+HOOK_URL+"?t="+ System.currentTimeMillis() +"';(document.querySelector('head') || document.body).appendChild(s);";
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
        binding.webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Log.e("TAG", "onPageFinished: " + url);

                view.evaluateJavascript(getHook(),new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.e("TAG", "onReceiveValue value=" + value);
                    }
                });
            }
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e("TAG", "onPageStarted: " + url);
                view.evaluateJavascript(getHook(),new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.e("TAG", "onReceiveValue value=" + value);
                    }
                });
            }

        });
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setJavaScriptEnabled(true);
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
         if (KeyEvent.KEYCODE_BACK == keyCode && binding.webView.canGoBack()) {
             String currUri = binding.webView.getUrl().split("://")[1].split("/")[1];
             Log.e("TAG",currUri);
             if(currUri.startsWith("follow")){
                 binding.webView.evaluateJavascript("history.go(-1)",new ValueCallback<String>() {
                     @Override
                     public void onReceiveValue(String value) {
                         Log.e("TAG", "onReceiveValue value=" + value);
                     }
                 });
             }else if(currUri.indexOf("match/football")!=-1){
                 binding.webView.evaluateJavascript("var n=document.getElementById(\"divPage_Main\");if(n){ n.getElementsByTagName(\"a\")[0].click()} else {window.goback()}",new ValueCallback<String>() {
                     @Override
                     public void onReceiveValue(String value) {
                         Log.e("TAG", "onReceiveValue value=" + value);
                     }
                 });
             }else if(currUri.equals("")||currUri.equals("Trade/")||currUri.equals("Trade/Index.aspx")){
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
                         Log.e("TAG", "history.go(-1)=" + value);
                     }
                 });
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