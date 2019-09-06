package utils.get_context_no_dependence_anything;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import test.mxm.android_app_template.BuildConfig;

/**
 * 借用的源码：
 * https://github.com/kaedea/Feya/blob/master/feya/applications/droid.feya/src/main/java/me/kaede/feya/context/AndroidHacks.java
 */
@SuppressWarnings("WeakerAccess")
public class AndroidHacks {
    private static final String TAG = "AndroidHacks";
    private static Object sActivityThread;

    @NonNull
    public static Object getActivityThread() {
        if (sActivityThread != null) return sActivityThread;
        synchronized (AndroidHacks.class) {
            if (sActivityThread != null) return sActivityThread;

            if (Looper.getMainLooper() == Looper.myLooper()) {
                sActivityThread = getActivityThreadFromUIThread();
                if (sActivityThread != null) return sActivityThread;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            synchronized (AndroidHacks.class) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sActivityThread = getActivityThreadFromUIThread();
                        synchronized (AndroidHacks.class) {
                            AndroidHacks.class.notifyAll();
                        }
                    }
                });
                try {
                    while (sActivityThread == null) {
                        AndroidHacks.class.wait();
                    }
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Waiting notification from UI thread error.", e);
                    }
                }
            }
        }
        return sActivityThread;
    }

    private static Object getActivityThreadFromUIThread() {
        Object activityThread = null;
        try {
            Method method = Class.forName("android.app.ActivityThread").getMethod("currentActivityThread");
            method.setAccessible(true);
            activityThread = method.invoke(null);
        } catch (final Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to get ActivityThread from ActivityThread#currentActivityThread. " +
                        "In some case, this method return null in worker thread.", e);
            }
        }
        return activityThread;
    }
}
