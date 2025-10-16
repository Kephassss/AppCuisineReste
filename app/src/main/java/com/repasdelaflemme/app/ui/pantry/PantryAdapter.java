package com.repasdelaflemme.app.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.model.Ingredient;
import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface OnRemove { void onRemove(Ingredient ing); }

    private final List<Ingredient> data = new ArrayList<>();
    private final OnRemove onRemove;

    public PantryAdapter(OnRemove onRemove) { this.onRemove = onRemove; }

    public void submit(List<Ingredient> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        com.repasdelaflemme.app.ui.common.AnimUtils.attachPressAnimator(v);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Ingredient ing = data.get(position);
        holder.title.setText(ing.name);
        holder.subtitle.setText(ing.category);
        holder.btnRemove.setOnClickListener(v -> {
            if (onRemove != null) onRemove.onRemove(ing);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle; ImageButton btnRemove;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
