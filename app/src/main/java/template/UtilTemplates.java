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
import android.os.Looper;
import android.os.StatFs;
import android.speech.tts.TextToSpeech;
import android.text.format.Formatter;
import android.util.Log;

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
}
