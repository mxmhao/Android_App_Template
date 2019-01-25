package template;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

//https://stackoverflow.com/questions/40176244/how-to-disable-bottomnavigationview-shift-mode
public class BottomNavigationFragment extends Fragment {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
            }
            return false;
        }
    };

    /*
    布局文件
    <android.support.design.widget.BottomNavigationView
        android:id="@+id/navigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="0dp"
        android:layout_marginStart="0dp"
        android:background="?android:attr/windowBackground"
        app:labelVisibilityMode="labeled"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:menu="@menu/navigation" />
    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        BottomNavigationView的item移动的原因有两个：
        1、选中的item显示title，而未选中的不显示；
            解决方案：1.设置所有item不显示title
                2.设置所有item显示title，这也是产生移动的第二个原因
        2、item有2个TextView，分别显示选中和未选中的title，但他们的textSize不一样
            解决方案：1.覆盖这两个textSize，设置成一样
         */
        BottomNavigationView navigation = new BottomNavigationView(container.getContext());
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //解决移动的第1个原因，此属性貌似在安卓8.0以后才有，
        //也可在布局文件中设置app:labelVisibilityMode="labeled"
//        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        /*
        解决移动的第2个原因
        然后把下面两个拷贝到dimens.xml文件中，注意：这两行的值要完全相等，以保证选中和未选中状态的字体大小一样
        <dimen name="design_bottom_navigation_active_text_size">12sp</dimen>
        <dimen name="design_bottom_navigation_text_size">12sp</dimen>

        //禁止item平移，但textSize不一样时，会有上下移动
        app:itemHorizontalTranslationEnabled="false"

        如果要设置icon距离上边距的距离，也可以通过重新定义R.dimen.design_bottom_navigation_margin来实现，
        也就是
        <dimen name="design_bottom_navigation_margin">12dp</dimen>
        */

        //使用了上面两种方法，这个就不用了
//        removeShiftMode(navigation);
        return null;
    }

    @SuppressLint("RestrictedApi")
    static void removeShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
            //noinspection RestrictedApi
            item.setShifting(false);//Android8.0以前不是这个方法，而且要用反射
            item.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            // set once again checked value, so view will be updated
            //noinspection RestrictedApi
            item.setChecked(item.getItemData().isChecked());
        }
    }
}
