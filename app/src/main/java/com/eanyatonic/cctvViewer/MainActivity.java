package com.eanyatonic.cctvViewer;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Looper;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private final static long HIDE_DELAY = 5000; // 5 秒钟没有操作后隐藏
    private Handler handler;
    private WebView webView; // 导入 X5 WebView



    //private int currentLiveIndex=1;

    private static final String PREF_NAME = "MyPreferences";

    private boolean doubleBackToExitPressedOnce = false;

    private boolean doubleMenuPressedOnce = false;
    private boolean doubleMenuPressedTwice = false;

    final int[] g = {0,0};
    String ua;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TVUrls.loadFromJson(TVUrls.defJson,getSharedPreferences("array_key", Context.MODE_PRIVATE));
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        setContentView(R.layout.activity_main);

        // 初始化 WebView
        webView = findViewById(R.id.webView);
        ua= webView.getSettings().getUserAgentString();
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
                                         var fullscreenBtn = document.querySelector('#player_pagefullscreen_yes_player')||document.querySelector('.videoFull')||document.querySelector('.vjs-fullscreen-control');
                                         var video=document.querySelector('video');
                                         if(video!=null){
                                            //alert(fullscreenBtn)
                                          //fullscreenBtn.click();
                                        document.body.appendChild(video)
                                          video.volume=1;
                                          //alert(window.location.href);

                                            //if(document.querySelector('.vjs-fullscreen-control')!=null){

                                        				video.style.position = 'fixed';
                                        				video.style.top = '0';
                                        				video.style.left = '0';
                                        				video.style.width = '100%';
                                        				video.style.height = '100%';
                                        				video.style.zIndex = '9999';
									video.style.objectFit= 'contain'; /* 保持宽高比，视频铺满容器 */
									video.style.backgroundColor= 'black'; /* 背景颜色可以根据需要更改 */
                                        				const aspectRatio = video.videoWidth / video.videoHeight;
                                        				const screenRatio = window.innerWidth / window.innerHeight;
                                        				//alert(video.videoWidth +"",""+ video.videoHeight+window.innerWidth +"",""+ window.innerHeight);
                                            //}
                    
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
        loadLiveUrl(0,0);

        //列表
        ListView lv=findViewById(R.id.list0);
        var arr=new String[TVUrls.liveUrls2.length];
        for (var i=0;i<arr.length;i++){
            arr[i]=TVUrls.liveUrls2[i].name;
        }
        var ldad=new ArrayAdapter<>(this,
                //android.R.layout.simple_list_item_1
                R.layout.custom_list_item
                ,arr);
        lv.setAdapter(ldad);

        ListView lv2=findViewById(R.id.list1);
        var arr2=new ArrayList<String>();
        var ldad2=new ArrayAdapter<>(this, R.layout.custom_list_item,arr2);
        lv2.setAdapter(ldad2);


        lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateList2(ldad2,i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Toast.makeText(getApplicationContext(),"b",Toast.LENGTH_SHORT).show();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                updateList2(ldad2,i);
            }
        });

        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),"click"+i+","+l,Toast.LENGTH_SHORT).show();
                g[1]=i;
                loadLiveUrl(g[0],g[1]);
                restartHideTimer();
            }
        });


        ListView lv3=findViewById(R.id.list2);
        var arr3=new String[]{"刷新","添加自定义","关于"};
        var ldad3=new ArrayAdapter<>(this,
                //android.R.layout.simple_list_item_1
                R.layout.custom_list_item
                ,arr3);
        lv3.setAdapter(ldad3);
        var cxt=this;
        lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),"click"+i+","+l,Toast.LENGTH_SHORT).show();
                switch (i){
                    case 0:
                        webView.reload();
                        break;
                    case 1:
                        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
                        builder.setTitle("输入框视频地址");

                        final EditText input = new EditText(cxt);
                        builder.setView(input);

                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userInput = input.getText().toString();
                                // 在这里处理用户输入
                                var cusg=TVUrls.liveUrls2[TVUrls.liveUrls2.length-1];
                                cusg.getTvUrls().add(new TVUrl(userInput,userInput));
                                cusg.toUrl();
                                // 保存数组到SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("array_key", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.apply();
                                Gson gson = new Gson();
                                String json = gson.toJson(cusg);
                                editor.putString("array_key", json);
                                editor.apply();
                            }
                        });

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                        break;
                    case 2:
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(cxt);
                        builder2.setTitle("关于");
                        builder2.setMessage(""+ua+"\n\nhttps://github.com/matrix3d/CCTV_Viewer");
                        builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击确定按钮后的操作
                                dialog.dismiss(); // 关闭对话框
                            }
                        });
                        builder2.show();
                        break;
                }
                restartHideTimer();
            }
        });

        //lv.setVisibility(View.GONE);
        //lv2.setVisibility(View.GONE);
        findViewById(R.id.menu).setVisibility(View.GONE);
        // 初始化 Handler
        handler = new Handler(getMainLooper());
    }

    private void updateList2(ArrayAdapter ldad2, int i){
        restartHideTimer();
        //Toast.makeText(getApplicationContext(),"a"+i+","+l,Toast.LENGTH_SHORT).show();
        // 更新数据源
        ldad2.clear();
        g[0] =i;
        var urlg =TVUrls.liveUrls2[i];
        for (var a: urlg.getTvUrls()
        ) {
            ldad2.add(a.name);
        }
        // 通知适配器数据已更改
        ldad2.notifyDataSetChanged();
    }

    // 重置隐藏计时器
    private void restartHideTimer() {
        // 移除之前的隐藏任务
        handler.removeCallbacksAndMessages(null);
        Log.d("aaaaa", "restartHideTimer: ");
        // 延迟一段时间后隐藏 ListView
        handler.postDelayed(() -> {
            findViewById(R.id.menu).setVisibility(View.GONE);
        }, HIDE_DELAY);
    }

    // 频道选择列表
    private void showChannelList() {
        findViewById(R.id.menu).setVisibility(View.VISIBLE);
        //findViewById(R.id.list0).requestFocus();
        restartHideTimer();
        // 构建频道列表对话框
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择频道");
        // 设置频道列表项
        builder.setItems(channelNames, (dialog, which) -> {
            if(which==0){
                webView.reload();
            }else {
                // 在此处处理选择的频道
                currentLiveIndex = which;
                loadLiveUrl(0,0);
                saveCurrentLiveIndex(); // 保存当前位置
            }
        });

        // 显示对话框
        builder.create().show();*/
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 重置隐藏计时器
        restartHideTimer();
        if(findViewById(R.id.menu).getVisibility()==View.VISIBLE){
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    // 执行上一个直播地址的操作
                    navigateLive(-1);
                    //navigateToPreviousLive();
                    return true;  // 返回 true 表示事件已处理，不传递给 WebView
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 执行下一个直播地址的操作
                    navigateLive(1);
                    //navigateToNextLive();
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


    /*private void loadLastLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        //currentLiveIndex = preferences.getInt(PREF_KEY_LIVE_INDEX, 0); // 默认值为0
        loadLiveUrl(0,0); // 加载上次保存的位置的直播地址
    }*/

    private void saveCurrentLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //editor.putInt(PREF_KEY_LIVE_INDEX, currentLiveIndex);
        editor.apply();
    }


    private void loadLiveUrl(int g,int i) {
        //restartHideTimer();
        //if (currentLiveIndex >= 0 && currentLiveIndex < liveUrls.length) {
            webView.setInitialScale(getMinimumScale());
            var url=TVUrls.liveUrls2[g].getTvUrls().get(i).url;//  liveUrls[currentLiveIndex];
            webView.loadUrl(url);
            if(url.startsWith("https://www.yangshipin.cn/tv/home")) {
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
        //}
    }

    private void navigateLive(int adder) {
        //currentLiveIndex = (currentLiveIndex - 1 + liveUrls.length) % liveUrls.length;
        var g0=g[0];
        var i=g[1]+adder;
        var cururl=TVUrls.liveUrls2[g[0]];
        if(adder>0){
            if(i>=cururl.getTvUrls().size()){
                g0++;
                i=0;
                if(g0>=TVUrls.liveUrls2.length){
                    return;
                }
            }
        }else {
            if(i<0){
                g0--;
                if(g0<0){
                    return;
                }
                i=TVUrls.liveUrls2[g0].getTvUrls().size()-1;
            }
        }
        g[0]=g0;
        g[1]=i;
        loadLiveUrl(g0,i);
        saveCurrentLiveIndex(); // 保存当前位置
    }

    //private void navigateToNextLive() {
        ///currentLiveIndex = (currentLiveIndex + 1) % liveUrls.length;
    //    loadLiveUrl(0,0);
    //    saveCurrentLiveIndex(); // 保存当前位置
    //}

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

