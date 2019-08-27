package template;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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
        Log.e(TAG, "btn1: " + btn1.getText());
        Log.e(TAG, "btn2: " + btn2.getText());
        Log.e(TAG, "btn3: " + btn3.getText());
    }
}