package template;

import android.app.AlertDialog;
import android.content.Context;

import test.mxm.android_app_template.R;

//https://www.jianshu.com/p/06a3bbb7ce79
public class AlertDialogTheme {

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
                .setPositiveButton("OK", null)
                .show();

        //通过这种方法修改消息体
//        TextView messageText = dialog.findViewById(android.R.id.message);
//        messageText.setTextSize(20);
    }
}