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
            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.cover.getContext())
                        .load(item.imageUrl)
                        .placeholder(R.drawable.logo_app)
                        .centerCrop()
                        .into(holder.cover);
            } else if (item.imageResId != null && item.imageResId != 0) {
                holder.cover.setImageResource(item.imageResId);
                holder.cover.setColorFilter(null);
            } else {
                holder.cover.setImageResource(R.drawable.logo_app);
                holder.cover.setColorFilter(null);
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
                chip2.setChipBackgroundColorResource(R.color.cr_surface_variant);
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
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(item);
        });

        // Keyframe-like slideIn animation (CSS-style)
        AnimUtils.slideInKeyframes(holder.itemView, position * 20L);
    }

    @Override
    public int getItemCount() {
        return data.size();
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
}
