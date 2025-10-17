package com.repasdelaflemme.app.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecipeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        adapter = new RecipeAdapter(item -> {
            NavController nav = NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", item.id);
            nav.navigate(R.id.recipeDetailFragment, args);
        });

        // RecyclerView in 2-column grid
        binding.recycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Show skeleton while loading
        binding.recycler.setAdapter(new SkeletonAdapter(6));
        binding.recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe and display home recipes
        homeViewModel.getRecipeCards().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                if (binding.recycler.getAdapter() != adapter) {
                    binding.recycler.setAdapter(adapter);
                }
                adapter.submit(items);

                String statText = requireContext().getString(R.string.home_saved_recipes, items.size());
                binding.homeStat.setText(statText);

                if (binding.emptyHome != null) {
                    binding.emptyHome.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });

        homeViewModel.loadRecipes(requireContext());

        setupShortcutButtons();
    }

    private void setupShortcutButtons() {
        NavController navController = NavHostFragment.findNavController(this);
        binding.btnPantry.setOnClickListener(v -> navController.navigate(R.id.pantryFragment));
        binding.btnDiscover.setOnClickListener(v -> navController.navigate(R.id.recipesFragment));
        binding.btnFindByPantry.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("focusPantry", true);
            navController.navigate(R.id.recipesFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

