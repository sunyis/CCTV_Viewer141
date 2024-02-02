package com.eanyatonic.cctvViewer;

import static com.eanyatonic.cctvViewer.FileUtils.copyAssets;
import static com.eanyatonic.cctvViewer.TVUrls.channelNames;
import static com.eanyatonic.cctvViewer.TVUrls.liveUrls;

import static java.lang.Thread.sleep;

import android.graphics.Bitmap;
import android.net.http.SslError;
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

public class MainActivity extends AppCompatActivity {

    private WebView webView; // 导入 X5 WebView



    private int currentLiveIndex;

    private static final String PREF_NAME = "MyPreferences";
    private static final String PREF_KEY_LIVE_INDEX = "currentLiveIndex";

    private boolean doubleBackToExitPressedOnce = false;

    private StringBuilder digitBuffer = new StringBuilder(); // 用于缓存按下的数字键
    private static final long DIGIT_TIMEOUT = 3000; // 超时时间（毫秒）

    private TextView inputTextView; // 用于显示正在输入的数字的 TextView

    // 初始化透明的View
    private View loadingOverlay;

    // 频道显示view
    private TextView overlayTextView;

    private String info = "";

    // 在 MainActivity 中添加一个 Handler
    private final Handler handler = new Handler();

    private boolean doubleMenuPressedOnce = false;
    private boolean doubleMenuPressedTwice = false;

    private boolean doubleEnterPressedOnce = false;
    private boolean doubleEnterPressedTwice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 WebView
        webView = findViewById(R.id.webView);

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 在这里执行长按操作
                showChannelList();
                return true;
            }
        });


        // 初始化显示正在输入的数字的 TextView
        inputTextView = findViewById(R.id.inputTextView);

        // 初始化 loadingOverlay
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 初始化 overlayTextView
        overlayTextView = findViewById(R.id.overlayTextView);

        // 加载上次保存的位置
        //loadLastLiveIndex();




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
            // 在此处处理选择的频道
            currentLiveIndex = which;
            loadLiveUrl();
            saveCurrentLiveIndex(); // 保存当前位置
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
                int numericKey = event.getKeyCode() - KeyEvent.KEYCODE_0;

                // 将按下的数字键追加到缓冲区
                digitBuffer.append(numericKey);

                // 使用 Handler 来在超时后处理输入的数字
                new Handler().postDelayed(() -> handleNumericInput(), DIGIT_TIMEOUT);

                // 更新显示正在输入的数字的 TextView
                updateInputTextView();

                return true;  // 事件已处理，不传递给 WebView
            }
        }

        return super.dispatchKeyEvent(event);  // 如果不处理，调用父类的方法继续传递事件
    }

    private void handleNumericInput() {
        // 将缓冲区中的数字转换为整数
        if (digitBuffer.length() > 0) {
            int numericValue = Integer.parseInt(digitBuffer.toString());

            // 检查数字是否在有效范围内
            if (numericValue > 0 && numericValue <= liveUrls.length) {
                currentLiveIndex = numericValue - 1;
                loadLiveUrl();
                saveCurrentLiveIndex(); // 保存当前位置
            }

            // 重置缓冲区
            digitBuffer.setLength(0);

            // 取消显示正在输入的数字
            inputTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateInputTextView() {
        // 在 TextView 中显示当前正在输入的数字
        inputTextView.setVisibility(View.VISIBLE);
        inputTextView.setText("换台：" + digitBuffer.toString());
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
            // 显示加载的View
            //loadingOverlay.setVisibility(View.VISIBLE);

            webView.setInitialScale(getMinimumScale());
            webView.loadUrl(liveUrls[currentLiveIndex]);
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

