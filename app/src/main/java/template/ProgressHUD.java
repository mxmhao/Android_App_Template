package template;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

import test.mxm.android_app_template.R;

public class ProgressHUD {

    private static AlertDialog dialog;

    private static ProgressBar initProgressBar(Activity activity) {
        ProgressBar bar = new ProgressBar(activity, null, android.R.attr.progressBarStyle);
        int padding = dp2px(activity, 20);
        bar.setPadding(padding, padding, padding, padding);

        int wh = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, activity.getResources().getDisplayMetrics());
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(wh, wh);
        flp.gravity = Gravity.CENTER;
        bar.setLayoutParams(flp);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setColor(Color.argb(185, 0, 0, 0));
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, activity.getResources().getDisplayMetrics()));
        bar.setBackground(drawable);

        return bar;
    }

    /**
     * 进度条显示在整个window上面，下层view无法点击，使用{@link ProgressHUD#dismiss}移除
     * @param activity
     * @see ProgressHUD#dismiss
     */
    public static void showOnWindow(Activity activity) {
        ProgressBar bar = initProgressBar(activity);
        dialog = new AlertDialog.Builder(activity, R.style.NoBackgroundDialog)
                .setCancelable(false)
                .show();
        dialog.getWindow().setContentView(bar);
    }

    /**
     * @see ProgressHUD#showOnWindow(Activity)
     */
    public static void dismiss() {
        if (null == dialog) return;
        dialog.dismiss();
        dialog = null;
    }

    //使用弱引用，可以防止内存泄漏
    private static WeakReference<ProgressBar> wrpb;
    /**
     * 指示器显示在Activity的DecorView的上面，可以随着Activity的消失而消失，
     * 请在Activity销毁前调用{@link ProgressHUD#dismiss(Activity)}，否则可能造成内存泄漏
     * @param activity
     * @see ProgressHUD#dismiss(Activity)
     */
    public static void showOnContent(Activity activity) {
        ProgressBar pb = initProgressBar(activity);
        wrpb = new WeakReference<>(pb);
        ViewGroup vg = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        vg.addView(pb);
    }

    /**
     * @see ProgressHUD#showOnContent(Activity)
     * @param activity
     */
    public static void dismiss(Activity activity) {
        if (null == wrpb || wrpb.get() == null) return;
        ((ViewGroup) activity.getWindow().getDecorView()
                .findViewById(android.R.id.content)).removeView(wrpb.get());
        wrpb = null;
    }


    private static float scale = 0;
    private static int dp2px(Context context, float dpValue) {
        if (0 == scale) scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
