package template;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import min.test.android_app_template.R;

public class BottomSheetDialogTheme {

    /* 主要是看R.style.BottomSheetBgNullTheme和AppTheme中的bottomSheetDialogTheme */
    public static void test(Context context) {
        //去掉BottomSheetDialog的背景，方便自定义视图添加圆角什么的
        final BottomSheetDialog bsd = new BottomSheetDialog(context, R.style.BottomSheetBgNullTheme);
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsd.dismiss();
            }
        };
        bsd.setContentView(view);
        bsd.show();
    }
}
