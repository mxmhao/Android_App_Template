package template.fragment_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentActivity extends AppCompatActivity {

    public static final String FRAGMENT_CLASS = "FragmentClass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Class fs = Class.forName(getIntent().getStringExtra(FRAGMENT_CLASS));
            //这里作为root view不加入到返回栈中，使back键可以关闭Activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, (Fragment) fs.newInstance())
                    .commit();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接启动activity
     * @param context
     * @param cls
     */
    public static void startFragment(Context context, Class<? extends Fragment> cls) {
        context.startActivity(new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_CLASS, cls.getName()));
    }

    /**
     * 获取启动的Intent，用户设置一些传递参数，然后用户自己启动
     * @param context
     * @param cls
     * @return
     */
    public static Intent getStartIntent(Context context, Class<? extends Fragment> cls) {
        return new Intent(context, FragmentActivity.class).putExtra(FRAGMENT_CLASS, cls.getName());
    }
}
