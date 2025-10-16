package com.repasdelaflemme.app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;

public class SkeletonAdapter extends RecyclerView.Adapter<SkeletonAdapter.VH> {
    private final int count;
    private final int layoutResId;
    public SkeletonAdapter(int count) { this(count, R.layout.item_recipe_skeleton); }
    public SkeletonAdapter(int count, int layoutResId) {
        this.count = Math.max(4, count);
        this.layoutResId = layoutResId;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        // Apply shimmer on all leaf views
        applyShimmerRecursive(holder.itemView);
    }

    @Override public int getItemCount() { return count; }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) { super(itemView); }
    }

    private void applyShimmerRecursive(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) applyShimmerRecursive(vg.getChildAt(i));
        } else {
            // Put shimmer in foreground to keep base skeleton background visible
            try {
                com.repasdelaflemme.app.ui.common.ShimmerDrawable sh = new com.repasdelaflemme.app.ui.common.ShimmerDrawable();
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    v.setForeground(sh);
                    v.post(() -> sh.start(v));
                } else {
                    // Fallback: set as background overlay (less ideal)
                    v.setBackground(sh);
                    v.post(() -> sh.start(v));
                }
            } catch (Exception ignored) {}
        }
    }
}
