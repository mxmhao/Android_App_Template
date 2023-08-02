package utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class Utils {
    /**
     * 根据文件路径，获取MIME类型
     */
    public static String getMIME(String path) {
        //方式一：
        //不建议使用getFileExtensionFromUrl方法，它总是返回空，应该是google的问题
        String extension = MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase();
        //这个还是可以用的
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        //这个是用来获取媒体的元信息的，无法获取其他MIME，只能获取音乐，视频等MIME
        /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);//path文件要真实存在，远程、本地都可，否则报错
        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);*/
        return mime;
    }

    /**
     * 判断字符串是否为IP地址
     *
     * https://baijiahao.baidu.com/s?id=1597417805306906227&wfr=spider&for=pc
     *
     * 链路本地地址，前缀FE80::/10
     * 注意，很容易会把链路本地地址和IPv4的私网/内网地址对应起来，
     * 其实链路本地地址对应于IPv4的APIPA地址，也就是169.254开
     * 头的地址（典型场景就是windows开启自动获取地址而获取失败后
     * 自动分配一个169.254的地址），使用“链路本地地址”时，要加上
     * scope_id，scope_id指定了使用哪个网络接口（网卡）
     *
     * 唯一本地地址：前缀FC00::/7，相当于IPv4的私网地址（10.0.0.0、172.16.0.0、192.168.0.0）
     */
    public static void isNetAddr(String addr) {//判断是不是网络地址
        //android要在非主线程中
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = null;
                InetAddress ia = null;
                try {
                    /*getByName可以是IPv4、IPv6、主机名、域名
                     * IPv4、IPv6不会发起网络请求，其它的会*/
                    ia = InetAddress.getByName("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789%8");
                    ip = ia.getHostName();
//                    ia.isReachable(2000); // Android自带的ping功能，判断地址是否可达
                } catch (UnknownHostException e) {//当ip地址不正确，或者主机名、域名连接不上时就会报错
                    return;
                }

                if (ia instanceof Inet6Address && countChar(ip, ':') > 1) {
                    //是IPv6地址，拼接端口号时单独处理
                } else {
                    //其他的包括IPv4、url拼接端口号时的处理都相同
                }
            }
        }).start();
    }
    private static int countChar(String content, char ch) {
        int index = 0;
        int count = 0;
        while ((index = content.indexOf(ch, index) + 1) > 0) {
            count++;
        }
        return count;
    }

    /**
     * 分享到邮件
     * https://blog.csdn.net/qq_23892379/article/details/80911994
     * @param activity
     * @param title 标题
     * @param message 内容
     */
    public static void emailShare(Activity activity, String title, String message) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);//设置分享行为
        intent.setType("text/plain");//设置分享内容的类型
        //这行是必须的，否则无法调起Gmail
        intent.setData(Uri.parse("mailto:"));//后面可添加完整的Email地址
        intent.putExtra(Intent.EXTRA_SUBJECT, title);//添加标题
        intent.putExtra(Intent.EXTRA_TEXT, message);//添加分享内容
        activity.startActivity(intent);
    }

    /**
     * WiFi判断
     * @param context android.content.Context
     */
    public static boolean isWiFiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return cm.getNetworkCapabilities(cm.getActiveNetwork())
                    .hasTransport(NetworkCapabilities.TRANSPORT_WIFI);//Wi-Fi
//            NetworkCapabilities.TRANSPORT_CELLULAR//蜂窝网络，2/3/4/5G
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /*
    获取指定语言环境下的字符串
    注意如果使用的是App Bundle，则需要确保禁用语言拆分或动态安装其他语言，可以在build.gradle中设置:
    android {

        ...
        bundle {
            language {
                // Specifies that the app bundle should not support
                // configuration APKs for language resources. These
                // resources are instead packaged with each base and
                // dynamic feature APK.
                enableSplit = false
            }
        }
    }
     */
    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(requestedLocale);//Locale.CHINESE; Locale.ENGLISH;
            result = context.createConfigurationContext(config).getResources().getString(resourceId);
        } else { // support older android versions
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, null);

            // retrieve resources from desired locale
            result = resources.getString(resourceId);

            // restore original locale
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, null);
        }

        return result;
    }

    /**
     * 判断当前是否为鸿蒙系统
     *
     * @return 是否是鸿蒙系统，是：true，不是：false
     */
    private static boolean isHarmonyOS() {
        try {
            Class<?> buildExClass = Class.forName("com.huawei.system.BuildEx");
            Object osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass);
            if (osBrand == null) {
                return false;
            }
            return "harmony".equalsIgnoreCase(osBrand.toString()); //log打印为： harmony
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获取鸿蒙系统 Version
     *
     * @return HarmonyOS Version
     */
    public static String getHarmonyVersion() {
        // HarmonyOS系统版本号
        return getProp("hw_sc.build.platform.version", "unknown");
        // HarmonyOS SDK版本号，与系统版本号不同
//        return getProp("hw_sc.build.os.version", "");
    }

    public static String getHarmonySdkInt() {
//        return getProp("hw_sc.build.platform.version.sdk", "");
        return getProp("hw_sc.build.os.apiversion", "unknown");
    }

    private static String getProp(String property, String defaultValue) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> spClz = Class.forName("android.os.SystemProperties");
            Method method = spClz.getDeclaredMethod("get", String.class);
            String value = (String) method.invoke(spClz, property);
            if (TextUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        } catch (Throwable e) {
            Log.e("Utils", "getProp: ", e);
        }
        return defaultValue;
    }

    /**
     * 判断activity是否正在显示
     * @param activity Activity
     * @return true：正在显示
     */
    public static boolean activityVisible(Activity activity) {
        return activity.getWindow().getDecorView().getVisibility() == View.VISIBLE;
    }
}
