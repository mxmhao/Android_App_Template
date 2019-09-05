package utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
}
