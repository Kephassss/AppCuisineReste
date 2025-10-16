package com.repasdelaflemme.app.ui.home;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import com.repasdelaflemme.app.R;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import android.view.animation.AnimationUtils;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(item -> {
            NavController nav = NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", item.id);
            nav.navigate(R.id.recipeDetailFragment, args);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<RecipeCard> cards = toCards(loadRecipes(requireContext()));
        adapter.submit(cards);

        // Update header stat with number of saved recipes (placeholder using items size)
        TextView stat = view.findViewById(R.id.homeStat);
        if (stat != null) {
            String txt = getString(R.string.home_saved_recipes, cards != null ? cards.size() : 0);
            stat.setText(txt);
        }

        // Shortcuts buttons to sections
        View btnPantry = view.findViewById(R.id.btnPantry);
        View btnDiscover = view.findViewById(R.id.btnDiscover);
        View btnFindByPantry = view.findViewById(R.id.btnFindByPantry);
        NavController navController = NavHostFragment.findNavController(this);
        if (btnPantry != null) {
            btnPantry.setOnClickListener(v -> navController.navigate(R.id.pantryFragment));
        }
        if (btnDiscover != null) {
            btnDiscover.setOnClickListener(v -> navController.navigate(R.id.recipesFragment));
        }
        if (btnFindByPantry != null) {
            btnFindByPantry.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putBoolean("focusPantry", true);
                navController.navigate(R.id.recipesFragment, args);
            });
        }
    }

    private List<Recipe> loadRecipes(Context context) {
        List<Recipe> list = AssetsRepository.getRecipes(context);
        if (list.isEmpty()) {
            Toast.makeText(context, getString(R.string.error_loading_recipes), Toast.LENGTH_SHORT).show();
        }
        return list;
    }

    private List<RecipeCard> toCards(List<Recipe> recipes) {
        PrefPantryStore store = new PrefPantryStore(requireContext());
        List<String> have = store.getIngredientIds();
        List<RecipeCard> out = new ArrayList<>();
        for (Recipe r : recipes) {
            int haveCount = 0;
            int total = 0;
            if (r.ingredients != null) {
                total = r.ingredients.size();
                for (RecipeIngredient ri : r.ingredients) {
                    if (have.contains(ri.id)) haveCount++;
                }
            }
            int missing = Math.max(0, total - haveCount);
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
            RecipeCard card = new RecipeCard(r.id, r.title, r.minutes, total == 0 ? null : (haveCount * 100 / Math.max(1, total)))
                    .withMissing(missing)
                    .withVegetarian(r.vegetarian != null && r.vegetarian)
                    .withImage(resId)
                    .withImageUrl(imageUrl);
            out.add(card);
        }
        return out;
    }
}
