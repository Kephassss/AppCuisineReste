package com.repasdelaflemme.app.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Importation nécessaire
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.databinding.FragmentHomeBinding; // Importation du ViewBinding

public class HomeFragment extends Fragment {

    // Le ViewModel pour gérer les données de ce fragment
    private HomeViewModel homeViewModel;
    // Le ViewBinding pour accéder aux vues de manière sécurisée
    private FragmentHomeBinding binding;
    // L'adaptateur pour le RecyclerView
    private RecipeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Utilisation du ViewBinding pour infler le layout
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialisation de l'adaptateur pour les recettes
        adapter = new RecipeAdapter(item -> {
            NavController nav = NavHostFragment.findNavController(this);
            Bundle args = new Bundle();
            args.putString("recipeId", item.id);
            nav.navigate(R.id.recipeDetailFragment, args);
        });

        // Configuration du RecyclerView
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        // Affichage d'un squelette (skeleton) pendant le chargement
        binding.recycler.setAdapter(new SkeletonAdapter(6));
        binding.recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));

        return binding.getRoot(); // Retourne la vue racine du binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialisation du ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observation des données depuis le ViewModel
        homeViewModel.getRecipeCards().observe(getViewLifecycleOwner(), recipeCards -> {
            if (recipeCards != null) {
                // ... (other code)

                // Mise à jour du compteur de recettes (CORRECTED LINE)
                String statText = requireContext().getString(R.string.home_saved_recipes, recipeCards.size());
                binding.homeStat.setText(statText);
            }
        });

        // Chargement des données (le ViewModel s'en occupe en arrière-plan)
        homeViewModel.loadRecipes(requireContext());

        // Configuration des boutons de raccourci
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
        // Nettoyage de la référence au binding pour éviter les fuites de mémoire
        binding = null;
    }
}
