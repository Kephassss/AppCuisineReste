package com.repasdelaflemme.app.ui.recipes;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.chip.Chip;
import android.widget.EditText;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import com.repasdelaflemme.app.ui.home.RecipeAdapter;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipesFragment extends Fragment {

    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView list = view.findViewById(R.id.recipesList);
        TextView empty = new TextView(getContext()); // fallback if needed, we use dataset for emptiness
        list.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));
        adapter = new RecipeAdapter(item -> {
            NavController nav = NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", item.id);
            nav.navigate(R.id.recipeDetailFragment, args);
        });
        list.setAdapter(adapter);
        list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));

        allRecipes = AssetsRepository.getRecipes(requireContext());

        EditText search = view.findViewById(R.id.inputSearch);
        Chip veg = view.findViewById(R.id.checkVegetarian);
        Chip quick = view.findViewById(R.id.checkQuick);
        Chip fromPantryOnly = view.findViewById(R.id.checkFromPantryOnly);
        Chip allowOneMissing = view.findViewById(R.id.checkAllowOneMissing);

        View.OnClickListener trigger = v -> applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing);
        veg.setOnClickListener(trigger);
        quick.setOnClickListener(trigger);
        fromPantryOnly.setOnClickListener(trigger);
        allowOneMissing.setOnClickListener(trigger);
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        boolean focusPantry = getArguments() != null && getArguments().getBoolean("focusPantry", false);
        if (focusPantry) {
            fromPantryOnly.setChecked(true);
        }
        // initial
        applyFilters(search, veg, quick, fromPantryOnly, allowOneMissing);
    }

    private void applyFilters(EditText search, Chip veg, Chip quick, Chip fromPantryOnly, Chip allowOneMissing) {
        String q = search.getText() != null ? search.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        boolean onlyVeg = veg.isChecked();
        boolean onlyQuick = quick.isChecked();
        boolean pantryOnly = fromPantryOnly.isChecked();
        boolean oneMissing = allowOneMissing.isChecked();

        PrefPantryStore store = new PrefPantryStore(requireContext());
        List<String> have = store.getIngredientIds();

        List<RecipeCard> out = new ArrayList<>();
        for (Recipe r : allRecipes) {
            if (onlyVeg && (r.vegetarian == null || !r.vegetarian)) continue;
            if (onlyQuick && r.minutes > 20) continue;

            String hay = (r.title != null ? r.title.toLowerCase(Locale.ROOT) : "");
            if (!q.isEmpty() && !hay.contains(q)) continue;

            int haveCount = 0;
            int total = r.ingredients != null ? r.ingredients.size() : 0;
            if (r.ingredients != null) {
                for (RecipeIngredient ri : r.ingredients) {
                    if (have.contains(ri.id)) haveCount++;
                }
            }
            int missing = Math.max(0, total - haveCount);
            if (pantryOnly && missing > 0) continue;
            if (!pantryOnly && oneMissing && missing > 1) continue;
            Integer resId = null; String imageUrl = null;
            if (r.image != null) {
                if (r.image.startsWith("res:")) {
                    String name = r.image.substring(4);
                    int id = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
                    if (id != 0) resId = id;
                } else if (r.image.startsWith("http")) {
                    imageUrl = r.image;
                }
            }
            RecipeCard card = new RecipeCard(r.id, r.title, r.minutes, total == 0 ? 0 : (haveCount * 100 / Math.max(1, total)))
                    .withMissing(missing)
                    .withVegetarian(r.vegetarian != null && r.vegetarian)
                    .withImage(resId)
                    .withImageUrl(imageUrl);
            out.add(card);
        }
        // naive sort by matchScore desc
        out.sort((a,b) -> Integer.compare(b.matchScore != null ? b.matchScore : 0, a.matchScore != null ? a.matchScore : 0));
        adapter.submit(out);
    }
}
