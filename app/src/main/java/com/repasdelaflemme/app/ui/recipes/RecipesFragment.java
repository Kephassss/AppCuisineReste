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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.repasdelaflemme.app.R;
import com.repasdelaflemme.app.data.AssetsRepository;
import com.repasdelaflemme.app.data.PrefPantryStore;
import com.repasdelaflemme.app.data.model.Recipe;
import com.repasdelaflemme.app.data.model.RecipeIngredient;
import com.repasdelaflemme.app.ui.common.RecipeCard;
import com.repasdelaflemme.app.ui.home.RecipeAdapter;
import android.view.animation.AnimationUtils;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.repasdelaflemme.app.data.remote.MealDbClient;
import com.repasdelaflemme.app.data.remote.MealDbResponse;

public class RecipesFragment extends Fragment {

    private RecyclerView listView;
    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private MealDbClient remote;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            this.rootView = view;

            // Configuration du RecyclerView
            RecyclerView list = view.findViewById(R.id.recipesList);
            View empty = view.findViewById(R.id.emptyView);

            if (list != null) {
                list.setLayoutManager(new GridLayoutManager(getContext(), 2));
                list.setAdapter(new com.repasdelaflemme.app.ui.home.SkeletonAdapter(8));
                list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_up));
                this.listView = list;
            }

            // Configuration de l'adaptateur
            adapter = new RecipeAdapter(item -> {
                try {
                    NavController nav = NavHostFragment.findNavController(this);
                    Bundle args = new Bundle();
                    args.putString("recipeId", item.id);
                    nav.navigate(R.id.recipeDetailFragment, args);
                } catch (Throwable ignored) {
                    Log.e("RecipesFragment", "Navigation error", ignored);
                }
            });

            // Chargement des recettes
            allRecipes = AssetsRepository.getRecipes(requireContext());
            try { remote = new MealDbClient(); } catch (Throwable t) { remote = null; Log.w("RecipesFragment", "Remote client init failed", t); }

            // Configuration des filtres
            setupFilters(view);

            // Toggle panneau des filtres avancés
            View advanced = view.findViewById(R.id.advancedFilters);
            View chipToggle = view.findViewById(R.id.checkAdvancedToggle);
            if (chipToggle != null && advanced != null) {
                chipToggle.setOnClickListener(v -> {
                    boolean show = advanced.getVisibility() != View.VISIBLE;
                    animateAdvanced(advanced, show);
                    updateAdvancedButton();
                });
                updateAdvancedButton();
            }

            // Actions réinitialiser/appliquer
            View btnReset = view.findViewById(R.id.btnResetFilters);
            if (btnReset != null) btnReset.setOnClickListener(v -> resetAllFilters());
            View btnApply = view.findViewById(R.id.btnApplyFilters);
            if (btnApply != null) btnApply.setOnClickListener(v -> applyFilters());

            // Focus initial sur le placard si demandé
            boolean focusPantry = getArguments() != null && getArguments().getBoolean("focusPantry", false);
            Chip fromPantryOnly = view.findViewById(R.id.checkFromPantryOnly);
            if (fromPantryOnly != null) {
                // Ne pas pré-sélectionner par défaut lorsque l'on arrive depuis "Trouver des recettes"
                if (focusPantry) { fromPantryOnly.setChecked(false); }
            }

            // Affichage initial
            applyFilters();

        } catch (Throwable fatal) {
            Log.e("RecipesFragment", "Fatal setup error", fatal);
            showSafeError();
            safePopulateFallback();
        }
    }

    private void setupFilters(View view) {
        // Récupération des filtres
        EditText search = view.findViewById(R.id.inputSearch);
        Chip veg = view.findViewById(R.id.checkVegetarian);
        Chip quick = view.findViewById(R.id.checkQuick);
        Chip fromPantryOnly = view.findViewById(R.id.checkFromPantryOnly);
        Chip allowOneMissing = view.findViewById(R.id.checkAllowOneMissing);

        // Configuration des listeners
        View.OnClickListener trigger = v -> applyFilters();

        if (veg != null) veg.setOnClickListener(trigger);
        if (quick != null) quick.setOnClickListener(trigger);
        if (fromPantryOnly != null) fromPantryOnly.setOnClickListener(trigger);
        if (allowOneMissing != null) allowOneMissing.setOnClickListener(trigger);

        // Tous les autres filtres
        int[] chipIds = {
                R.id.checkHalal, R.id.checkBudget1, R.id.checkBudget2, R.id.checkBudget3,
                R.id.checkTimeQuick, R.id.checkTimeMedium, R.id.checkTimeLong,
                R.id.checkUstPoele, R.id.checkUstCasserole, R.id.checkUstFour,
                R.id.checkUstWok, R.id.checkUstCocotte, R.id.checkUstMixeur,
                R.id.checkCuisineFrancais, R.id.checkCuisineItalien, R.id.checkCuisineAsiatique,
                R.id.checkCuisineIndien, R.id.checkCuisineMaghreb, R.id.checkCuisineMexicain,
                R.id.checkCuisineUS, R.id.checkCuisineMed, R.id.checkCuisineAutres,
                R.id.checkNoGluten, R.id.checkNoLactose, R.id.checkNoOeuf,
                R.id.checkNoArachide, R.id.checkNoFruitsCoque, R.id.checkNoCrustace,
                R.id.checkNoSoja, R.id.checkNoSesame, R.id.checkNoAlcohol, R.id.checkNoPork
        };

        for (int id : chipIds) {
            Chip chip = view.findViewById(id);
            if (chip != null) chip.setOnClickListener(v -> {
                trigger.onClick(v);
                updateAdvancedButton();
            });
        }

        // Recherche en temps réel
        if (search != null) {
            search.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void resetAllFilters() {
        if (rootView == null) return;
        try {
            // Clear search
            EditText search = rootView.findViewById(R.id.inputSearch);
            if (search != null) search.setText("");

            // All chip ids used across sections
            int[] ids = new int[]{
                    R.id.checkVegetarian, R.id.checkQuick, R.id.checkFromPantryOnly, R.id.checkAllowOneMissing,
                    R.id.checkHalal, R.id.checkBudget1, R.id.checkBudget2, R.id.checkBudget3,
                    R.id.checkTimeQuick, R.id.checkTimeMedium, R.id.checkTimeLong,
                    R.id.checkUstPoele, R.id.checkUstCasserole, R.id.checkUstFour,
                    R.id.checkUstWok, R.id.checkUstCocotte, R.id.checkUstMixeur,
                    R.id.checkCuisineFrancais, R.id.checkCuisineItalien, R.id.checkCuisineAsiatique,
                    R.id.checkCuisineIndien, R.id.checkCuisineMaghreb, R.id.checkCuisineMexicain,
                    R.id.checkCuisineUS, R.id.checkCuisineMed, R.id.checkCuisineAutres,
                    R.id.checkNoGluten, R.id.checkNoLactose, R.id.checkNoOeuf, R.id.checkNoArachide,
                    R.id.checkNoFruitsCoque, R.id.checkNoCrustace, R.id.checkNoSoja, R.id.checkNoSesame,
                    R.id.checkNoAlcohol, R.id.checkNoPork
            };
            for (int id : ids) {
                View v = rootView.findViewById(id);
                if (v instanceof Chip) ((Chip) v).setChecked(false);
            }
            applyFilters();
            updateAdvancedButton();
        } catch (Throwable t) {
            Log.w("RecipesFragment", "resetAllFilters failed", t);
        }
    }

    private void animateAdvanced(View view, boolean show) {
        try {
            if (show) {
                view.setAlpha(0f);
                view.setVisibility(View.VISIBLE);
                ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
                va.setDuration(200);
                va.setInterpolator(new AccelerateDecelerateInterpolator());
                va.addUpdateListener(a -> view.setAlpha((Float) a.getAnimatedValue()));
                va.start();
            } else {
                ValueAnimator va = ValueAnimator.ofFloat(1f, 0f);
                va.setDuration(200);
                va.setInterpolator(new AccelerateDecelerateInterpolator());
                va.addUpdateListener(a -> view.setAlpha((Float) a.getAnimatedValue()));
                va.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(android.animation.Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
                va.start();
            }
        } catch (Throwable ignored) { view.setVisibility(show ? View.VISIBLE : View.GONE); }
    }

    private int countActiveFilters() {
        if (rootView == null) return 0;
        int count = 0;
        try {
            int[] ids = new int[]{
                    R.id.checkVegetarian, R.id.checkQuick, R.id.checkFromPantryOnly, R.id.checkAllowOneMissing,
                    R.id.checkHalal, R.id.checkBudget1, R.id.checkBudget2, R.id.checkBudget3,
                    R.id.checkTimeQuick, R.id.checkTimeMedium, R.id.checkTimeLong,
                    R.id.checkUstPoele, R.id.checkUstCasserole, R.id.checkUstFour,
                    R.id.checkUstWok, R.id.checkUstCocotte, R.id.checkUstMixeur,
                    R.id.checkCuisineFrancais, R.id.checkCuisineItalien, R.id.checkCuisineAsiatique,
                    R.id.checkCuisineIndien, R.id.checkCuisineMaghreb, R.id.checkCuisineMexicain,
                    R.id.checkCuisineUS, R.id.checkCuisineMed, R.id.checkCuisineAutres,
                    R.id.checkNoGluten, R.id.checkNoLactose, R.id.checkNoOeuf, R.id.checkNoArachide,
                    R.id.checkNoFruitsCoque, R.id.checkNoCrustace, R.id.checkNoSoja, R.id.checkNoSesame,
                    R.id.checkNoAlcohol, R.id.checkNoPork
            };
            for (int id : ids) {
                View v = rootView.findViewById(id);
                if (v instanceof Chip && ((Chip) v).isChecked()) count++;
            }
            EditText search = rootView.findViewById(R.id.inputSearch);
            if (search != null && search.getText() != null && search.getText().toString().trim().length() > 0) count++;
        } catch (Throwable ignored) {}
        return count;
    }

    private void updateAdvancedButton() {
        try {
            com.google.android.material.chip.Chip chipToggle = (rootView != null) ? rootView.findViewById(R.id.checkAdvancedToggle) : null;
            View advanced = (rootView != null) ? rootView.findViewById(R.id.advancedFilters) : null;
            int n = countActiveFilters();
            boolean open = (advanced != null && advanced.getVisibility() == View.VISIBLE);
            if (chipToggle != null) {
                String base = getString(open ? R.string.filters_hide : R.string.filters_advanced);
                chipToggle.setText(n > 0 ? (base + " (" + n + ")") : base);
            }
        } catch (Throwable ignored) {}
    }

    private void applyFilters() {
        if (getContext() == null || rootView == null) return;

        try {
            // Récupération de la recherche
            String query = "";
            EditText search = rootView.findViewById(R.id.inputSearch);
            if (search != null && search.getText() != null) {
                query = search.getText().toString().trim().toLowerCase(Locale.ROOT);
            }

            // Récupération des filtres de base
            boolean onlyVeg = chipChecked(R.id.checkVegetarian);
            boolean onlyQuick = chipChecked(R.id.checkQuick);
            boolean pantryOnly = chipChecked(R.id.checkFromPantryOnly);
            boolean oneMissing = chipChecked(R.id.checkAllowOneMissing);

            // Récupération des filtres avancés
            boolean halalOnly = chipChecked(R.id.checkHalal);

            java.util.Set<Integer> budgets = new java.util.HashSet<>();
            if (chipChecked(R.id.checkBudget1)) budgets.add(1);
            if (chipChecked(R.id.checkBudget2)) budgets.add(2);
            if (chipChecked(R.id.checkBudget3)) budgets.add(3);

            java.util.Set<String> times = new java.util.HashSet<>();
            if (chipChecked(R.id.checkTimeQuick)) times.add("quick");
            if (chipChecked(R.id.checkTimeMedium)) times.add("medium");
            if (chipChecked(R.id.checkTimeLong)) times.add("long");

            java.util.Set<String> utensils = new java.util.HashSet<>();
            if (chipChecked(R.id.checkUstPoele)) utensils.add("poele");
            if (chipChecked(R.id.checkUstCasserole)) utensils.add("casserole");
            if (chipChecked(R.id.checkUstFour)) utensils.add("four");
            if (chipChecked(R.id.checkUstWok)) utensils.add("wok");
            if (chipChecked(R.id.checkUstCocotte)) utensils.add("cocotte");
            if (chipChecked(R.id.checkUstMixeur)) utensils.add("mixeur");

            java.util.Set<String> cuisines = new java.util.HashSet<>();
            if (chipChecked(R.id.checkCuisineFrancais)) cuisines.add("français");
            if (chipChecked(R.id.checkCuisineItalien)) cuisines.add("italien");
            if (chipChecked(R.id.checkCuisineAsiatique)) cuisines.add("asiatique");
            if (chipChecked(R.id.checkCuisineIndien)) cuisines.add("indien");
            if (chipChecked(R.id.checkCuisineMaghreb)) cuisines.add("maghrébin");
            if (chipChecked(R.id.checkCuisineMexicain)) cuisines.add("mexicain");
            if (chipChecked(R.id.checkCuisineUS)) cuisines.add("us");
            if (chipChecked(R.id.checkCuisineMed)) cuisines.add("méditerranéen");
            if (chipChecked(R.id.checkCuisineAutres)) cuisines.add("autres");

            java.util.Set<String> avoid = new java.util.HashSet<>();
            if (chipChecked(R.id.checkNoGluten)) avoid.add("gluten");
            if (chipChecked(R.id.checkNoLactose)) avoid.add("lactose");
            if (chipChecked(R.id.checkNoOeuf)) avoid.add("oeuf");
            if (chipChecked(R.id.checkNoArachide)) avoid.add("arachide");
            if (chipChecked(R.id.checkNoFruitsCoque)) avoid.add("fruits_a_coque");
            if (chipChecked(R.id.checkNoCrustace)) avoid.add("crustace");
            if (chipChecked(R.id.checkNoSoja)) avoid.add("soja");
            if (chipChecked(R.id.checkNoSesame)) avoid.add("sesame");

            boolean noAlcohol = chipChecked(R.id.checkNoAlcohol);
            boolean noPork = chipChecked(R.id.checkNoPork);

            // Récupération des ingrédients du placard
            List<String> have = new ArrayList<>();
            try {
                PrefPantryStore store = new PrefPantryStore(requireContext());
                have = store.getIngredientIds();
                if (have == null) have = new ArrayList<>();
            } catch (Throwable t) {
                Log.e("RecipesFragment", "Error loading pantry", t);
            }

            // Filtrage des recettes
            List<RecipeCard> out = new ArrayList<>();
            for (Recipe r : allRecipes) {
                // Filtres de base
                if (onlyVeg && (r.vegetarian == null || !r.vegetarian)) continue;
                if (onlyQuick && r.minutes > 20) continue;
                if (halalOnly && (r.halal == null || !r.halal)) continue;

                // Filtre budget
                if (!budgets.isEmpty() && (r.budget == null || !budgets.contains(r.budget))) continue;

                // Filtre temps
                if (!times.isEmpty()) {
                    boolean okTime = (r.minutes < 20 && times.contains("quick")) ||
                            (r.minutes >= 20 && r.minutes <= 40 && times.contains("medium")) ||
                            (r.minutes > 40 && times.contains("long"));
                    if (!okTime) continue;
                }

                // Filtre ustensiles
                if (!utensils.isEmpty()) {
                    boolean okU = false;
                    if (r.utensils != null) {
                        for (String u : r.utensils) {
                            if (utensils.contains(u)) {
                                okU = true;
                                break;
                            }
                        }
                    }
                    if (!okU) continue;
                }

                // Filtre cuisine (vérifie les flags directement, comparaison normalisée)
                boolean anyCui = chipChecked(R.id.checkCuisineFrancais) || chipChecked(R.id.checkCuisineItalien) ||
                        chipChecked(R.id.checkCuisineAsiatique) || chipChecked(R.id.checkCuisineIndien) ||
                        chipChecked(R.id.checkCuisineMaghreb) || chipChecked(R.id.checkCuisineMexicain) ||
                        chipChecked(R.id.checkCuisineUS) || chipChecked(R.id.checkCuisineMed) ||
                        chipChecked(R.id.checkCuisineAutres);
                if (anyCui) {
                    String cuRaw = r.cuisine != null ? r.cuisine : null;
                    String cu = cuRaw != null ? normalizeString(cuRaw) : null;
                    String key = mapCuisineKey(cu);
                    boolean okCui = false;
                    if (key != null) {
                        okCui = (chipChecked(R.id.checkCuisineFrancais) && "francais".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineItalien) && "italien".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineAsiatique) && "asiatique".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineIndien) && "indien".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineMaghreb) && "maghrebin".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineMexicain) && "mexicain".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineUS) && "us".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineMed) && "mediterraneen".equals(key)) ||
                                 (chipChecked(R.id.checkCuisineAutres) && "autres".equals(key));
                    }
                    if (!okCui) continue;
                }

                // Filtre allergènes
                if (!avoid.isEmpty() && r.allergens != null) {
                    boolean hasBad = false;
                    for (String a : r.allergens) {
                        if (avoid.contains(a)) {
                            hasBad = true;
                            break;
                        }
                    }
                    if (hasBad) continue;
                }

                if (noAlcohol && Boolean.TRUE.equals(r.containsAlcohol)) continue;
                if (noPork && Boolean.TRUE.equals(r.containsPork)) continue;

                // Recherche textuelle
                if (!query.isEmpty()) {
                    String hay = (r.title != null ? r.title.toLowerCase(Locale.ROOT) : "");
                    if (!hay.contains(query)) continue;
                }

                // Calcul du pourcentage de correspondance
                int haveCount = 0;
                int total = (r.ingredients != null) ? r.ingredients.size() : 0;
                if (r.ingredients != null) {
                    for (RecipeIngredient ri : r.ingredients) {
                        if (have.contains(ri.id)) haveCount++;
                    }
                }
                int missing = Math.max(0, total - haveCount);

                // Filtre placard
                if (pantryOnly && missing > 0) continue;
                if (!pantryOnly && oneMissing && missing > 1) continue;

                // Création de la carte
                Integer resId = null;
                String imageUrl = null;
                if (r.image != null) {
                    if (r.image.startsWith("res:")) {
                        String name = r.image.substring(4);
                        int id = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
                        if (id != 0) resId = id;
                    } else if (r.image.startsWith("http")) {
                        imageUrl = r.image;
                    }
                }

                int matchScore = total == 0 ? 0 : (haveCount * 100 / Math.max(1, total));
                RecipeCard card = new RecipeCard(r.id, r.title, r.minutes, matchScore)
                        .withMissing(missing)
                        .withVegetarian(r.vegetarian != null && r.vegetarian)
                        .withHalal(r.halal)
                        .withBudget(r.budget)
                        .withUtensils(r.utensils)
                        .withCuisine(r.cuisine)
                        .withAllergens(r.allergens)
                        .withContainsAlcohol(r.containsAlcohol)
                        .withContainsPork(r.containsPork)
                        .withImage(resId)
                        .withImageUrl(imageUrl);
                out.add(card);
            }

            // Tri par pourcentage de correspondance (décroissant)
            out.sort((a, b) -> {
                int scoreA = a.matchScore != null ? a.matchScore : 0;
                int scoreB = b.matchScore != null ? b.matchScore : 0;
                return Integer.compare(scoreB, scoreA);
            });

            // Mise à jour de l'affichage
            if (listView != null && listView.getAdapter() != adapter) {
                listView.setAdapter(adapter);
            }
            if (adapter != null) {
                adapter.submit(out);
            }

            // Gestion du message vide
            View emptyV = rootView.findViewById(R.id.emptyView);
            if (emptyV != null) {
                emptyV.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
            }

            // Recherche externe si query longue
            if (query.length() >= 3 && remote != null) {
                searchRemote(query, out);
            }

        } catch (Throwable t) {
            Log.e("RecipesFragment", "Error applying filters", t);
            showSafeError();
        }
    }

    private void searchRemote(String query, List<RecipeCard> currentResults) {
        try {
            remote.search(query, new MealDbClient.OnResult() {
                @Override
                public void onSuccess(MealDbResponse res) {
                    if (res == null || res.meals == null || getActivity() == null) return;

                    List<RecipeCard> remoteCards = new ArrayList<>();
                    for (MealDbResponse.Meal m : res.meals) {
                        RecipeCard c = new RecipeCard(
                                "mealdb_" + m.idMeal,
                                m.strMeal != null ? m.strMeal : "Recette",
                                20,
                                null
                        ).withImageUrl(m.strMealThumb);
                        remoteCards.add(c);
                    }

                    // De-duplicate by id first, then by normalized title
                    java.util.LinkedHashMap<String, RecipeCard> map = new java.util.LinkedHashMap<>();
                    for (RecipeCard rc : currentResults) {
                        if (rc != null && rc.id != null) map.put(rc.id, rc);
                    }
                    for (RecipeCard rc : remoteCards) {
                        if (rc != null && rc.id != null) map.put(rc.id, rc);
                    }
                    // Extra pass by title to avoid visual duplicates
                    java.util.LinkedHashMap<String, RecipeCard> byTitle = new java.util.LinkedHashMap<>();
                    for (RecipeCard rc : map.values()) {
                        String key = (rc.title != null ? rc.title.trim().toLowerCase(java.util.Locale.ROOT) : "");
                        if (!byTitle.containsKey(key)) byTitle.put(key, rc);
                    }
                    List<RecipeCard> combined = new ArrayList<>(byTitle.values());

                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) adapter.submit(combined);
                    });
                }

                @Override
                public void onError(Throwable t) {
                    Log.w("RecipesFragment", "Remote search error", t);
                }
            });
        } catch (Throwable t) {
            Log.w("RecipesFragment", "Remote search failed", t);
        }
    }

    private void safePopulateFallback() {
        try {
            if (getContext() == null) return;

            List<Recipe> src = !allRecipes.isEmpty() ? allRecipes : AssetsRepository.getRecipes(requireContext());
            List<String> have = new ArrayList<>();

            try {
                PrefPantryStore store = new PrefPantryStore(requireContext());
                have = store.getIngredientIds();
                if (have == null) have = new ArrayList<>();
            } catch (Throwable t) {
                Log.e("RecipesFragment", "Error in fallback", t);
            }

            List<RecipeCard> cards = new ArrayList<>();
            for (Recipe r : src) {
                int haveCount = 0;
                int total = r.ingredients != null ? r.ingredients.size() : 0;

                if (r.ingredients != null) {
                    for (RecipeIngredient ri : r.ingredients) {
                        if (have.contains(ri.id)) haveCount++;
                    }
                }

                Integer resId = null;
                String imageUrl = null;
                if (r.image != null) {
                    if (r.image.startsWith("res:")) {
                        String name = r.image.substring(4);
                        int id = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
                        if (id != 0) resId = id;
                    } else if (r.image.startsWith("http")) {
                        imageUrl = r.image;
                    }
                }

                int matchScore = total == 0 ? 0 : (haveCount * 100 / Math.max(1, total));
                RecipeCard c = new RecipeCard(r.id, r.title, r.minutes, matchScore)
                        .withMissing(Math.max(0, total - haveCount))
                        .withVegetarian(r.vegetarian != null && r.vegetarian)
                        .withHalal(r.halal)
                        .withBudget(r.budget)
                        .withUtensils(r.utensils)
                        .withCuisine(r.cuisine)
                        .withAllergens(r.allergens)
                        .withContainsAlcohol(r.containsAlcohol)
                        .withContainsPork(r.containsPork)
                        .withImage(resId)
                        .withImageUrl(imageUrl);
                cards.add(c);
            }

            cards.sort((a, b) -> {
                int scoreA = a.matchScore != null ? a.matchScore : 0;
                int scoreB = b.matchScore != null ? b.matchScore : 0;
                return Integer.compare(scoreB, scoreA);
            });

            if (listView != null && listView.getAdapter() != adapter) {
                listView.setAdapter(adapter);
            }
            if (adapter != null) {
                adapter.submit(cards);
            }

            View emptyV = rootView != null ? rootView.findViewById(R.id.emptyView) : null;
            if (emptyV != null) {
                emptyV.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
            }
        } catch (Throwable ignored) {
            Log.e("RecipesFragment", "Fallback failed", ignored);
        }
    }

    private boolean chipChecked(int id) {
        try {
            if (rootView == null) return false;
            View v = rootView.findViewById(id);
            return (v instanceof Chip) && ((Chip) v).isChecked();
        } catch (Throwable t) {
            return false;
        }
    }

    private void showSafeError() {
        try {
            if (getContext() != null) {
                android.widget.Toast.makeText(
                        getContext(),
                        getString(R.string.error_loading_recipes),
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Throwable ignored) {}
    }

    // Normalise une chaîne: minuscule + suppression des accents
    private static String normalizeString(String s) {
        try {
            if (s == null) return null;
            String lower = s.toLowerCase(java.util.Locale.ROOT);
            String norm = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD);
            return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Throwable t) {
            return s;
        }
    }

    // Map variations (genre/pluriel/accents) vers une cla canonique
    private static String mapCuisineKey(String normalized) {
        if (normalized == null || normalized.isEmpty()) return null;
        String s = normalized;
        // italia"n/e/enne
        if (s.contains("italien")) return "italien";
        // francais/francaise
        if (s.contains("francais") || s.contains("francaise")) return "francais";
        // asiatique
        if (s.contains("asiat")) return "asiatique";
        // indien/indienne
        if (s.startsWith("indien") || s.startsWith("indienn")) return "indien";
        // maghrebin/maghrebine
        if (s.contains("maghreb")) return "maghrebin";
        // mexicain/mexicaine
        if (s.contains("mexic")) return "mexicain";
        // mediterraneen/mediterraneenne
        if (s.contains("mediterran")) return "mediterraneen";
        // us/americaine/americain
        if ("us".equals(s) || s.contains("americ")) return "us";
        // autres
        if (s.contains("autre")) return "autres";
        return s;
    }
}
