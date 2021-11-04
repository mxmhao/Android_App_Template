package template;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import test.mxm.android_app_template.R;

public class TranslucentBarActivity extends AppCompatActivity {

    //R.style.LauncherTheme、R.style.TranslucentBarTheme 这两个里面的介绍很重要
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window = getWindow();
            //这个会把底部的导航栏的背面也用上，官方注释说可以与WindowInsets.Type.statusBars()等配合，但是没测试出来效果
            window.setDecorFitsSystemWindows(false);
//            window.getAttributes().setFitInsetsTypes(WindowInsets.Type.statusBars());
            //高亮显示状态栏，黑色
            window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            //获取导航栏高度
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (0 != resourceId) {
                int navigation_bar_height = getResources().getDimensionPixelSize(resourceId);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) window.getDecorView().findViewById(android.R.id.content).getLayoutParams();
                //防止底部被导航栏遮住
                layoutParams.bottomMargin = navigation_bar_height;
//                window.getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, navigation_bar_height);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private View splashView;
    /**
     * 可当广告页
     * 在最上层view中加入一个和启动页一样的页面，假装延长启动页的显示时间，等资源加载完成后移除假的启动页：
     * 在主题中 有两种设置启动页的方式，选其一：
     *  android:windowBackground 只能设置 图片资源 或 layer-list等图层的资源文件
     *  android:windowSplashscreenContent (安卓8.0以上) 能设置 图片资源 或 layer-list等图层资源文件 或 layout
     */
    private void initSplash() {
        /**
         * 使用layout去匹配启动页，layout中的imageview的 底部margin 和 layer-list的图片item bottom的值一样
         * 当 android:windowSplashscreenContent 设置的是layout 时，layout加入 android.R.id.content 的view 或 DecorView中
         * 都一样，人眼看不出切换差异
         * 当 android:windowSplashscreenContent 设置的是layer-list等图层 或图片 时，layout加入 android.R.id.content 的view 或
         * DecorView 都会有底部导航栏的高度的切换差异感，下面会有去掉差异的方法
         * 当使用 android:windowBackground 做启动页时，layout加入 DecorView 无切换差异感，而加入 android.R.id.content 的view，
         * 有差异感
         */
        /*ViewGroup vg = (ViewGroup) getLayoutInflater().inflate(R.layout.launch_splash, null);
        splashView = vg;
        ((ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content)).addView(vg);
//        ((ViewGroup) getWindow().getDecorView()).addView(vg);
        //去掉差异感
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            View iv = vg.getChildAt(0);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) iv.getLayoutParams();
            lp.bottomMargin -= getResources().getDimensionPixelSize(resourceId);//+或-，请根据测试调，测试多次，否则可能有上次的缓存导致调试不准确
        }*/

        /**
         * 当使用 android:windowBackground 作为启动页时，此ImageView适合添加到DecorView中，人眼看不出切换差异
         * 但使用 android:windowSplashscreenContent 做启动页时，不要加入到DecorView，会有一个底部导航栏的差异
         * 可以加入到 android.R.id.content 的view中，不会有底部导航栏的差异，人眼看不出切换差异
         */
        ImageView iv = new ImageView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iv.setLayoutParams(layoutParams);
        splashView = iv;
        iv.setImageResource(R.drawable.launcher_splash);
        ((ViewGroup) getWindow().getDecorView()).addView(iv);
//        ((ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content)).addView(iv);
    }

    //动画隐藏启动页
    private void hideSplash() {
//            getWindow().getDecorView().setBackground(null);
//            getWindow().setBackgroundDrawable(null);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(splashView, "alpha", 1f, 0f);
            animator.setDuration(300);//时间
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup)splashView.getParent()).removeView(splashView);
                    splashView = null;
                }
            });
            animator.start();
        }, 500);
    }
}