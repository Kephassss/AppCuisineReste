package com.repasdelaflemme.app.ui.pantry;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.ui.common.AnimUtils;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Ingredient;
import android.view.ContextThemeWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.repasdelaflemme.app.ui.home.SkeletonAdapter;

public class PantryFragment extends Fragment {

    private PrefPantryStore store;
    private com.repasdelaflemme.app.data.PrefPantryStats stats;
    private com.repasdelaflemme.app.data.PrefPantryFavorites favorites;
    private final Map<String, Ingredient> ingredientIndex = new HashMap<>();
    private PantryCatalogAdapter catalogAdapter;
    private RecyclerView list;
    private TextView emptyView;
    private ChipGroup chipsGroup;
    private ChipGroup chipsCategories;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new PrefPantryStore(requireContext());
        stats = new com.repasdelaflemme.app.data.PrefPantryStats(requireContext());
        favorites = new com.repasdelaflemme.app.data.PrefPantryFavorites(requireContext());

        List<Ingredient> catalog = AssetsRepository.getIngredients(requireContext());
        ingredientIndex.clear();
        for (Ingredient i : catalog) { ingredientIndex.put(i.id, i); }

        list = view.findViewById(R.id.pantryList);
        chipsGroup = view.findViewById(R.id.chipSelected);
        chipsCategories = view.findViewById(R.id.chipsCategories);
        emptyView = view.findViewById(R.id.emptyView);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        catalogAdapter = new PantryCatalogAdapter((ing, selected) -> {
            if (selected) {
                store.add(ing.id);
                try { stats.bump(ing.id); } catch (Throwable ignored) {}
            } else {
                store.remove(ing.id);
            }
            updateAfterChange();
        });
        catalogAdapter.setFavorites(favorites.get());
        catalogAdapter.setOnFavoriteToggle((ing, fav) -> {
            if (fav) favorites.add(ing.id); else favorites.remove(ing.id);
            renderQuickSections();
        });

        // Show skeleton while we prepare the catalog
        list.setAdapter(new SkeletonAdapter(10, R.layout.item_pantry_skeleton));
        list.postDelayed(() -> list.setAdapter(catalogAdapter), 150);
        list.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));

        // Recherche + catégories
        TextView input = view.findViewById(R.id.inputSearchIngredient);
        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = (s != null ? s.toString() : "");
                catalogAdapter.filter(currentQuery);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        catalogAdapter.setCatalog(catalog);
        catalogAdapter.setSelectedIds(store.getIngredientIds());
        setupCategoryChips(catalog);

        // Action groupée: tout désélectionner sur la liste visible
        View btnClear = view.findViewById(R.id.btnClearVisible);
        if (btnClear != null) btnClear.setOnClickListener(v -> catalogAdapter.clearVisible());
        View btnClearAll = view.findViewById(R.id.btnClearAll);
        if (btnClearAll != null) btnClearAll.setOnClickListener(v -> {
            store.setIngredientIds(new java.util.ArrayList<>());
            updateAfterChange();
        });
        updateAfterChange();
    }

    private void setupCategoryChips(List<Ingredient> catalog) {
        if (chipsCategories == null || catalog == null) return;
        chipsCategories.removeAllViews();
        java.util.Map<String, Integer> catScore = new java.util.HashMap<>();
        for (Ingredient i : catalog) {
            if (i.category == null || i.category.trim().isEmpty()) continue;
            int c = catScore.getOrDefault(i.category, 0);
            c += stats.getCount(i.id);
            catScore.put(i.category, c);
        }
        java.util.List<String> cats = new java.util.ArrayList<>(catScore.keySet());
        cats.sort((a,b) -> Integer.compare(catScore.getOrDefault(b,0), catScore.getOrDefault(a,0)));
        java.util.Set<String> selectedCats = new java.util.HashSet<>();
        for (String c : cats) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(new ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Widget_Material3_Chip_Filter));
            chip.setText(c);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) selectedCats.add(c); else selectedCats.remove(c);
                catalogAdapter.setCategoryFilters(selectedCats);
                catalogAdapter.filter(currentQuery);
            });
            chipsCategories.addView(chip);
        }
    }

    private List<Ingredient> getSelectedIngredients() {
        List<String> ids = store.getIngredientIds();
        List<Ingredient> have = new ArrayList<>();
        for (String id : ids) {
            Ingredient ing = ingredientIndex.get(id);
            if (ing != null) have.add(ing);
        }
        return have;
    }

    private void renderChips(ChipGroup chipGroup, List<Ingredient> have) {
        if (chipGroup == null) return;
        chipGroup.removeAllViews();
        int idx = 0;
        for (Ingredient ing : have) {
            ContextThemeWrapper chipCtx = new ContextThemeWrapper(requireContext(), R.style.Widget_CooknRest_Chip_Ingredient);
            Chip chip = new Chip(chipCtx);
            chip.setText(ing.name);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                store.remove(ing.id);
                updateAfterChange();
            });
            chipGroup.addView(chip);
            AnimUtils.slideInKeyframesX(chip, idx * 20L);
            idx++;
        }
    }

    private void updateAfterChange() {
        // Mettre à jour les chips, la sélection du catalogue et l'état vide
        if (catalogAdapter != null) {
            catalogAdapter.setSelectedIds(store.getIngredientIds());
        }
        renderChips(chipsGroup, getSelectedIngredients());
        try {
            TextView cnt = getView().findViewById(R.id.pantryCount);
            if (cnt != null) cnt.setText(getString(R.string.pantry_selected_count, store.getIngredientIds().size()));
        } catch (Throwable ignored) {}
        renderQuickSections();
        if (emptyView != null) {
            emptyView.setVisibility(store.getIngredientIds().isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Animate/pulse the FAB when selection present; stop when empty
        try {
            View fab = requireActivity().findViewById(R.id.fab_main);
            boolean hasAny = store != null && !store.getIngredientIds().isEmpty();
            if (fab != null) {
                Object tag = fab.getTag(R.id.tag_fab_pulse);
                if (hasAny) {
                    if (!(tag instanceof android.animation.ObjectAnimator)) {
                        fab.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                        android.animation.ObjectAnimator a = com.repasdelaflemme.app.ui.common.AnimUtils.pulseLoop(fab, 0.96f, 1.04f, 900L);
                        fab.setTag(R.id.tag_fab_pulse, a);
                    }
                } else {
                    if (tag instanceof android.animation.ObjectAnimator) {
                        try { ((android.animation.ObjectAnimator) tag).cancel(); } catch (Exception ignored2) {}
                        fab.setTag(R.id.tag_fab_pulse, null);
                        fab.setScaleX(1f); fab.setScaleY(1f);
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    private void renderQuickSections() {
        try {
            ChipGroup recents = (ChipGroup) (getView() != null ? getView().findViewById(R.id.chipsRecents) : null);
            ChipGroup frequents = (ChipGroup) (getView() != null ? getView().findViewById(R.id.chipsFrequents) : null);
            ChipGroup favs = (ChipGroup) (getView() != null ? getView().findViewById(R.id.chipsFavorites) : null);
            if (recents != null) {
                recents.removeAllViews();
                List<String> ids = stats.topRecent(10);
                addQuickChips(recents, ids);
            }
            if (frequents != null) {
                frequents.removeAllViews();
                List<String> ids = stats.topFrequent(10);
                addQuickChips(frequents, ids);
            }
            if (favs != null) {
                favs.removeAllViews();
                List<String> ids = new java.util.ArrayList<>(favorites.get());
                addQuickChips(favs, ids);
            }
        } catch (Throwable ignored) {}
    }

    private void addQuickChips(ChipGroup group, List<String> ids) {
        if (ids == null) return;
        int idx = 0;
        for (String id : ids) {
            Ingredient ing = ingredientIndex.get(id);
            if (ing == null) continue;
            ContextThemeWrapper chipCtx = new ContextThemeWrapper(requireContext(), R.style.Widget_CooknRest_Chip_Ingredient);
            Chip chip = new Chip(chipCtx);
            chip.setText(ing.name);
            // reflect current selection state
            boolean selected = store.getIngredientIds().contains(id);
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setChecked(selected);
            chip.setOnClickListener(v -> {
                boolean now = !selected;
                if (now) { store.add(id); try { stats.bump(id); } catch (Throwable ignored) {} }
                else { store.remove(id); }
                updateAfterChange();
            });
            group.addView(chip);
            AnimUtils.slideInKeyframesX(chip, idx * 16L);
            idx++;
        }
    }
}
