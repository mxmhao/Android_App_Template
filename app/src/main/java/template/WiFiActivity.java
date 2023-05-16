package template;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 从android 9起，WiFi的所有功能只要有精确定位权限既可；9起既要定位权限，也要开启定位服务。详细看下面的链接
 * android 10之前 蓝牙需要 ACCESS_COARSE_LOCATION 而后需要 ACCESS_FINE_LOCATION；9起蓝牙既要定位权限，也要开启定位服务。
 * WiFi扫描的限制：
 * https://developer.android.google.cn/guide/topics/connectivity/wifi-scan#wifi-scan-restrictions
 * 蓝牙扫描的限制：
 * https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le
 *
 * 建议：
 *  如果是扫描外设，使用CompanionDeviceManager类会节约很多的步骤，比如：权限请求等
 *  https://developer.android.com/guide/topics/connectivity/companion-device-pairing
 */
public class WiFiActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int state = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (state == PackageManager.PERMISSION_GRANTED) {
            new WiFiUtils().startScan(this, results -> {

            });
        } else if (state == PackageManager.PERMISSION_DENIED) {
            //调到权限设置界面
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new WiFiUtils().startScan(this, results -> {

            });
        }
    }
}

/**
 * https://juejin.cn/post/6844903875263250445#heading-16
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 */
class WiFiUtils extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                if (mNeedScan && intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0) == WifiManager.WIFI_STATE_ENABLED) {
                    mWiFiManager.startScan();
                    mNeedScan = false;
                }
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION://wifi列表变化
                mContext.unregisterReceiver(this);

                // EXTRA_RESULTS_UPDATED 的值必须为true，否则getScanResults返回的是最近一次扫描成功的结果，导致一些已经关闭的热点也会存在缓存中
                if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                    mScanResults.didWiFiScanResult(null);
                    return;
                }
                List<ScanResult> scanResults = mWiFiManager.getScanResults();

                ArrayList<String> list = new ArrayList<>(5);
                for (ScanResult result : scanResults) {
                    if (result.SSID.startsWith("你们的特殊前缀")) {
                        list.add(result.SSID);
                    }
                }
                mScanResults.didWiFiScanResult(list);
                break;
        }
    }

    private Handler handler;
    private WifiManager mWiFiManager;
    private boolean mNeedScan;
    private ScanResults mScanResults;
    public void startScan(final Context context, final ScanResults scanResults) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        if (scanResults == null) {
            throw new NullPointerException("scanResults == null");
        }
        mContext = context;
        mScanResults = scanResults;
        if (null == handler) {
            handler = new Handler(Looper.getMainLooper());
        }
        if (null == mWiFiManager) {
            mWiFiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        mNeedScan = false;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态

        if (mWiFiManager.isWifiEnabled()) {
            context.registerReceiver(this, filter);
            handler.post(() -> {
                if (mWiFiManager.isWifiEnabled()) {
                    mWiFiManager.startScan();
                }
            });
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            new AlertDialog.Builder(context)
                    .setTitle("WiFi还未开启")
                    .setMessage("请先开启WiFi")
                    .setPositiveButton("去开启", (dialog, which) -> {
                        context.registerReceiver(WiFiUtils.this, filter);
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    })
                    .setNegativeButton("取消", null)
                    .show();

//            BottomSheetDialog bsd = new BottomSheetDialog(context);//用这个弹框会不会好点？
        } else {
            //监听扫描的结果
            context.registerReceiver(this, filter);
            mNeedScan = true;
            mWiFiManager.setWifiEnabled(true);
        }
    }

    public void stop() {
        if (null != mContext) {
            mContext.unregisterReceiver(this);
        }
    }

    private void closeWiFi() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (null != mWiFiManager && mWiFiManager.isWifiEnabled()) {//貌似不这么写会崩溃
                mWiFiManager.setWifiEnabled(false);
            }
        }
    }

    public interface ScanResults {
        void didWiFiScanResult(List<String> results);
    }

    public interface WifiNameGet {
        void didGetWifiName(String results);
    }

    /**
     * 获取当前连接的WiFi名称
     * 9.0起需要ACCESS_FINE_LOCATION权限，调用前请检查权限
     * 10 起开始需要开启定位服务，提示用户去打开
     */
    public static void getWiFiName(Context context, @NonNull final WifiNameGet wifiNameGet) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiNameGet.didGetWifiName(null);
            return;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();//就算WiFi没有开启，这个也有返回值，只是 id < 0
            Log.e("TAG", "wifiInfo：" + wifiInfo.toString()); //打印全部wifi信息
            Log.e("TAG", "SSID：" + wifiInfo.getSSID());      //打印SSID
            Log.e("TAG", "ID：" + wifiInfo.getNetworkId());      //打印SSID
            if (wifiInfo.getNetworkId() < 0) {
                wifiNameGet.didGetWifiName(null);
                return;
            }
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);//去掉首尾的双引号
            }
            Log.e("TAG", "SSID：" + ssid);      //打印SSID
            wifiNameGet.didGetWifiName(ssid);
            return;
        }

        final NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        final ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO) {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                //这个要到API>=31才会有返回值，并且要使用 FLAG_INCLUDE_LOCATION_INFO
                WifiInfo wifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
//                Log.e("TAG", "onCapabilitiesChanged0: " + networkCapabilities.getTransportInfo());
                if (null != wifiInfo && wifiInfo.getNetworkId() > -1) {
                    String ssid = wifiInfo.getSSID();
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);//去掉首尾的双引号
                    }
                    wifiNameGet.didGetWifiName(ssid);
                    Log.e("TAG", "onCapabilitiesChanged1: " + ssid);
                } else {
                    wifiNameGet.didGetWifiName(null);
                    Log.e("TAG", "onCapabilitiesChanged2: " + networkCapabilities.getTransportInfo());
                }
            }

            @Override
            public void onUnavailable() {
                wifiNameGet.didGetWifiName(null);
                Log.e("TAG", "onUnavailable: ");
            }
        };
        cm.requestNetwork(request, networkCallback, 100); // For request，这个只请求一下
//        cm.registerNetworkCallback(request, networkCallback); // For listen 这个是实时监听网络
    }

    //判断定位服务是否开启
    public static boolean isGpsEnabled(Context ctx) {
        //从系统服务中获取定位管理器
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    //----------------------------------------------------------------------------
    /*
    https://developer.android.com/guide/topics/connectivity/wifi-bootstrap
    //连接WiFi示例：
    WiFiUtils.connectWiFiBySpecifier(this, "123", null, new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            //回调是在主线程，耗时任务去其他线程
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            //先绑定网络，如果不绑，有些手机系统使用TCP、UDP会直接报错，比如小米MIUI
            cm.bindProcessToNetwork(network);

            //做自己的业务。。。。。。

            //不需要此网络了就要解绑
            cm.bindProcessToNetwork(null);
            //然后注销当前请求，注销后系统会自动连接到上次的网络，不注销不会自动连接
            cm.unregisterNetworkCallback(this);
        }

        @Override
        public void onUnavailable() {
        }
    });

    建议：
        如果是扫描外设，使用CompanionDeviceManager类会节约很多的步骤，比如：权限请求等
        https://developer.android.com/guide/topics/connectivity/companion-device-pairing
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void connectWiFiBySpecifier(Context context, String ssid, String password, ConnectivityManager.NetworkCallback networkCallback) {
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                .setSsidPattern(new PatternMatcher(ssid, PatternMatcher.PATTERN_LITERAL));//方式一
        //.setSsid(ssid);
        if (password != null) builder = builder.setWpa2Passphrase(password);

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(builder.build())
                .build();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // WiFi连接回调
        cm.requestNetwork(request, networkCallback, 15000);
    }

    //https://developer.android.com/guide/topics/connectivity/wifi-suggest
    //Android10以上，通过suggestion连接WIFI，有些情况系统更本不了你的Suggestion，比如你的WiFi不能连接外网
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void connectWiFiBySuggestion(Context context, String ssid, String password, ConnectivityManager.NetworkCallback networkCallback) {
        WifiNetworkSuggestion.Builder builder = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsAppInteractionRequired(true); // Optional (Needs location permission)
        if (null != password) builder.setWpa2Passphrase(password);
        //wifiManager.removeNetworkSuggestions(suggestionsList)
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int status = wm.addNetworkSuggestions(Collections.singletonList(builder.build()));
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            networkCallback.onUnavailable();
            return;
        }
        final Timer timer = new Timer();
        BroadcastReceiver netRec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {//等了很久这里根本不来，所以系统未采纳建议去连接指定的WiFi
                if (!intent.getAction().equals(
                        WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    return;
                }
                timer.cancel();
                context.unregisterReceiver(this);
                networkCallback.onAvailable(null);
            }
        };
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                networkCallback.onUnavailable();
                context.unregisterReceiver(netRec);
            }
        }, 1000 * 20);
        IntentFilter intentFilter = new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
        context.registerReceiver(netRec, intentFilter);
    }
}