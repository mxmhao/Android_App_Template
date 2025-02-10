package template;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.StatFs;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputType;
import android.text.format.Formatter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class UtilTemplates {
    private static final String TAG = "UtilTemplates";

    /**
     * //android 11以上要在AndroidManifest.xml文件中加入
     <queries>
     <intent>
     <action android:name="android.intent.action.TTS_SERVICE" />
     </intent>
     </queries>
     */
    private TextToSpeech tts;
    public void speak(Context context, String text) {
        if (null != tts) {
            speak(text);
            return;
        }
        //第一次会有授权框弹出
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.CHINA);
                if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE) {
                    Log.e(TAG, "TTS暂时不支持这种语音的朗读！");
                    return;
                }
                speak(text);
            }
        });
    }
    private void speak(String text) {
//        tts.setSpeechRate(0.4f);//语速
//        tts.setPitch(0.1f);//音调
//        tts.setVoice()//设置声音
//        tts.synthesizeToFile()//文字转换成音频文件
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    //获取图片长宽
    public static BitmapFactory.Options getImage(Resources resources, @StringRes int id) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;//仅解码图片的长宽
        BitmapFactory.decodeResource(resources, id , o);
        Log.e("TAG", "initData: " + o.outWidth + ", " + o.outHeight);
        return o;
    }

    /**
     * 获取对应 density 下的原图，例如：当前手机 density=2.75，R.drawable.xxxx获取的图片是就近density=3的图片
     * 但是，系统默认会把density=3的图片缩放成density=2.75的图片，但我们不想让它缩放，就用此方法
     */
    public static Bitmap getNoScaledImage(Resources resources, @StringRes int id) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;//设置不用根据屏幕分辨率缩放，可以得到原尺寸大小的图片
        Bitmap bmp = BitmapFactory.decodeResource(resources, id , o);
        Log.e("TAG", "initData: " + bmp.getWidth() + ", " + bmp.getHeight());
        return bmp;
    }

    //获取剪切板的类容
    public void getClipboardText(Context context) {
        //如果是在app启动时获取，请在onStart之后调用，且必须延迟，不然获取不到剪切板的类容。这是Android10起开始的问题，必须是在app获取焦点后获取
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (!cm.hasPrimaryClip()) return;

            CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
            if (null == text) return;
            Log.e(TAG, "getFromClipboard text=" + text + "  " + cm.getPrimaryClipDescription().getMimeType(0));
            // 设置剪切板，8.0及以下系统要在主线程中调用，不然会设置失败
            cm.setPrimaryClip(ClipData.newPlainText(null, null));//这里是用设置的null方式清空
        }, 1000);
    }

    /**
     * 获取指定文件夹大小
     */
    public static long getDirSize(String dirPath) {
        return getDirSize(new File(dirPath));
    }

    /**
     * 获取指定文件夹大小
     */
    public static long getDirSize(File dir) {
        if (null == dir || !dir.exists()) return 0;

        if (!dir.isDirectory()) {
            return dir.length();
        }
        File[] files = dir.listFiles();
        if (null == files || files.length == 0) return 0;

        long size = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                size = size + getDirSize(file);
//                size = size + file.length() + getDirSize(file);//文件夹本身的长度要不要加上？
            } else {
                size += file.length();
            }
        }

        return size;
    }

    /**
     * 获取某个目录的可用空间
     */
    public static long getAvailableSpace(String path) {
        StatFs statfs = new StatFs(path);
        long size = statfs.getBlockSizeLong();//获取分区的大小
        long count = statfs.getAvailableBlocksLong();//获取可用分区块的个数
        return size * count;// = statfs.getAvailableBytes()
    }
    /**
     * 获取手机内部存储可用空间
     */
    public static long getDataAvailableSpace() {
        return new StatFs(Environment.getDataDirectory().getAbsolutePath()).getAvailableBytes();
    }
    /**
     * 测试
     */
    public static void showAvailableSize(Context context) {
        long romSize = getAvailableSpace(Environment.getDataDirectory().getAbsolutePath());//手机内部存储大小
        long sdSize = getAvailableSpace(Environment.getExternalStorageDirectory().getAbsolutePath());//外部存储大小
        long dataSize = getAvailableSpace("/data");//外部存储大小
        Log.e(TAG, "showAvailableSize: rom: " + Formatter.formatFileSize(context, romSize));
        Log.e(TAG, "showAvailableSize: sd: " + Formatter.formatFileSize(context, sdSize));
        Log.e(TAG, "showAvailableSize: sd: " + Formatter.formatFileSize(context, dataSize));
    }

    /**
     * 原C语言的算法：
     static unsigned short const kCrc16tab[] = {
     0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
     0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
     };
     static unsigned short crc16_xmodem(unsigned char *ptr, unsigned int len)
     {
     unsigned short crc = 0;
     unsigned char ch = 0;

     printf("2-len: %d\n", len);
     while (len-- != 0)
     {
     ch = crc >> 12;
     crc <<= 4;
     crc ^= kCrc16tab[ch ^ (*ptr / 16)];

     ch = crc >> 12;
     crc <<= 4;
     crc ^= kCrc16tab[ch ^ (*ptr & 0x0f)];
     ptr++;
     }
     return crc;
     }
     */
    public static short crc16Xmodem(byte[] bytes, final int off, final int len) {
        final int[] crc16tab = {
                0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
        };
        int count = off + len;
        int ptr = off;
        short crc = 0;
        byte ch;
        while (count-- != off) {
            /*
             这里是仿C语言算法，C语言的 '>>' 高位会补0；网上查询说Java的 '>>>' 也是 高位会补0，但是遇到负数时，高位没有补0，这是个bug？
             这段代码在"JDK1.8"上运行可以看到高位没有补0
             public static void main(String[] args) {
                short aa = -1;
                System.out.println(String.format("%X", aa >>> 12));
                System.out.println(String.format("%X", aa >> 12));
                System.out.println(String.format("%X", (short)(aa >>> 12)));
                System.out.println(String.format("%X", (short)(aa >> 12)));
            }
             */
            ch = (byte) ((crc >>> 12) & 0x000f);
            crc <<= 4;
            crc ^= crc16tab[(ch ^ (bytes[ptr] / 16))];

            ch = (byte) ((crc >>> 12) & 0x000f);
            crc <<= 4;

            crc ^= crc16tab[(ch ^ (bytes[ptr] & 0x0f))];
            ptr++;
        }
        return crc;
    }

    // KeyStore 加载证书，Android 未限制必须是BKS格式的证书：https://developer.android.com/training/articles/security-ssl#java 文档中介绍的很详细，建议仔细阅读
    public static SSLSocketFactory getSSLSocketFactory(InputStream keyStore, String password) throws UnsupportedOperationException {
        // 此方式只支持.bks的证书
        try{
            KeyStore trustStore = KeyStore.getInstance("BKS");
            trustStore.load(keyStore, password.toCharArray());
//            keyStore.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext ctx = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 开始支持1.3
                ctx = SSLContext.getInstance("TLSv1.3");
            } else {
                ctx = SSLContext.getInstance("TLSv1.2");
            }
            ctx.init(null, tm, null);

            return ctx.getSocketFactory();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static SSLSocketFactory getSSLSocketFactory2(InputStream keyStore) throws UnsupportedOperationException {
        // 这是不带密码的.bks的证书
        try{
            KeyStore trustStore = KeyStore.getInstance("BKS");
            trustStore.load(keyStore, new char[0]);
//            keyStore.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext ctx = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 开始支持1.3
                ctx = SSLContext.getInstance("TLSv1.3");
            } else {
                ctx = SSLContext.getInstance("TLSv1.2");
            }
            ctx.init(null, tm, null);

            return ctx.getSocketFactory();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    // CertificateFactory 加载证书，然后设置到 KeyStore
    public static SSLSocketFactory getSSLSocketFactory(InputStream keyStore) throws UnsupportedOperationException {
        // 此方式支持各种 X.509 标准格式的证书，如：crt、cer、der等，开发者最好自己测试一遍
        try{
            // X.509 或 X509 都可
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // 只包含一个证书
            Certificate ca = cf.generateCertificate(keyStore);
            // 文件包含多个证书
//            Collection<? extends Certificate> ces = cf.generateCertificates(keyStore);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", ca);
            // X509 可，X.509 报错
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext ctx = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 开始支持1.3
                ctx = SSLContext.getInstance("TLSv1.3");
            } else {
                ctx = SSLContext.getInstance("TLSv1.2");
            }
            ctx.init(null, tm, null);

            return ctx.getSocketFactory();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new UnsupportedOperationException(e);
        } finally {
            streamClose(keyStore);
        }
    }

    // KeyStore 空InputStream，KeyManagerFactory加载，这种用的少
    public static SSLSocketFactory getSSLSocketFactory2(InputStream keyStore, String password) throws UnsupportedOperationException {
        // 此方式是双向验证证书？
        try{
            // X.509 或 X509 都可
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // 只包含一个证书
            Certificate ca = cf.generateCertificate(keyStore);
//            keyStore.close();
            // 文件包含多个证书
//            Collection<? extends Certificate> ces = cf.generateCertificates(keyStore);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // KeyStore 空InputStream
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", ca);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(trustStore, null);
            KeyManager[] kms = kmf.getKeyManagers();
            // X509 可，X.509 报错
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext ctx = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 开始支持1.3
                ctx = SSLContext.getInstance("TLSv1.3");
            } else {
                ctx = SSLContext.getInstance("TLSv1.2");
            }
            ctx.init(kms, tm, null);

            return ctx.getSocketFactory();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static void streamClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {}
    }

    /**
     * EditText 输入限制
     */
    private static void editTextLimiter(Context context) {
        EditText et = new EditText(context);

        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        // 限制范围 0 ~ 65535
        et.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) > 65535) {
                    s.delete(4, 5);
                }
            }
        });
        // 限制输入1 ~ 100
        et.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                int len = s.length();
                int num;
                if (len > 0 && ((num = Integer.parseInt(s.toString())) > 100 || num < 1)) {
                    s.delete(len - 1, len);
                }
            }
        });

        // 设置只能输入数字和小数点，且会限制软键盘的类型
        et.setInputType((InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER));
        // 限制输入0 ~ 90，只能输入一个小数点，保留两位小数
        et.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                String num = s.toString();
                final int point = num.indexOf('.');
                final int len = s.length();
                // 保留2位小数
                if ((0 == point && 1 == len) || (-1 != point && len - 1 - point > 2) || (len > 0 && Float.parseFloat(num) > 90)) {
                    s.delete(len - 1, len);
                }
            }
        });

        // 设置只能输入数字、小数点、正负号，且会限制软键盘的类型
        et.setInputType((InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED));
        // 限制输入，只能输入一个小数点，保留两位小数
        et.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                final int len = s.length();
                if (0 == len) return;

                String string = s.toString();
                if (1 == len && ("-".equals(string) || "+".equals(string))) return;

                final int point = string.indexOf('.');
                float num;
                // 保留2为小数点
                if ((0 == point && 1 == len) || (-1 != point && len - 1 - point > 2) || ((num = Float.parseFloat(string)) > 3.40E+38f || num < -(3.40E+38f))) {
                    s.delete(len - 1, len);
                }
            }
        });

        // 限制字符只能输入数字、正负号
        et.setInputType((InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));

        // 限制字符只能输入 1234567890ABCDEFabcdef 中的字符------------
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        // android:digits="1234567890ABCDEFabcdef" 的代码写法：
        et.setKeyListener(DigitsKeyListener.getInstance("1234567890ABCDEFabcdef"));
        // -------------------------------------------------------

        // 集中焦点，才有键盘弹出
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();

        // 如果你的输入框在 Dialog 上，用这个才能有软键盘弹出
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // 如果上面的代码没有弹出软键盘 可以使用下面另一种方式
//        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//        imm.showSoftInput(etTime, 0);

        // 防止 Dialog 键盘弹起时底部布局被顶起，可以放在 onCreate 调用
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private static final int USER_ID_LENGTH = 16; // Id长度
    // 用时间戳生成一种随机id
    public static String getTimestampRandomId() {
        Random random = new Random();
        // 从2020 年 1 月 1 日 00:00:00 开始的时间戳。与iOS端一致
        StringBuilder sb = new StringBuilder((System.currentTimeMillis() - 1577836800000L) + (random.nextInt() % 2 == 0 ? "A" : "a"));
        for (int i = 0, len = USER_ID_LENGTH - sb.length(); i < len; i++) {
            sb.insert(random.nextInt(sb.length() - 1), (char) ((random.nextInt() % 2 == 0 ? 'A' : 'a') + random.nextInt(26)));
        }

        // 以‘a’或‘A’ 结尾表示安卓端。
        return sb.toString();
    }

    // 创建不在主线程工作的 Handler
    public static Handler workThreadHandler() {
        HandlerThread handlerThread = new HandlerThread("Not main Handler");
        handlerThread.start();
//        handlerThread.quit();// 退出
        // 将handler绑定到子线程中
        Handler handler = new Handler(handlerThread.getLooper());
        return handler;
    }

    // 用法一：
    public static void userOptional(Context context, A a) {
        TextView tv = new TextView(context);
        // 原来的写法
        if (null != a) {
            if (null != a.b) {
                if (null != a.b.c) {
                    if (null != a.b.c.name) {
                        tv.setText(a.b.c.name);
                    }
                }
            }
        }
        // 其实可以这么写，上面只是假设
//        if (null != a && null != a.b && null != a.b.c && null != a.b.c.name) {
//        }
        // 使用 Optional 写法，改善了多层嵌套
        Optional.ofNullable(a)
                .map(a1 -> a1.b) // map 转换操作
                .map(b1 -> b1.c)
                .map(c1 -> c1.name)
                .ifPresent(tv::setText); // 设置值

        Optional.ofNullable(a)
                .flatMap(a1 -> a1.getAddress(context))
                .ifPresent(tv::setText); // 设置值
    }

    public static class A {
        public B b;
        // 用法二：把 Optional 作为返回值，然后就可以像'用法一'那样操作了
        // Swift 和 kotlin 等语言有语法糖，Optional<String> 可以写成 "String?"，其本质还是 Optional<String>
        public Optional<String> getAddress(Context context) {
            if (null != context) {
                return Optional.of("aaaaa");
            }

            return Optional.empty();
        }
    }

    public static class B {
        public C c;
    }

    public static class C {
        public String name;
    }
}
