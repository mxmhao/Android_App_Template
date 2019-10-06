package template;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.StringRes;

import test.mxm.android_app_template.R;

//https://www.jianshu.com/p/06a3bbb7ce79
public class AlertDialogTheme {
    private static final String TAG = "AlertDialogTheme";

    /* 主要是看R.style.AlertDialog和AppTheme中的alertDialogTheme */
    public static void test(Context context) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            sb.append("消息消息消息消息消息消息消息消息消息消");
        }
        //设置了全局alertDialogTheme
//        AlertDialog dialog = new AlertDialog.Builder(context)
        //单个使用
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialog)
                .setTitle("标题")
                .setMessage(sb.toString())
                .setNegativeButton("Cancel", null)//android.R.id.button2
                .setNeutralButton("Neutral", null)//android.R.id.button3
                .setPositiveButton("OK", null)//android.R.id.button1
                .show();

        //可以通过这种方法修改消息体和按钮，以下所有view只能在show()之后获取，create()之后都不行
        TextView text = dialog.findViewById(android.R.id.message);
        text.setTextSize(20);
        Button btn1 = dialog.findViewById(android.R.id.button1);
        Button btn2 = dialog.findViewById(android.R.id.button2);
        Button btn3 = dialog.findViewById(android.R.id.button3);
        //也可以这么获取
//        Button btn1 = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        Button btn2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        Button btn3 = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        Log.e(TAG, "btn1: " + btn1.getText());
        Log.e(TAG, "btn2: " + btn2.getText());
        Log.e(TAG, "btn3: " + btn3.getText());
    }

    public void test2(Context context) {
        new Builder(context)
                .setTitle("title")
                .setMessage("hello")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: Ok");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "onClick: Cancel");
                    }
                })
                .show();
    }
}

/**
 * 仿iOS的Alert，但API尽量和AlertDialog.Builder一致
 */
class Builder {
    private Context context;
    public Builder(Context context) {
        this.context = context;
    }

    private CharSequence mTitle;
    public Builder setTitle(CharSequence title) {
        mTitle = title;
        return this;
    }
    public Builder setTitle(@StringRes int titleId) {
        mTitle = context.getText(titleId);
        return this;
    }

    private CharSequence mMessage;
    public Builder setMessage(CharSequence message) {
        mMessage = message;
        return this;
    }
    public Builder setMessage(@StringRes int messageId) {
        mMessage = context.getText(messageId);
        return this;
    }

    private CharSequence leftBtnText;
    private DialogInterface.OnClickListener leftBtnClick;
    public Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener onClickListener) {
        leftBtnText = text;
        leftBtnClick = onClickListener;
        return this;
    }
    public Builder setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener onClickListener) {
        leftBtnText = context.getText(textId);
        leftBtnClick = onClickListener;
        return this;
    }

    private CharSequence rightBtnText;
    private DialogInterface.OnClickListener rightBtnClick;
    public Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener onClickListener) {
        rightBtnText = text;
        rightBtnClick = onClickListener;
        return this;
    }
    public Builder setPositiveButton(@StringRes int textId, DialogInterface.OnClickListener onClickListener) {
        rightBtnText = context.getText(textId);
        rightBtnClick = onClickListener;
        return this;
    }

    private boolean mCancelable = true;
    public Builder setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        return this;
    }

    private DialogInterface.OnCancelListener mOnCancelListener;
    public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
        return this;
    }

    private DialogInterface.OnDismissListener mOnDismissListener;
    public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        return this;
    }

    private DialogInterface.OnKeyListener mOnKeyListener;
    public Builder setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        mOnKeyListener = onKeyListener;
        return this;
    }

    public AlertDialog create() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_2_btn_alert, null, false);

        AlertDialog dialog = new AlertDialog.Builder(context)//R.style.AlertDialogTheme
                .setView(view)
                .setCancelable(mCancelable)
                .setOnCancelListener(mOnCancelListener)
                .setOnDismissListener(mOnDismissListener)
                .setOnKeyListener(mOnKeyListener)
                .create();

        //设置AlertDialog背景，和使用R.style.AlertDialogTheme创建的AlertDialog效果相同
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                context.getResources().getDisplayMetrics()));
        InsetDrawable iDrawable = new InsetDrawable(drawable,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26,
                        context.getResources().getDisplayMetrics()));
        dialog.getWindow().setBackgroundDrawable(iDrawable);

        //设置自定义视图dialog_2_btn_alert
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView btnLeft = view.findViewById(R.id.btn2);
        TextView btnRight = view.findViewById(R.id.btn1);

        if (!TextUtils.isEmpty(mTitle)) {
            tvTitle.setText(mTitle);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(mMessage)) {
            tvMessage.setText(mMessage);
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(leftBtnText)) {
            btnLeft.setText(leftBtnText);
            btnLeft.setOnClickListener(new DialogOnClick(dialog, leftBtnClick, DialogInterface.BUTTON_NEGATIVE));
        } else {
            btnLeft.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(rightBtnText)) {
            btnRight.setText(rightBtnText);
            btnRight.setOnClickListener(new DialogOnClick(dialog, rightBtnClick, DialogInterface.BUTTON_POSITIVE));
        } else {
            btnRight.setVisibility(View.GONE);
        }

        return dialog;
    }

    public AlertDialog show() {
        AlertDialog dialog = create();
        dialog.show();
        return dialog;
    }
}

class DialogOnClick implements View.OnClickListener {
    private DialogInterface mDialog;
    private DialogInterface.OnClickListener mOnClickListener;
    private int mWhichButton;

    public DialogOnClick(DialogInterface dialog, DialogInterface.OnClickListener onClickListener, int whichButton) {
        mDialog = dialog;
        mOnClickListener = onClickListener;
        mWhichButton = whichButton;
    }

    @Override
    public void onClick(View v) {
        mDialog.dismiss();
        if (null != mOnClickListener) {
            mOnClickListener.onClick(mDialog, mWhichButton);
        }
        mDialog = null;
        mOnClickListener = null;
    }
}