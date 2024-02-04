package com.eanyatonic.cctvViewer;

import static com.eanyatonic.cctvViewer.FileUtils.copyAssets;
import static com.eanyatonic.cctvViewer.TVUrls.channelNames;
import static com.eanyatonic.cctvViewer.TVUrls.liveUrls;

import static java.lang.Thread.sleep;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private WebView webView; // 导入 X5 WebView



    private int currentLiveIndex=1;

    private static final String PREF_NAME = "MyPreferences";
    private static final String PREF_KEY_LIVE_INDEX = "currentLiveIndex";

    private boolean doubleBackToExitPressedOnce = false;

    private boolean doubleMenuPressedOnce = false;
    private boolean doubleMenuPressedTwice = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 WebView
        webView = findViewById(R.id.webView);
        // 直接传入函数
        webView.addJavascriptInterface(new Object() {

            // 提供给JavaScript调用的方法
            @JavascriptInterface
            public void reload(String message) {
                    new Handler(webView.getContext().getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //webView.reload();
                        }
                    });
            }

        }, "Android");

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 在这里执行长按操作
                showChannelList();
                return true;
            }
        });
        // 配置 WebView 设置
       WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
        webSettings.setBlockNetworkImage(true);

        // 启用 JavaScript 自动点击功能
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置 WebViewClient 和 WebChromeClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler handler, SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(WebView view, String url) {
                //doFullScren(view,true);
                view.evaluateJavascript(
                        """
                                     
                                     function af(){ 
                                         var fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player')||document.querySelector('.videoFull');
                                         if(fullscreenBtn!=null){
                                            //alert(fullscreenBtn)
                                          fullscreenBtn.click();
                                          document.querySelector('video').volume=1;
                                          //alert(window.location.href);
                                          window.Android.reload("Hello from WebView!")
                                         }else{
                                             setTimeout(
                                                ()=>{ af();}
                                            ,16); 
                                         }
                                     }
                                af()
                                """
                        ,
                    value -> {
                    });
            }
        });



        // 禁用缩放
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        // 在 Android TV 上，需要禁用焦点自动导航
        webView.setFocusable(false);

        // 设置 WebView 客户端
        webView.setWebChromeClient(new WebChromeClient());

        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 加载初始网页
        loadLiveUrl();
    }

    // 频道选择列表
    private void showChannelList() {
        // 构建频道列表对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择频道");
        // 设置频道列表项
        builder.setItems(channelNames, (dialog, which) -> {
            if(which==0){
                webView.reload();
            }else {
                // 在此处处理选择的频道
                currentLiveIndex = which;
                loadLiveUrl();
                saveCurrentLiveIndex(); // 保存当前位置
            }
        });

        // 显示对话框
        builder.create().show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    // 执行上一个直播地址的操作
                    navigateToPreviousLive();
                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 执行下一个直播地址的操作
                    navigateToNextLive();
                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                } else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
                }else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {

                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                    if (doubleMenuPressedOnce) {
                        // 双击菜单键操作
                        // 刷新 WebView 页面
                        if (webView != null) {
                            webView.reload();
                        }
                        doubleMenuPressedTwice = true;
                        return true;  // 返回 true 表示事件已处理，不传递给 WebView
                    }

                    doubleMenuPressedOnce = true;

                    new Handler().postDelayed(() -> {
                        doubleMenuPressedOnce = false;
                        if(!doubleMenuPressedTwice) {
                            // 单击菜单键操作
                            // 显示频道列表
                            showChannelList();
                        }
                        doubleMenuPressedTwice = false;
                    }, 1000);

                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                }
                return true;  // 返回 true 表示事件已处理，不传递给 WebView
            }else if (event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9) {
            }
        }

        return super.dispatchKeyEvent(event);  // 如果不处理，调用父类的方法继续传递事件
    }


    private void loadLastLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLiveIndex = preferences.getInt(PREF_KEY_LIVE_INDEX, 0); // 默认值为0
        loadLiveUrl(); // 加载上次保存的位置的直播地址
    }

    private void saveCurrentLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_KEY_LIVE_INDEX, currentLiveIndex);
        editor.apply();
    }


    private void loadLiveUrl() {
        if (currentLiveIndex >= 0 && currentLiveIndex < liveUrls.length) {
            webView.setInitialScale(getMinimumScale());
            var url=liveUrls[currentLiveIndex];
            webView.loadUrl(url);
            if(url.startsWith("https://www.yangshipin.cn")) {
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                // 定义要执行的任务
                Runnable task = () -> {
                    // 在这里放置你要延迟执行的代码
                    //System.out.println("函数执行了！");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //webView.reload();
                        }
                    });
                };

                // 延迟执行任务，时间单位为秒（例如，延迟5秒执行可以设置为5）
                long delay = 1000;
                scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
                scheduler.shutdown();
            }
        }
    }

    private void navigateToPreviousLive() {
        currentLiveIndex = (currentLiveIndex - 1 + liveUrls.length) % liveUrls.length;
        loadLiveUrl();
        saveCurrentLiveIndex(); // 保存当前位置
    }

    private void navigateToNextLive() {
        currentLiveIndex = (currentLiveIndex + 1) % liveUrls.length;
        loadLiveUrl();
        saveCurrentLiveIndex(); // 保存当前位置
    }

    private int getMinimumScale() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 计算缩放比例，使用 double 类型进行计算
        double scale = Math.min((double) screenWidth / 1920.0, (double) screenHeight / 1080.0) * 100;

        Log.d("scale", "scale: " + scale);
        // 四舍五入并转为整数
        return (int) Math.round(scale);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        // 如果两秒内再次按返回键，则退出应用
    }


    @Override
    protected void onDestroy() {
        // 在销毁活动时，释放 WebView 资源
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

