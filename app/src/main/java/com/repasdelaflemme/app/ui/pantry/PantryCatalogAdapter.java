package com.repasdelaflemme.app.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.model.Ingredient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PantryCatalogAdapter extends RecyclerView.Adapter<PantryCatalogAdapter.VH> {

    public interface OnToggle { void onToggle(Ingredient ing, boolean selected); }

    private final List<Ingredient> full = new ArrayList<>();
    private final List<Ingredient> visible = new ArrayList<>();
    private final Set<String> selected = new HashSet<>();
    private final OnToggle onToggle;

    public PantryCatalogAdapter(OnToggle onToggle) {
        this.onToggle = onToggle;
    }

    public void setCatalog(List<Ingredient> items) {
        full.clear(); visible.clear();
        if (items != null) { full.addAll(items); visible.addAll(items); }
        notifyDataSetChanged();
    }

    public void setSelectedIds(List<String> ids) {
        selected.clear();
        if (ids != null) selected.addAll(ids);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        visible.clear();
        if (query == null || query.trim().isEmpty()) {
            visible.addAll(full);
        } else {
            String q = query.toLowerCase();
            for (Ingredient i : full) {
                String hay = (i.name != null ? i.name : "").toLowerCase();
                if (hay.contains(q)) visible.add(i);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ingredient ing = visible.get(position);
        h.title.setText(ing.name);
        h.subtitle.setText(ing.category);
        boolean isSel = selected.contains(ing.id);
        h.check.setChecked(isSel);
        h.itemView.setOnClickListener(v -> {
            boolean now = !h.check.isChecked();
            h.check.setChecked(now);
            if (now) selected.add(ing.id); else selected.remove(ing.id);
            if (onToggle != null) onToggle.onToggle(ing, now);
        });
        h.check.setOnClickListener(v -> {
            boolean now = h.check.isChecked();
            if (now) selected.add(ing.id); else selected.remove(ing.id);
            if (onToggle != null) onToggle.onToggle(ing, now);
        });
    }

    @Override
    public int getItemCount() { return visible.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle; CheckBox check;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            check = itemView.findViewById(R.id.check);
        }
    }
}

