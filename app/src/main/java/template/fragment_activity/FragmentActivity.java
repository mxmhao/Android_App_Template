package template.fragment_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

//使用final后，编译器会有方法内联优化
public final class FragmentActivity extends AppCompatActivity {

    private static final String FRAGMENT_CLASS = "FragmentClass";
    private static final String FRAGMENT_OBJECT = "FragmentObject";
    private static final String TAG = "FragmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            long key = getIntent().getLongExtra(FRAGMENT_OBJECT, Long.MIN_VALUE);
            Fragment fragment = fragmentMap.get(key);
            fragmentMap.remove(key);

            if (fragment == null) {
                Class fs = Class.forName(getIntent().getStringExtra(FRAGMENT_CLASS));
                fragment = (Fragment) fs.newInstance();
            }

            //这里作为root view不加入到返回栈中，使back键可以关闭Activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            Log.e(TAG, "", e);
        }
    }

    /**
     * 直接启动activity
     * @param context Context
     * @param cls Class extends Fragment
     */
    public static void startFragment(Context context, Class<? extends Fragment> cls) {
        context.startActivity(new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_CLASS, cls.getName()));
    }

    private static final LongSparseArray<Fragment> fragmentMap = new LongSparseArray<>(2);
    public static void startFragment(Context context, Fragment fragment) {
        long key = SystemClock.elapsedRealtime();
        fragmentMap.put(key, fragment);
        context.startActivity(new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_OBJECT, key));
    }

    /**
     * 获取启动的Intent，用户设置一些传递参数，然后用户自己启动
     * @param context Context
     * @param cls Class extends Fragment
     * @return Intent
     */
    public static Intent getStartIntent(Context context, Class<? extends Fragment> cls) {
        return new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_CLASS, cls.getName());
    }

    public static Intent getStartIntent(Context context, Fragment fragment) {
        long key = SystemClock.elapsedRealtime();
        fragmentMap.put(key, fragment);
        return new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_OBJECT, key);
    }
}
