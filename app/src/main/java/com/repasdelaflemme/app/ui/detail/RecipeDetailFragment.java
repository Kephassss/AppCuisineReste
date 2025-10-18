package com.repasdelaflemme.app.ui.detail;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.AnimUtils;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View skeleton = view.findViewById(R.id.detailSkeleton);
        if (skeleton != null) skeleton.setVisibility(View.VISIBLE);
        String recipeId = getArguments() != null ? getArguments().getString("recipeId") : null;
        if (recipeId == null) {
            Toast.makeText(getContext(), "ID recette manquant", Toast.LENGTH_SHORT).show();
            return;
        }
        Recipe r = AssetsRepository.findRecipeById(requireContext(), recipeId);
        if (r == null) {
            Toast.makeText(getContext(), getString(R.string.error_loading_recipes), Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView photo = view.findViewById(R.id.imgPhoto);
        try { photo.setImageTintList(null); } catch (Throwable ignored) {}
        try { photo.setColorFilter(null); } catch (Throwable ignored) {}
        TextView title = view.findViewById(R.id.txtTitle);
        TextView timeServ = view.findViewById(R.id.txtTimeServings);
        TextView ings = view.findViewById(R.id.txtIngredients);
        ViewGroup stepsContainer = view.findViewById(R.id.stepsContainer);
        MaterialButton start = view.findViewById(R.id.btnStartRecipe);
        AnimUtils.attachPressAnimator(start);
        ChipGroup chipsMeta = view.findViewById(R.id.chipsMeta);

        title.setText(r.title);
        if (r.image != null) {
            if (r.image.startsWith("http")) {
                Glide.with(photo.getContext()).load(r.image).placeholder(R.drawable.ic_recipe).error(R.drawable.ic_recipe).centerCrop().into(photo);
            } else if (r.image.startsWith("res:")) {
                String resName = r.image.substring(4);
                int resId = getResources().getIdentifier(resName, "drawable", requireContext().getPackageName());
                if (resId != 0) Glide.with(photo.getContext()).load(resId).centerCrop().into(photo);
                else photo.setImageResource(R.drawable.ic_recipe);
            } else {
                photo.setImageResource(R.drawable.ic_recipe);
            }
        } else {
            photo.setImageResource(R.drawable.ic_recipe);
        }

        String meta = getString(R.string.minutes_short, r.minutes) + (r.servings != null ? " • " + getString(R.string.servings_short, r.servings) : "");
        timeServ.setText(meta);

        if (chipsMeta != null) {
            chipsMeta.removeAllViews();
            if (r.minutes <= 20) {
                Chip c = new Chip(requireContext());
                c.setText(getString(R.string.filter_quick)); c.setCheckable(false);
                c.setChipBackgroundColorResource(R.color.cr_primary_container);
                c.setTextColor(getResources().getColor(R.color.cr_primary));
                chipsMeta.addView(c);
            }
            if (Boolean.TRUE.equals(r.vegetarian)) {
                Chip c = new Chip(requireContext());
                c.setText(getString(R.string.filter_veg)); c.setCheckable(false);
                c.setChipBackgroundColorResource(R.color.cr_secondary);
                c.setTextColor(getResources().getColor(android.R.color.white));
                chipsMeta.addView(c);
            }
        }

        PrefPantryStore store = new PrefPantryStore(requireContext());
        List<String> missingIds = renderIngredients(r, ings, store);

        if (stepsContainer != null) {
            stepsContainer.removeAllViews();
            if (r.steps != null) {
                LayoutInflater infl = LayoutInflater.from(requireContext());
                for (int i = 0; i < r.steps.size(); i++) {
                    View row = infl.inflate(R.layout.item_step, stepsContainer, false);
                    TextView num = row.findViewById(R.id.stepNumber);
                    TextView txt = row.findViewById(R.id.stepText);
                    num.setText(String.valueOf(i + 1));
                    txt.setText(r.steps.get(i));
                    stepsContainer.addView(row);
                    AnimUtils.fadeInKeyframes(row, 400, i * 40L);
                }
            }
        }

        final long dur = 500L;
        AnimUtils.fadeInEaseIn(photo, dur, 0);
        AnimUtils.fadeInEaseIn(title, dur, 50);
        AnimUtils.fadeInEaseIn(timeServ, dur, 100);
        AnimUtils.fadeInEaseIn(ings, dur, 150);
        AnimUtils.fadeInEaseIn(start, dur, 250);

        start.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            androidx.navigation.NavController nav = androidx.navigation.fragment.NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", r.id);
            nav.navigate(R.id.cookingFragment, args);
        });

        View btnAddMissing = view.findViewById(R.id.btnAddMissingToShopping);
        if (btnAddMissing != null) {
            btnAddMissing.setOnClickListener(v -> {
                List<String> current = store.getIngredientIds();
                java.util.Set<String> set = new java.util.HashSet<>(current);
                set.addAll(missingIds);
                store.setIngredientIds(new ArrayList<>(set));
                List<String> newMissing = renderIngredients(r, ings, store);
                Toast.makeText(getContext(), newMissing.isEmpty() ? "Tous les ingrédients sont disponibles" : "Ingrédients ajoutés", Toast.LENGTH_SHORT).show();
            });
        }
        try { if (skeleton != null) skeleton.setVisibility(View.GONE); } catch (Exception ignored) {}
    }

    private List<String> renderIngredients(Recipe r, TextView ings, PrefPantryStore store) {
        Map<String, String> nameMap = new HashMap<>();
        for (com.repasdelaflemme.app.data.model.Ingredient ing : AssetsRepository.getIngredients(requireContext())) {
            nameMap.put(ing.id, ing.name);
        }
        List<String> missingIds = new ArrayList<>();
        List<String> have = store.getIngredientIds();
        StringBuilder sb = new StringBuilder();
        if (r.ingredients != null) {
            for (int i = 0; i < r.ingredients.size(); i++) {
                RecipeIngredient ri = r.ingredients.get(i);
                String displayName = nameMap.getOrDefault(ri.id, ri.id);
                String unit = ri.unit != null ? ri.unit : "";
                boolean ok = have.contains(ri.id);
                sb.append(ok ? "• " : "○ ")
                  .append(displayName)
                  .append(" — ")
                  .append(ri.qty).append(" ").append(unit);
                if (!ok) missingIds.add(ri.id);
                if (i < r.ingredients.size() - 1) sb.append("\n");
            }
        }
        ings.setText(sb.toString());
        return missingIds;
    }
}
