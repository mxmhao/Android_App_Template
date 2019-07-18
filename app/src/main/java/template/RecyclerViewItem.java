package template;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * 给RecyclerView的Item添加上下文菜单，和点击事件
 */
public class RecyclerViewItem extends RecyclerView.Adapter<MyViewHolder> implements View.OnCreateContextMenuListener, View.OnClickListener {

    ArrayList<String> datas;

    //已选中的位置，划重点
    int selectedPosition = RecyclerView.NO_POSITION;
    //保存映射，划重点
    WeakHashMap<View, MyViewHolder> item2Holder = new WeakHashMap<>(10);

    ItemInterface itemInterface;//用于传递Item的点击事件

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        view.setOnCreateContextMenuListener(this);//划重点，上下文菜单
        view.setOnClickListener(this);//划重点，点击事件
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //先做好映射
        item2Holder.put(holder.itemView, holder);//划重点
        //再绑定数据
    }

    @Override
    public int getItemCount() {
        return null == datas? 0 : datas.size();
    }

    /*
    在Activity 或者Fragment中重写上下文菜单点击方法
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == ContextMenu.FIRST
                && RecyclerView.NO_POSITION != adapter.selectedPosition) {
            deleteData();
            adapter.selectedPosition = RecyclerView.NO_POSITION;//清除位置
        }
        return super.onContextItemSelected(item);
    }
    */
    //实现View.OnCreateContextMenuListener
    @Override//上下文菜单
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        selectedPosition = item2Holder.get(v).getAdapterPosition();//划重点
        menu.add(ContextMenu.NONE, ContextMenu.FIRST, ContextMenu.NONE, "删除");
        menu.add(ContextMenu.NONE, ContextMenu.FIRST + 1, ContextMenu.NONE, "收藏");
    }

    private long lastClick;//防止重复点击，应该有更优雅的方式
    //View.OnClickListener
    @Override//item点击实践
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastClick < 800) return;//0.8秒内防止重复点击
        itemInterface.onItemClick(item2Holder.get(v).getAdapterPosition());//划重点
        lastClick = SystemClock.elapsedRealtime();
    }

    public static interface ItemInterface {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView tv1,
            tv2,
            tv3;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
//        tv1 = itemView.findViewById(R.id.tv1);
//        tv2 = itemView.findViewById(R.id.tv2);
//        tv3 = itemView.findViewById(R.id.tv3);
    }
}
