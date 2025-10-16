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

public class PantryFragment extends Fragment {

    private PrefPantryStore store;
    private Map<String, Ingredient> ingredientIndex = new HashMap<>();
    private PantryCatalogAdapter catalogAdapter;
    private RecyclerView list;
    private TextView emptyView;
    private ChipGroup chipsGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new PrefPantryStore(requireContext());

        List<Ingredient> catalog = AssetsRepository.getIngredients(requireContext());
        ingredientIndex.clear();
        for (Ingredient i : catalog) { ingredientIndex.put(i.id, i); }

        list = view.findViewById(R.id.pantryList);
        chipsGroup = view.findViewById(R.id.chipSelected);
        emptyView = view.findViewById(R.id.emptyView);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        catalogAdapter = new PantryCatalogAdapter((ing, selected) -> {
            if (selected) store.add(ing.id); else store.remove(ing.id);
            updateAfterChange();
        });
        list.setAdapter(catalogAdapter);
        list.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));

        // Suggestions
        TextView input = view.findViewById(R.id.inputSearchIngredient);
        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                catalogAdapter.filter(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        catalogAdapter.setCatalog(catalog);
        catalogAdapter.setSelectedIds(store.getIngredientIds());
        updateAfterChange();
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
        // Mettre à jour les chips, la sélection du catalogue et l’état vide
        if (catalogAdapter != null) {
            catalogAdapter.setSelectedIds(store.getIngredientIds());
        }
        renderChips(chipsGroup, getSelectedIngredients());
        if (emptyView != null) {
            emptyView.setVisibility(store.getIngredientIds().isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
