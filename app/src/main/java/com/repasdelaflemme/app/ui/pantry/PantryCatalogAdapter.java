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
    public interface OnFavoriteToggle { void onFav(Ingredient ing, boolean favorite); }

    private final List<Ingredient> full = new ArrayList<>();
    private final List<Ingredient> visible = new ArrayList<>();
    private final Set<String> selected = new HashSet<>();
    private final OnToggle onToggle;
    private OnFavoriteToggle onFavoriteToggle;
    private final Set<String> favorites = new HashSet<>();
    private final Set<String> categoryFilters = new HashSet<>();
    private String queryText = "";

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

    public void setFavorites(Set<String> favs) {
        favorites.clear();
        if (favs != null) favorites.addAll(favs);
        notifyDataSetChanged();
    }

    public void setOnFavoriteToggle(OnFavoriteToggle cb) { this.onFavoriteToggle = cb; }

    public void filter(String query) {
        queryText = (query != null) ? query.trim().toLowerCase() : "";
        applyFilters();
    }

    public void setCategoryFilters(Set<String> cats) {
        categoryFilters.clear();
        if (cats != null) categoryFilters.addAll(cats);
        applyFilters();
    }

    private void applyFilters() {
        visible.clear();
        for (Ingredient i : full) {
            boolean matchQuery = queryText.isEmpty() || ((i.name != null ? i.name : "").toLowerCase().contains(queryText));
            boolean matchCat = categoryFilters.isEmpty() || (i.category != null && categoryFilters.contains(i.category));
            if (matchQuery && matchCat) visible.add(i);
        }
        notifyDataSetChanged();
    }

    public void selectVisible() {
        for (Ingredient i : visible) {
            if (i != null && i.id != null && !selected.contains(i.id)) {
                selected.add(i.id);
                if (onToggle != null) onToggle.onToggle(i, true);
            }
        }
        notifyDataSetChanged();
    }

    public void clearVisible() {
        for (Ingredient i : visible) {
            if (i != null && i.id != null && selected.contains(i.id)) {
                selected.remove(i.id);
                if (onToggle != null) onToggle.onToggle(i, false);
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
        boolean isFav = favorites.contains(ing.id);
        h.fav.setText(isFav ? "★" : "☆");
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
        h.fav.setOnClickListener(v -> {
            boolean now = !favorites.contains(ing.id);
            if (now) favorites.add(ing.id); else favorites.remove(ing.id);
            h.fav.setText(now ? "★" : "☆");
            if (onFavoriteToggle != null) onFavoriteToggle.onFav(ing, now);
        });
    }

    @Override
    public int getItemCount() { return visible.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle; CheckBox check; TextView fav;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            check = itemView.findViewById(R.id.check);
            fav = itemView.findViewById(R.id.favToggle);
        }
    }
}
