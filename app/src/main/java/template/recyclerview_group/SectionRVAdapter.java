package template.recyclerview_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SectionRVAdapter extends RecyclerView.Adapter<SectionRVAdapter.ViewHolder> {
    public int count = 0;
    public int sectionFirstItemPosition[];

    private final ArrayList<ArrayList<String>> mValues;
    private final OnItemInteractionListener mListener;

    public SectionRVAdapter(ArrayList<ArrayList<String>> items, OnItemInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public final void onBindViewHolder(final ViewHolder holder, int position) {
        if (null != sectionFirstItemPosition && position >= sectionFirstItemPosition[0]) {//有多个分组
            int groupFirst;
            for (int i = sectionFirstItemPosition.length - 1; i > -1; --i) {//倒序循环
                groupFirst = sectionFirstItemPosition[i];//从最大的值开始
                if (position >= groupFirst) {
                    onBindViewHolder(holder, i+1, position - groupFirst);
                    return;
                }
            }
        } else {//第一组
            onBindViewHolder(holder, 0, position);
        }
    }

    public void onBindViewHolder(final ViewHolder holder, int section, int row) {
        String mItem = mValues.get(section).get(row);
        holder.section = section;
        holder.row = row;
    }

    @Override
    public int getItemCount() {
        return count > 0? count : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView tvTitle;
        public int section;
        public int row;
        public Object data;
        private OnItemInteractionListener mListener;

        public ViewHolder(View view, OnItemInteractionListener listener) {
            super(view);
//            tvTitle = view.findViewById(R.id.)
            mListener = listener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            getAdapterPosition()
            if (null != mListener) {
                mListener.OnItemClick(section, row, data);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (null != mListener) {
                mListener.OnItemLongClick(section, row, data);
            }
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvTitle.getText() + "'";
        }
    }

    public interface OnItemInteractionListener {
        void OnItemClick(int section, int row, Object data);
        void OnItemLongClick(int section, int row, Object data);
    }
}
