package com.repasdelaflemme.app.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.AnimUtils;

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

        Recipe loaded = AssetsRepository.findRecipeById(requireContext(), recipeId);
        if (loaded == null) {
            Toast.makeText(getContext(), getString(R.string.error_loading_recipes), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean initialIsLight = recipeId.endsWith("_light");
        String baseId = initialIsLight ? recipeId.substring(0, recipeId.length() - 6) : recipeId;
        final Recipe baseResolved = (initialIsLight ? AssetsRepository.findRecipeById(requireContext(), baseId) : loaded);
        final Recipe baseFinal = (baseResolved != null) ? baseResolved : loaded;
        final Recipe light = AssetsRepository.findRecipeById(requireContext(), baseId + "_light");
        final Recipe[] current = new Recipe[]{ initialIsLight && light != null ? light : baseFinal };

        ImageView photo = view.findViewById(R.id.imgPhoto);
        TextView title = view.findViewById(R.id.txtTitle);
        TextView timeServ = view.findViewById(R.id.txtTimeServings);
        TextView ings = view.findViewById(R.id.txtIngredients);
        ViewGroup stepsContainer = view.findViewById(R.id.stepsContainer);
        MaterialButton btnStart = view.findViewById(R.id.btnStartRecipe);
        ChipGroup chipsMeta = view.findViewById(R.id.chipsMeta);
        com.google.android.material.switchmaterial.SwitchMaterial switchLight = view.findViewById(R.id.switchLight);

        AnimUtils.attachPressAnimator(btnStart);

        // Bind header
        title.setText(current[0].title);
        bindImage(photo, current[0].image);
        String meta = getString(R.string.minutes_short, current[0].minutes)
                + (current[0].servings != null ? " - " + getString(R.string.servings_short, current[0].servings) : "");
        timeServ.setText(meta);

        try {
            AnimUtils.fadeInKeyframes(title, 280L, 0L);
            AnimUtils.fadeInKeyframes(timeServ, 320L, 40L);
            AnimUtils.slideInKeyframes(btnStart, 60L);
        } catch (Throwable ignored) {}

        // Chips meta
        if (chipsMeta != null) {
            chipsMeta.removeAllViews();
            // Time chip
            Chip chipTime = new Chip(requireContext());
            chipTime.setText(getString(R.string.minutes_short, current[0].minutes)); chipTime.setCheckable(false);
            chipTime.setChipBackgroundColorResource(R.color.cr_surface_variant);
            chipTime.setTextColor(getResources().getColor(R.color.cr_on_surface));
            chipsMeta.addView(chipTime);
            // Servings chip (if any)
            if (current[0].servings != null) {
                Chip chipServ = new Chip(requireContext());
                chipServ.setText(getString(R.string.servings_short, current[0].servings)); chipServ.setCheckable(false);
                chipServ.setChipBackgroundColorResource(R.color.cr_surface_variant);
                chipServ.setTextColor(getResources().getColor(R.color.cr_on_surface));
                chipsMeta.addView(chipServ);
            }
            if (current[0].minutes <= 20) {
                Chip c = new Chip(requireContext());
                c.setText(getString(R.string.filter_quick)); c.setCheckable(false);
                c.setChipBackgroundColorResource(R.color.cr_primary_container);
                c.setTextColor(getResources().getColor(R.color.cr_primary));
                chipsMeta.addView(c);
            }
            if (Boolean.TRUE.equals(current[0].vegetarian)) {
                Chip c = new Chip(requireContext());
                c.setText(getString(R.string.filter_veg)); c.setCheckable(false);
                c.setChipBackgroundColorResource(R.color.cr_secondary);
                c.setTextColor(getResources().getColor(android.R.color.white));
                chipsMeta.addView(c);
            }
        }

        // Ingredients
        PrefPantryStore store = new PrefPantryStore(requireContext());
        renderIngredients2(current[0], ings, store);

        // Steps
        if (stepsContainer != null) {
            stepsContainer.removeAllViews();
            if (current[0].steps != null) {
                LayoutInflater infl = LayoutInflater.from(requireContext());
                for (int i = 0; i < current[0].steps.size(); i++) {
                    View row = infl.inflate(R.layout.item_step, stepsContainer, false);
                    TextView num = row.findViewById(R.id.stepNumber);
                    TextView txt = row.findViewById(R.id.stepText);
                    num.setText(String.valueOf(i + 1));
                    txt.setText(current[0].steps.get(i));
                    stepsContainer.addView(row);
                }
            }
        }

        // Start cooking
        btnStart.setOnClickListener(v -> {
            try {
                NavController nav = NavHostFragment.findNavController(this);
                Bundle args = new Bundle();
                args.putString("recipeId", current[0].id);
                nav.navigate(R.id.cookingFragment, args);
            } catch (Throwable ignored) {}
        });

        // Light switch if available
        if (switchLight != null) {
            if (light != null) {
                switchLight.setChecked(initialIsLight);
                switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Recipe target = isChecked ? (light != null ? light : baseFinal) : baseFinal;
                    current[0] = target;
                    title.setText(target.title);
                    timeServ.setText(getString(R.string.minutes_short, target.minutes)
                            + (target.servings != null ? " - " + getString(R.string.servings_short, target.servings) : ""));
                    bindImage(photo, target.image);
                    // chips
                    if (chipsMeta != null) {
                        chipsMeta.removeAllViews();
                        Chip chipTime2 = new Chip(requireContext());
                        chipTime2.setText(getString(R.string.minutes_short, target.minutes)); chipTime2.setCheckable(false);
                        chipTime2.setChipBackgroundColorResource(R.color.cr_surface_variant);
                        chipTime2.setTextColor(getResources().getColor(R.color.cr_on_surface));
                        chipsMeta.addView(chipTime2);
                        if (target.servings != null) {
                            Chip chipServ2 = new Chip(requireContext());
                            chipServ2.setText(getString(R.string.servings_short, target.servings)); chipServ2.setCheckable(false);
                            chipServ2.setChipBackgroundColorResource(R.color.cr_surface_variant);
                            chipServ2.setTextColor(getResources().getColor(R.color.cr_on_surface));
                            chipsMeta.addView(chipServ2);
                        }
                        if (target.minutes <= 20) {
                            Chip c = new Chip(requireContext());
                            c.setText(getString(R.string.filter_quick)); c.setCheckable(false);
                            c.setChipBackgroundColorResource(R.color.cr_primary_container);
                            c.setTextColor(getResources().getColor(R.color.cr_primary));
                            chipsMeta.addView(c);
                        }
                        if (Boolean.TRUE.equals(target.vegetarian)) {
                            Chip c = new Chip(requireContext());
                            c.setText(getString(R.string.filter_veg)); c.setCheckable(false);
                            c.setChipBackgroundColorResource(R.color.cr_secondary);
                            c.setTextColor(getResources().getColor(android.R.color.white));
                            chipsMeta.addView(c);
                        }
                    }
                    // Re-render ingredients & steps
                    renderIngredients2(target, ings, store);
                    if (stepsContainer != null) {
                        stepsContainer.removeAllViews();
                        if (target.steps != null) {
                            LayoutInflater infl2 = LayoutInflater.from(requireContext());
                            for (int i = 0; i < target.steps.size(); i++) {
                                View row = infl2.inflate(R.layout.item_step, stepsContainer, false);
                                TextView num = row.findViewById(R.id.stepNumber);
                                TextView txt = row.findViewById(R.id.stepText);
                                num.setText(String.valueOf(i + 1));
                                txt.setText(target.steps.get(i));
                                stepsContainer.addView(row);
                            }
                        }
                    }
                });
            } else {
                switchLight.setVisibility(View.GONE);
            }
        }

        if (skeleton != null) skeleton.setVisibility(View.GONE);
    }

    private static void bindImage(ImageView photo, String image) {
        if (photo == null) return;
        try { photo.setImageTintList(null); } catch (Throwable ignored) {}
        try { photo.setColorFilter(null); } catch (Throwable ignored) {}
        if (image != null) {
            if (image.startsWith("http")) {
                Glide.with(photo.getContext()).load(image)
                        .placeholder(R.drawable.ic_recipe).error(R.drawable.ic_recipe)
                        .centerCrop().into(photo);
            } else if (image.startsWith("res:")) {
                String resName = image.substring(4);
                int resId = photo.getResources().getIdentifier(resName, "drawable", photo.getContext().getPackageName());
                if (resId != 0) Glide.with(photo.getContext()).load(resId).centerCrop().into(photo);
                else photo.setImageResource(R.drawable.ic_recipe);
            } else {
                photo.setImageResource(R.drawable.ic_recipe);
            }
        } else {
            photo.setImageResource(R.drawable.ic_recipe);
        }
    }

    // Render ingredients with human-friendly units and quantities
    private List<String> renderIngredients2(Recipe r, TextView ings, PrefPantryStore store) {
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
                String unit = mapUnit(ri.unit, ri.qty);
                boolean ok = have.contains(ri.id);
                sb.append(ok ? "* " : "o ")
                  .append(displayName)
                  .append(" - ")
                  .append(formatQty(ri.qty)).append(" ").append(unit);
                if (!ok) missingIds.add(ri.id);
                if (i < r.ingredients.size() - 1) sb.append("\n");
            }
        }
        ings.setText(sb.toString());
        return missingIds;
    }

    private static String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-6) return String.valueOf((int) Math.round(q));
        return (q == (long) q) ? String.format(java.util.Locale.ROOT, "%d", (long) q)
                : String.format(java.util.Locale.ROOT, "%.2f", q).replaceAll("0+$", "").replaceAll("[.]$", "");
    }

    private static String mapUnit(String unit, double qty) {
        if (unit == null) return "";
        String u = unit.trim().toLowerCase(java.util.Locale.ROOT);
        switch (u) {
            case "tbsp": return "c. à soupe";
            case "tsp": return "c. à café";
            case "pcs": return qty > 1 ? "pièces" : "pièce";
            case "ml": return "ml";
            case "g": return "g";
            case "cube": return qty > 1 ? "cubes" : "cube";
            case "sachet": return qty > 1 ? "sachets" : "sachet";
            case "tranche": return qty > 1 ? "tranches" : "tranche";
            default: return unit;
        }
    }
}
