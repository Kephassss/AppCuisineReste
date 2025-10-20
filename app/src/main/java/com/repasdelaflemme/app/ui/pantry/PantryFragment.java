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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private final java.util.Set<String> selectedCats = new java.util.HashSet<>();
    private List<Ingredient> catalogAll = new ArrayList<>();

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
        catalogAll = new ArrayList<>(catalog);
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

        // On n'utilise plus la liste principale pour parcourir tout le catalogue
        list.setVisibility(View.GONE);

        // Recherche + catÃ©gories
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

        View btnClearAll = view.findViewById(R.id.btnClearAll);
        if (btnClearAll != null) btnClearAll.setOnClickListener(v -> {
            java.util.List<String> previous = new java.util.ArrayList<>(store.getIngredientIds());
            store.setIngredientIds(new java.util.ArrayList<>());
            updateAfterChange();
            try {
                Snackbar.make(requireView(), getString(R.string.pantry_cleared), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.action_undo), x -> {
                            store.setIngredientIds(previous);
                            updateAfterChange();
                        }).show();
            } catch (Throwable ignored) {}
        });
        updateAfterChange();
    }

    private void setupCategoryChips(List<Ingredient> catalog) {
        if (chipsCategories == null || catalog == null) return;
        chipsCategories.removeAllViews();
        selectedCats.clear();
        java.util.Map<String, Integer> catScore = new java.util.HashMap<>();
        for (Ingredient i : catalog) {
            if (i.category == null || i.category.trim().isEmpty()) continue;
            int c = catScore.getOrDefault(i.category, 0);
            c += stats.getCount(i.id);
            catScore.put(i.category, c);
        }
        java.util.List<String> cats = new java.util.ArrayList<>(catScore.keySet());
        cats.sort((a,b) -> Integer.compare(catScore.getOrDefault(b,0), catScore.getOrDefault(a,0)));
        for (String c : cats) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(new ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Widget_Material3_Chip_Filter));
            chip.setText(c);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setOnClickListener(v -> openCategorySheet(c));
            chipsCategories.addView(chip);
        }
    }

    private void openCategorySheet(String category) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_category_ingredients, null);
        dialog.setContentView(sheet);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = sheet.findViewById(R.id.sheetTitle);
        if (title != null) title.setText(category);

        RecyclerView rv = sheet.findViewById(R.id.sheetList);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            PantryCatalogAdapter adapter = new PantryCatalogAdapter((ing, selected) -> {
                if (selected) { store.add(ing.id); try { stats.bump(ing.id); } catch (Throwable ignored) {} }
                else { store.remove(ing.id); }
                updateAfterChange();
            });
            adapter.setFavorites(favorites.get());
            adapter.setOnFavoriteToggle((ing, fav) -> {
                if (fav) favorites.add(ing.id); else favorites.remove(ing.id);
                renderQuickSections();
            });
            List<Ingredient> subset = new ArrayList<>();
            for (Ingredient i : catalogAll) {
                if (i != null && category.equals(i.category)) subset.add(i);
            }
            adapter.setCatalog(subset);
            adapter.setSelectedIds(store.getIngredientIds());
            rv.setAdapter(adapter);
        }

        View btnClose = sheet.findViewById(R.id.btnCloseSheet);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
        // Mettre Ã  jour les chips, la sÃ©lection du catalogue et l'Ã©tat vide
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
