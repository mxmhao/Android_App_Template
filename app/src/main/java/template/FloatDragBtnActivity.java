package template;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import test.mxm.android_app_template.R;

public class FloatDragBtnActivity extends AppCompatActivity {

    public static final String TAG = "FloatDragBtnActivity";

    @SuppressLint("ClickableViewAccessibility, InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //以下为全部悬浮拖动按钮内容，使用的是setOnTouchListener，而非继承类
        View textView = getLayoutInflater().inflate(R.layout.float_drag_btn, null);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(v -> {
            Log.e(TAG, "onCreate: ");
            ((TextView)v).setTextColor(Color.GREEN);
        });
        textView.setY(900);
        textView.setX(Resources.getSystem().getDisplayMetrics().widthPixels - 200);
        textView.setOnTouchListener(new View.OnTouchListener() {
            private int touchX = 0;
            private int touchY = 0;
            private int beginX = 0;
            private int beginY = 0;
            private int w = 0;
            private int h = 0;
            private int dx = 0;
            private int dy = 0;
            private int parentW = 0;
            private int parentH = 0;
            private int navigationBarH = 0;
            private int statusBarH = 0;

            @SuppressLint("InternalInsetResource")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Resources resources = v.getContext().getResources();
                        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                        navigationBarH = getResources().getDimensionPixelSize(resourceId);
                        Log.e(TAG, "onTouch: navigation_bar_height " + navigationBarH);
                        resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
                        statusBarH = getResources().getDimensionPixelSize(resourceId);
                        Log.e(TAG, "onTouch: status_bar_height " + statusBarH);
                        w = v.getWidth();
                        h = v.getHeight();
                        View view = (View) v.getParent();
                        parentW = view.getWidth();
                        parentH = view.getHeight();
                        v.setPressed(true);
                        touchX = (int) v.getX();
                        touchY = (int) v.getY();
                        beginX = touchX;
                        beginY = touchY;
                        // 点击事件起始位置到原点的距离
                        dx = (int) (event.getRawX() - beginX);
                        dy = (int) (event.getRawY() - beginY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touchX = (int)event.getRawX();// x轴拖动的绝对距离
                        touchY = (int)event.getRawY();// y轴拖动的绝对距离
                        touchX -= dx;
                        touchY -= dy;
                        if (Math.abs(touchX - beginX) < 40 && Math.abs(touchY - beginY) < 40) {
                            break;
                        }
                        if (touchX < 0) {
                            touchX = 0;
                        }
                        if (touchY < statusBarH) {
                            touchY = statusBarH;
                        }
                        if (touchX + w > parentW) {
                            touchX = parentW - w;
                        }
                        if (touchY + h > parentH - navigationBarH) {
                            touchY = parentH - h - navigationBarH;
                        }
                        v.setX(touchX);
                        v.setY(touchY);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "onTouch: ACTION_UP " + Math.abs(touchX - beginX) + ", " + Math.abs(touchY - beginY));
                        // 以下5行代码必须这个么写，先后顺序不能颠倒，否则无法触发 Click 事件
                        if (Math.abs(touchX - beginX) < 40 && Math.abs(touchY - beginY) < 40) {
                            v.onTouchEvent(event);
                        }
                        v.setPressed(false);
                        return true;
                    default: break;
                }
                return false;
            }
        });

        ((ViewGroup)(getWindow().getDecorView())).addView(textView);
    }
}