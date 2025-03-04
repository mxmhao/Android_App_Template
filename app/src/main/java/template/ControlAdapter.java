package template;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ViewPager2 使用的 Adapter
public class ControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public View[] views;

    public ControlAdapter(View[] views) {
        this.views = views;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(views[viewType]);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return null == views ? 0 : views.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
