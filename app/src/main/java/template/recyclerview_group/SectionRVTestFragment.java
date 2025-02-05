package template.recyclerview_group;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;

public class SectionRVTestFragment extends Fragment implements SectionRVAdapter.OnItemInteractionListener {

    private Context context;
    private String titles[];
    private ArrayList<ArrayList<String>> tasks;
    private ArrayList<String> loadingTasks;
    private ArrayList<String> doneTasks;
    private ArrayList<String> errorTasks;

    private SectionItemDecoration titleDecoration;
    private SectionRVAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasks = new ArrayList<>(3);
        loadingTasks = new ArrayList<>(3);
        doneTasks = new ArrayList<>(3);
        errorTasks = new ArrayList<>(3);
//        titles = context.getResources().getStringArray(R.array.permission);
        titles = new String[]{"正在下载", "下载完成", "下载失败"};

        loadingTasks.add("task0-1");
        loadingTasks.add("task0-2");
        loadingTasks.add("task0-3");
        loadingTasks.add("task0-4");
        loadingTasks.add("task0-5");
        loadingTasks.add("task0-6");
        loadingTasks.add("task0-7");
        loadingTasks.add("task0-8");
        loadingTasks.add("task0-9");
        loadingTasks.add("task0-10");
        loadingTasks.add("task0-11");
        loadingTasks.add("task0-12");

        doneTasks.add("task1-1");
        doneTasks.add("task1-2");
        doneTasks.add("task1-3");
        doneTasks.add("task1-4");
        doneTasks.add("task1-5");
        doneTasks.add("task1-6");
        doneTasks.add("task1-7");
        doneTasks.add("task1-8");
        doneTasks.add("task1-9");
        doneTasks.add("task1-10");
        doneTasks.add("task1-11");
        doneTasks.add("task1-12");

        errorTasks.add("task2-1");
        errorTasks.add("task2-2");
        errorTasks.add("task2-3");
        errorTasks.add("task2-4");
        errorTasks.add("task2-5");
        errorTasks.add("task2-6");
        errorTasks.add("task2-7");
        errorTasks.add("task2-8");
        errorTasks.add("task2-9");
        errorTasks.add("task2-10");
        errorTasks.add("task2-11");
        errorTasks.add("task2-12");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View view = inflater.inflate(android.R.layout.list_content, container, false);
        View view = new RecyclerView(container.getContext());//这里换成自己的视图
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            adapter = new SectionRVAdapter(tasks, this);
            recyclerView.setAdapter(adapter);

            titleDecoration = new SectionItemDecoration(context);
            titleDecoration.titleDocksAtTheTop = true;
            recyclerView.addItemDecoration(titleDecoration);
            sortGroup();
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    private void sortGroup() {
        tasks.clear();
        //重新排列数据和标题
        LinkedList<String> list = new LinkedList<>();
        if (loadingTasks.size() > 0) {
            tasks.add(loadingTasks);
            list.add(titles[0]);
        }
        if (doneTasks.size() > 0) {
            tasks.add(doneTasks);
            list.add(titles[1]);
        }
        if (errorTasks.size() > 0) {
            tasks.add(errorTasks);
            list.add(titles[2]);
        }

        SectionItemDecoration.setGroup(titleDecoration, adapter, list, tasks);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemClick(int section, int row, Object data) {
        Log.e("OnItemClick", section + "," + row);
    }
    @Override
    public void OnItemLongClick(int section, int row, Object data) {
        Log.e("OnItemLongClick", section + "," + row);
    }
}
