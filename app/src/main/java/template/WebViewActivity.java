package template;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

import test.mxm.android_app_template.BuildConfig;

public class WebViewActivity extends Activity {
    private final String TAG = "WebActivity";

    public static final String URL_KEY = "url";
    private final String JSInterfaceName = "android";

    private WebView webView;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);//请加载自己的WebView
        url = getIntent().getStringExtra(URL_KEY);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ProgressHUD.dismiss(WebViewActivity.this);
            }

            @Override//解决部分机型打开Url会提示使用外部浏览器
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "shouldOverrideUrlLoading: " + url);
                }
                HashMap<String, String> header = new HashMap<>();
                header.put("", "");
                view.loadUrl(url);
                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                webView.loadUrl("about:blank");
                showErrorAlert();

                int errorCode = error.getErrorCode();
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
//                    view.loadUrl("about:blank"); // 避免出现默认的错误界面
                }
            }

            // 这个方法在6.0才出现
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                int statusCode = errorResponse.getStatusCode();
                if (404 == statusCode || 500 == statusCode) {
                    view.loadUrl("about:blank");
                    showErrorAlert();
                }
            }
        });
        //android 6.0 以下的错误处理
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // android 6.0 以下通过title获取
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (title.contains("404") || title.contains("500") || title.contains("Error")) {
                        view.loadUrl("about:blank");// 避免出现默认的错误界面
                        showErrorAlert();
                    }
                }
            }

            //<input type="file"/>标签适配
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                openFileChooserActivity(filePathCallback, fileChooserParams);
                return true;
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存
        webSettings.setSupportMultipleWindows(true);
        webSettings.setUserAgentString("My App");
        //
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 通过addJavascriptInterface()将Java对象映射到JS对象
        webView.addJavascriptInterface(this, JSInterfaceName);//当前类对象映射到JS的JSInterfaceName对象

        synCookies();
        //设置请求头
        HashMap<String, String> header = new HashMap<>();
        header.put("", "");
        webView.loadUrl(url, header);
        webView.evaluateJavascript("javascript:var sessionid = 123", null);

        //文件下载适配
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                downloadFile(url, contentLength);
            }
        });

        //显示进度
        ProgressHUD.showOnContent(this);
    }

    @Override
    protected void onDestroy() {
        //资源一定要释放
        webView.setWebViewClient(null);
        webView.removeJavascriptInterface(JSInterfaceName);
        webView.clearCache(true);
        webView.clearHistory();
        webView.destroy();
        super.onDestroy();
    }

    /**
     * 此方法会映射到JS的JSInterfaceName对象
     */
    @JavascriptInterface
    public void nativeBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
//        webView.evaluateJavascript("javascript:vueBack()", null);//Java调用js
    }

    private void showErrorAlert() {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "showErrorAlert: ");
        }
        //弹出报错框
    }

    //Cookie注入
    public void synCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);// 允许接受 Cookie

        cookieManager.removeSessionCookies(null);// 移除
        cookieManager.setCookie(".baidu.com", "sessionid=123");
        cookieManager.setCookie(".baidu.com", "orderid=111");

        cookieManager.flush();
    }

    private void downloadFile(String url, long contentLength) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        startActivity(intent);

        /*// 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
        request.setAllowedOverMetered(false);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(false);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 设置下载文件保存的路径和文件名
        String fileName  = URLUtil.guessFileName(url, contentDisposition, mimeType);
        log.debug("fileName:{}", fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
        log.debug("downloadId:{}", downloadId);*/
    }


    //打开文件选择器
    private static final int FILE_CHOOSER_RESULT_CODE = 6432;
    private ValueCallback<Uri[]> mValueCallback;
    private void openFileChooserActivity(ValueCallback<Uri[]> filePathCallback,
                                         WebChromeClient.FileChooserParams fileChooserParams) {
        mValueCallback = filePathCallback;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i,
                ""), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != FILE_CHOOSER_RESULT_CODE
                || null == mValueCallback) return;

        if (resultCode != Activity.RESULT_OK || data == null) {
            //取消了这里也要调用一次，否则下次无法起调onShowFileChooser
            mValueCallback.onReceiveValue(null);
            mValueCallback = null;
            return;
        }

        Uri[] results = null;

        ClipData clipData = data.getClipData();//获取夹带的数据
        if (clipData != null && clipData.getItemCount() > 0) {//多选
            results = new Uri[clipData.getItemCount()];
            for (int i = 0, len = clipData.getItemCount(); i < len; i++) {
                results[i] = clipData.getItemAt(i).getUri();
            }
        }

        Uri fileUri = data.getData();//单选
        if (fileUri != null) results = new Uri[]{fileUri};

        mValueCallback.onReceiveValue(results);
        mValueCallback = null;
    }
}
