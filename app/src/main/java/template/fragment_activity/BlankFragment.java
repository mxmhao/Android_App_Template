package template.fragment_activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


public class BlankFragment extends Fragment {
    private final String TAG = "BlankFragment";

    public static final String PARAM1 = "PARAM1";

    public BlankFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //获取传递的参数，根据传递的方式，对应下面2种方式获取
        Log.e(TAG, "onAttach: " + ((Activity)context).getIntent().getStringExtra(PARAM1));
//        Log.e(TAG, "onAttach: " + getArguments().getString(PARAM1));//
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.simple_list_item_1, container, false);

        return view;
    }

    public void push() {//在当前activity跳转到另一个Fragment
        getFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new BlankFragment())
                .addToBackStack(null)
                .commit();
    }

    //返回
    public void pop() {
        getFragmentManager().popBackStack();
    }

    public void popToRoot() {
        //直接推出全部的返回栈
//        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getActivity().finish();//或者直接关掉activity
    }

    @Override
    public void onDestroy() {//保存数据
        Log.e(TAG, "onDestroy: " + num);
        num++;
        super.onDestroy();
    }

    static int num = 0;
}
