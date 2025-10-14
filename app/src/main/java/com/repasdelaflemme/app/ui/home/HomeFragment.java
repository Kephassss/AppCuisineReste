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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.repasdelaflemme.app.R;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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
        adapter = new RecipeAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<RecipeItem> items = loadRecipes(requireContext());
        adapter.submit(items);

        // Update header stat with number of saved recipes (placeholder using items size)
        TextView stat = view.findViewById(R.id.homeStat);
        if (stat != null) {
            String txt = getString(R.string.home_saved_recipes, items != null ? items.size() : 0);
            stat.setText(txt);
        }

        // Shortcuts buttons to sections
        View btnPantry = view.findViewById(R.id.btnPantry);
        View btnDiscover = view.findViewById(R.id.btnDiscover);
        NavController navController = NavHostFragment.findNavController(this);
        if (btnPantry != null) {
            btnPantry.setOnClickListener(v -> navController.navigate(R.id.pantryFragment));
        }
        if (btnDiscover != null) {
            btnDiscover.setOnClickListener(v -> navController.navigate(R.id.recipesFragment));
        }
    }

    private List<RecipeItem> loadRecipes(Context context) {
        try {
            InputStream is = context.getAssets().open("recipes.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            Type listType = new TypeToken<ArrayList<RecipeItem>>(){}.getType();
            List<RecipeItem> list = new Gson().fromJson(reader, listType);
            reader.close();
            return list;
        } catch (Exception e) {
            Toast.makeText(context, "Erreur chargement recettes", Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }
    }

    // Model minimal
    public static class RecipeItem {
        public String id;
        public String title;
        public int durationMin;
    }
}
