package template;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class UtilTemplates {
    private static final String TAG = "UtilTemplates";

    private TextToSpeech tts;
    public void speak(Context context, String text) {
        if (null != tts) {
            speak(text);
            return;
        }
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
}
