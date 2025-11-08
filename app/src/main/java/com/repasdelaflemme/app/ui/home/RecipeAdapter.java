package com.repasdelaflemme.app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import com.repasdelaflemme.app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import com.repasdelaflemme.app.ui.common.AnimUtils;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    public interface OnClick {
        void onRecipeClick(RecipeCard item);
    }

    private final List<RecipeCard> data = new ArrayList<>();
    private final OnClick listener;

    public RecipeAdapter() { this(null); }

    public RecipeAdapter(OnClick listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submit(List<RecipeCard> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        AnimUtils.attachPressAnimator(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RecipeCard item = data.get(position);
        holder.title.setText(item.title);
        int mins = item.minutes;
        if (item.matchScore != null) {
            holder.subtitle.setText(mins + " min • " + item.matchScore + "% dispo");
        } else {
            holder.subtitle.setText(mins + " min");
        }
        if (holder.tagTime != null) {
            holder.tagTime.setText(mins + " min");
        }
        if (holder.cover != null) {
            // Always clear any tint/filter that may be set from XML
            try { holder.cover.setImageTintList(null); } catch (Throwable ignored) {}
            try { holder.cover.setColorFilter(null); } catch (Throwable ignored) {}

            // Cancel any previous pending Glide request on this recycled ImageView
            try { com.bumptech.glide.Glide.with(holder.cover.getContext()).clear(holder.cover); } catch (Throwable ignored) {}

            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.cover.getContext())
                        .load(item.imageUrl)
                        .placeholder(R.drawable.ic_recipe)
                        .error(R.drawable.ic_recipe)
                        .centerCrop()
                        .into(holder.cover);
            } else if (item.imageResId != null && item.imageResId != 0) {
                com.bumptech.glide.Glide.with(holder.cover.getContext())
                        .load(item.imageResId)
                        .placeholder(R.drawable.ic_recipe)
                        .error(R.drawable.ic_recipe)
                        .centerCrop()
                        .into(holder.cover);
            } else {
                // Fallback placeholder
                holder.cover.setImageResource(R.drawable.ic_recipe);
            }
        }
        if (holder.chips != null) {
            holder.chips.removeAllViews();
            if (mins <= 20) {
                Chip chip = new Chip(holder.itemView.getContext());
                chip.setText(holder.itemView.getContext().getString(R.string.filter_quick));
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.cr_primary_container);
                chip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cr_primary));
                holder.chips.addView(chip);
            }
            if (item.matchScore != null) {
                Chip chip2 = new Chip(holder.itemView.getContext());
                chip2.setText(item.matchScore + "% dispo");
                chip2.setClickable(false);
                int s = item.matchScore;
                if (s < 50) {
                    chip2.setChipBackgroundColorResource(R.color.cr_accent_red);
                    chip2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                } else if (s < 80) {
                    chip2.setChipBackgroundColorResource(R.color.cr_accent_orange);
                    chip2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                } else {
                    chip2.setChipBackgroundColorResource(R.color.cr_secondary);
                    chip2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                }
                holder.chips.addView(chip2);
            }
            if (item.missingCount != null && item.missingCount > 0) {
                Chip chip3 = new Chip(holder.itemView.getContext());
                chip3.setText("-" + item.missingCount + " manq.");
                chip3.setClickable(false);
                chip3.setChipBackgroundColorResource(R.color.cr_accent_orange);
                chip3.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                holder.chips.addView(chip3);
            }
            if (item.vegetarian != null && item.vegetarian) {
                Chip chip4 = new Chip(holder.itemView.getContext());
                chip4.setText(holder.itemView.getContext().getString(R.string.filter_veg));
                chip4.setClickable(false);
                chip4.setChipBackgroundColorResource(R.color.cr_secondary);
                chip4.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                holder.chips.addView(chip4);
            }
            if (item.halal != null && item.halal) {
                Chip c = new Chip(holder.itemView.getContext());
                c.setText(holder.itemView.getContext().getString(R.string.filter_halal));
                c.setClickable(false);
                c.setChipBackgroundColorResource(R.color.cr_secondary);
                c.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
                holder.chips.addView(c);
            }
            if (item.budget != null && item.budget > 0) {
                String label = item.budget == 1 ? "€" : (item.budget == 2 ? "€€" : "€€€");
                Chip c = new Chip(holder.itemView.getContext());
                c.setText(label);
                c.setClickable(false);
                c.setChipBackgroundColorResource(R.color.cr_surface_variant);
                holder.chips.addView(c);
            }
            if (item.cuisine != null && !item.cuisine.isEmpty()) {
                Chip c = new Chip(holder.itemView.getContext());
                c.setText(capFirst(item.cuisine));
                c.setClickable(false);
                c.setChipBackgroundColorResource(R.color.cr_surface_variant);
                holder.chips.addView(c);
            }
            if (item.utensils != null && !item.utensils.isEmpty()) {
                int count = Math.min(2, item.utensils.size());
                for (int i = 0; i < count; i++) {
                    String u = item.utensils.get(i);
                    Chip c = new Chip(holder.itemView.getContext());
                    c.setText(mapUstensilLabel(u));
                    c.setClickable(false);
                    c.setChipBackgroundColorResource(R.color.cr_surface_variant);
                    holder.chips.addView(c);
                }
            }
        }

        // Tag the bound view with the exact recipeId displayed
        try { holder.itemView.setTag(R.id.tag_recipe_id, item.id); } catch (Throwable ignored) {}

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                String tagId = null;
                try { Object t = holder.itemView.getTag(R.id.tag_recipe_id); tagId = (t instanceof String) ? (String) t : null; } catch (Throwable ignored) {}
                if (tagId != null) {
                    // Find the matching card by id to avoid any position drift
                    RecipeCard found = null;
                    for (RecipeCard rc : data) {
                        if (rc != null && tagId.equals(rc.id)) { found = rc; break; }
                    }
                    if (found != null) { listener.onRecipeClick(found); return; }
                }
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < data.size()) {
                    listener.onRecipeClick(data.get(pos));
                }
            }
        });

        // Keyframe-like slideIn animation (CSS-style)
        AnimUtils.slideInKeyframes(holder.itemView, position * 20L);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        try {
            RecipeCard rc = data.get(position);
            if (rc != null && rc.id != null) {
                return (long) rc.id.hashCode();
            }
        } catch (Throwable ignored) {}
        return RecyclerView.NO_ID;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle, tagTime;
        ChipGroup chips;
        android.widget.ImageView cover;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            tagTime = itemView.findViewById(R.id.tagTime);
            chips = itemView.findViewById(R.id.chips);
            cover = itemView.findViewById(R.id.cover);
        }
    }

    private static String capFirst(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private static String mapUstensilLabel(String u) {
        if (u == null) return "";
        switch (u.toLowerCase()) {
            case "poele": return "Poêle";
            case "casserole": return "Casserole";
            case "four": return "Four";
            case "wok": return "Wok";
            case "cocotte": return "Cocotte";
            case "mixeur": return "Mixeur";
            default: return capFirst(u);
        }
    }
}
