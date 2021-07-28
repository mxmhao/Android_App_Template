package template;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class WiFiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int state = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (state == PackageManager.PERMISSION_GRANTED) {
            new WiFiUtils().startScan(this, new WiFiUtils.ScanResults() {
                @Override
                public void didWiFiScanResult(List<String> results) {

                }
            });
        } else if (state == PackageManager.PERMISSION_DENIED) {
            //调到权限设置界面
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new WiFiUtils().startScan(this, new WiFiUtils.ScanResults() {
                @Override
                public void didWiFiScanResult(List<String> results) {

                }
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mWiFiManager.isWifiEnabled()) {
                        mWiFiManager.startScan();
                    }
                }
            });
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            new AlertDialog.Builder(context)
                    .setTitle("WiFi还未开启")
                    .setMessage("请先开启WiFi")
                    .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.registerReceiver(WiFiUtils.this, filter);
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
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
}