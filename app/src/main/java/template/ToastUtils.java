package template;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {

    private static Toast toast;
    public static void show(Context context, CharSequence text, int duration) {
        if (null == toast) {
            toast = Toast.makeText(context, text, duration);
        } else {
            toast.setText(text);
            toast.setDuration(duration);
        }
        toast.show();
    }
    public static void show(Context context, @StringRes int resId, int duration) {
        show(context, context.getResources().getText(resId), duration);
    }

    private static Toast centerToast;
    public static void showCenter(Context context, CharSequence text, int duration) {
        if (null == centerToast) {
            centerToast = Toast.makeText(context, text, duration);
            centerToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            centerToast.setText(text);
            centerToast.setDuration(duration);
        }
        centerToast.show();
    }
    public static void showCenter(Context context, @StringRes int resId, int duration) {
        showCenter(context, context.getResources().getText(resId), duration);
    }

    private static Toast blackToast;
    private static TextView textView;
    public static void showBlack(Context context, CharSequence text, int duration) {
        if (null == blackToast) {
            blackToast = new Toast(context);
            blackToast.setGravity(Gravity.CENTER, 0, 0);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(Color.argb(185, 0, 0, 0));
            drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()));

            textView = new TextView(context);
            textView.setBackground(drawable);
            textView.setTextSize(17);
            textView.setTextColor(Color.WHITE);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
            textView.setPadding(padding, padding, padding, padding);
            blackToast.setView(textView);
        }
        blackToast.setDuration(duration);
        textView.setText(text);
        blackToast.show();
    }
    public static void showBlack(Context context, @StringRes int resId, int duration) {
        showBlack(context, context.getResources().getText(resId), duration);
    }
}
