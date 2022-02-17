package template;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.StringRes;

import java.util.Locale;

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
            cm.setPrimaryClip(ClipData.newPlainText(null, null));
        }, 1000);
    }
}
